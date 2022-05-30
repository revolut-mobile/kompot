package com.revolut.kompot.navigable.screen

import android.content.Context
import android.os.Parcelable
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.revolut.decorations.dividers.DelegatesDividerItemDecoration
import com.revolut.decorations.overlay.DelegatesOverlayItemDecoration
import com.revolut.kompot.R
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.hooks.BaseRecyclerViewScreenHook
import com.revolut.recyclerkit.delegates.RecyclerViewDelegate
import com.revolut.rxdiffadapter.RxDiffAdapter

abstract class BaseRecyclerViewScreen<
        UI_STATE : ScreenStates.UIList,
        INPUT_DATA : IOData.Input,
        OUTPUT_DATA : IOData.Output>(inputData: INPUT_DATA) :
    BaseScreen<UI_STATE, INPUT_DATA, OUTPUT_DATA>(inputData) {

    override val layoutId: Int = R.layout.screen_recycler_view

    protected abstract val delegates: List<RecyclerViewDelegate<*, *>>
    protected lateinit var recyclerView: RecyclerView
    @IdRes
    protected open val recyclerViewId: Int = R.id.recyclerView
    protected lateinit var layoutManager: RecyclerView.LayoutManager
    protected open val autoScrollToTop = true
    protected open val saveRecyclerViewState = true
    private var recyclerViewState: Parcelable? = null

    protected open val listAdapter: RxDiffAdapter by lazy(LazyThreadSafetyMode.NONE) {
        RxDiffAdapter(
            delegates = emptyList(),
            async = false,
            autoScrollToTop = autoScrollToTop,
            detectMoves = true
        )
    }

    @CallSuper
    override fun onScreenViewCreated(view: View) {
        recyclerView = view.findViewById(recyclerViewId)
        recyclerView.apply {
            adapter = listAdapter.also { adapter ->
                adapter.delegatesManager.addDelegates(delegates)
            }

            layoutManager = createLayoutManager(view.context).also { layoutManager ->
                this@BaseRecyclerViewScreen.layoutManager = layoutManager
            }

            itemAnimator = DefaultItemAnimator().apply {
                supportsChangeAnimations = false
            }
            addItemDecoration(DelegatesDividerItemDecoration())
            addItemDecoration(DelegatesOverlayItemDecoration())

            hooksProvider?.getHook(BaseRecyclerViewScreenHook)?.patchRecyclerView?.invoke(this)
        }
    }

    @CallSuper
    override fun bindScreen(uiState: UI_STATE, payload: ScreenStates.UIPayload?) {
        listAdapter.setItems(uiState.items)
    }

    override fun onScreenViewAttached(view: View) {
        super.onScreenViewAttached(view)

        if (recyclerViewState != null) {
            layoutManager.onRestoreInstanceState(recyclerViewState)
        }
    }

    override fun onScreenViewDetached() {
        super.onScreenViewDetached()

        if (saveRecyclerViewState) {
            recyclerViewState = layoutManager.onSaveInstanceState()
        }
    }

    protected open fun createLayoutManager(context: Context): RecyclerView.LayoutManager =
        BaseRecyclerViewLayoutManager(context).apply {
            enablePredictiveItemAnimations = true
        }
}

class BaseRecyclerViewLayoutManager(context: Context) : LinearLayoutManager(context) {
    var scrollHorizontallyEnabled = true
    var scrollVerticallyEnabled = true
    var enablePredictiveItemAnimations = false

    override fun supportsPredictiveItemAnimations(): Boolean = enablePredictiveItemAnimations && super.supportsPredictiveItemAnimations()

    override fun canScrollHorizontally(): Boolean = scrollHorizontallyEnabled && super.canScrollHorizontally()

    override fun canScrollVertically(): Boolean = scrollVerticallyEnabled && super.canScrollVertically()
}

class BaseRecyclerViewGridLayoutManager(context: Context, spanSize: Int) : GridLayoutManager(context, spanSize) {

    var enablePredictiveItemAnimations = false

    override fun supportsPredictiveItemAnimations(): Boolean = enablePredictiveItemAnimations && super.supportsPredictiveItemAnimations()

}
