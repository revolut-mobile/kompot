package com.revolut.kompot.navigable.flow.scroller

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.revolut.kompot.ExperimentalKompotApi
import com.revolut.kompot.R
import com.revolut.kompot.common.Event
import com.revolut.kompot.common.EventResult
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.binder.CompositeBinding
import com.revolut.kompot.navigable.findRootFlow
import com.revolut.kompot.navigable.flow.Back
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.navigable.flow.FlowLifecycleDelegate
import com.revolut.kompot.navigable.flow.FlowNavigationCommand
import com.revolut.kompot.navigable.flow.FlowServiceEventHandler
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.flow.PostFlowResult
import com.revolut.kompot.navigable.flow.Quit
import com.revolut.kompot.navigable.flow.quitFlow
import com.revolut.kompot.navigable.flow.scroller.steps.StepsChangeCommand
import com.revolut.kompot.navigable.utils.Preconditions
import com.revolut.kompot.view.ControllerContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalKompotApi
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseScrollerFlow<STEP : FlowStep, INPUT_DATA : IOData.Input, OUTPUT_DATA : IOData.Output>(
    val inputData: INPUT_DATA
) : Controller(), ScrollerFlow<OUTPUT_DATA>, EventsDispatcher {

    private val lifecycleDelegate by lazy {
        FlowLifecycleDelegate(
            controller = this,
            controllerModel = flowModel as ControllerModel,
            childControllerManagers = ::childControllerManagers,
            onActivityResultInternal = ::onActivityResultInternal
        )
    }

    private val serviceEventHandler by lazy {
        FlowServiceEventHandler(
            controller = this,
            controllerModel = flowModel as ControllerModel,
            parentController = parentController,
            parentControllerManager = parentControllerManager,
            view = view
        )
    }

    final override var onFlowResult: (data: OUTPUT_DATA) -> Unit = { }

    override val layoutId: Int
        get() = R.layout.flow_scroller

    private val controllersAdapter by lazy {
        ScrollerFlowControllersAdapter(
            layoutContainerId = itemContainerLayoutId,
            parentController = this,
            controllersCache = this.controllersCache,
            flowModel = flowModel,
        )
    }

    private val childControllerManagers
        get() = controllersAdapter.childControllerManagers

    protected open val itemContainerLayoutId = R.layout.flow_scroller_item_container

    open val scrollMode: ScrollMode = ScrollMode.PAGER

    private val layoutManager by lazy {
        val orientation = when (scrollMode) {
            ScrollMode.VERTICAL -> RecyclerView.VERTICAL
            ScrollMode.HORIZONTAL,
            ScrollMode.PAGER -> RecyclerView.HORIZONTAL
        }
        LinearLayoutManager(activity, orientation, false)
    }

    private val snapHelper: SnapHelper? by lazy {
        PagerSnapHelper().takeIf { scrollMode == ScrollMode.PAGER }
    }

    protected abstract val flowModel: ScrollerFlowModel<STEP, OUTPUT_DATA>

    override val controllerDelegates by lazy {
        component.getControllerExtensions()
    }

    private val tillDestroyBinding = CompositeBinding()

    @IdRes
    protected open val recyclerViewId: Int = R.id.recyclerView

    private lateinit var recyclerView: RecyclerView

    override fun createView(inflater: LayoutInflater): View {
        val controllerContainer = patchLayoutInflaterWithTheme(inflater).inflate(layoutId, null, false) as? ControllerContainer
            ?: throw IllegalStateException("Root ViewGroup should be ControllerContainer")

        controllerContainer.fitStatusBar = fitStatusBar
        view = controllerContainer as View
        view.tag = controllerName
        view.findViewById<RecyclerView>(recyclerViewId).apply {
            recyclerView = this
            layoutManager = this@BaseScrollerFlow.layoutManager
            adapter = controllersAdapter
            if (scrollMode == ScrollMode.PAGER) setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING)
            snapHelper?.attachToRecyclerView(this)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val position = this@BaseScrollerFlow.layoutManager.findFirstCompletelyVisibleItemPosition()
                        if (position != -1 && position in 0..controllersAdapter.currentList.size) {
                            onFullyVisibleStepPositionChanged(controllersAdapter.currentList[position])
                        }
                    }
                }
            })
        }
        return view
    }

    private fun scrollToSelectedStep(stepsChangeCommand: StepsChangeCommand<STEP>) {
        val step = stepsChangeCommand.selected ?: return

        val position = controllersAdapter.currentList.indexOf(step)
        if (position in 0..layoutManager.itemCount) {
            if (stepsChangeCommand.smoothScroll) {
                recyclerView.smoothScrollToPosition(position)
            } else {
                layoutManager.scrollToPosition(position)
            }
        }
    }

    final override fun onCreate() {
        super.onCreate()
        (flowModel as ControllerModel).injectDependencies(
            dialogDisplayer = findRootFlow().rootDialogDisplayer,
            eventsDispatcher = this,
            controllersCache = controllersCache,
            mainDispatcher = Dispatchers.Main.immediate
        )

        onCreateFlowView(view)
        lifecycleDelegate.onCreate()

        tillDestroyBinding += flowModel.navigationBinder()
            .bind(::processFlowNavigationCommand)

        flowModel.stepsCommands()
            .onEach { stepsCommand -> submitStepsChange(stepsCommand) }
            .launchIn(createdScope)
    }

    private fun submitStepsChange(stepsChangeCommand: StepsChangeCommand<STEP>) {
        if (controllersAdapter.currentList != stepsChangeCommand.steps) {
            controllersAdapter.submitList(stepsChangeCommand.steps) {
                scrollToSelectedStep(stepsChangeCommand)
            }
        } else {
            scrollToSelectedStep(stepsChangeCommand)
        }
    }

    final override fun onDestroy() {
        childControllerManagers.forEach { manager -> manager.onDestroy() }
        super.onDestroy()
        tillDestroyBinding.clear()
        onDestroyFlowView()
        lifecycleDelegate.onDestroy()
    }

    override fun onAttach() {
        super.onAttach()
        lifecycleDelegate.onAttach()
        childControllerManagers.reversed().forEach { manager -> manager.onAttach() }
    }

    override fun onDetach() {
        super.onDetach()
        lifecycleDelegate.onDetach()
    }

    override fun onTransitionStart(enter: Boolean) {
        super.onTransitionStart(enter)
        lifecycleDelegate.onTransitionStart(enter)
    }

    override fun onTransitionEnd(enter: Boolean) {
        super.onTransitionEnd(enter)
        lifecycleDelegate.onTransitionEnd(enter)
    }

    override fun onHostPaused() {
        super.onHostPaused()
        lifecycleDelegate.onHostPaused()
    }

    override fun onHostResumed() {
        super.onHostResumed()
        lifecycleDelegate.onHostResumed()
    }

    override fun onHostStarted() {
        super.onHostStarted()
        lifecycleDelegate.onHostStarted()
    }

    override fun onHostStopped() {
        super.onHostStopped()
        lifecycleDelegate.onHostStopped()
    }

    open fun onActivityResultInternal(requestCode: Int, resultCode: Int, data: Intent?) = Unit
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        lifecycleDelegate.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        lifecycleDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun handleEvent(event: Event): EventResult? = serviceEventHandler.handleEvent(event)

    protected open fun onCreateFlowView(view: View) = Unit

    protected open fun onDestroyFlowView() = Unit

    protected open fun onFullyVisibleStepPositionChanged(step: STEP) = Unit

    protected fun back() {
        if (!handleBack()) {
            (parentController as? BaseFlow<*, *, *>)?.back()
        }
    }

    final override fun handleBack(): Boolean {
        for (manager in childControllerManagers.reversed()) {
            if (manager.handleBack()) {
                return true
            }
        }

        if (!backEnabled) {
            return true
        }

        return super.handleBack()
    }

    private fun processFlowNavigationCommand(command: FlowNavigationCommand<STEP, OUTPUT_DATA>) = when (command) {
        is Back -> {
            Preconditions.requireMainThread("BaseScrollerFlow.back()")
            back()
        }
        is Quit -> {
            Preconditions.requireMainThread("BaseScrollerFlow.quit()")
            post { quitFlow() }
        }
        is PostFlowResult -> {
            Preconditions.requireMainThread("BaseScrollerFlow.postFlowResult()")
            onFlowResult(command.data)
        }
        else -> throw IllegalStateException("$command is not supported by the ScrollerFlow")
    }

    private fun post(action: () -> Unit) {
        createdScope.launch(Dispatchers.Main) {
            action()
        }
    }

    enum class ScrollMode {
        HORIZONTAL, VERTICAL, PAGER
    }
}