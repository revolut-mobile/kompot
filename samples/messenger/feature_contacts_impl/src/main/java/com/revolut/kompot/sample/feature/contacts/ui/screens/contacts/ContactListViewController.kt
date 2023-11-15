package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts

import android.view.View
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.utils.viewBinding
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.composite.modal_ui_states.ModalHostUIListStatesController
import com.revolut.kompot.navigable.vc.composite.modal_ui_states.ModelBinding
import com.revolut.kompot.sample.feature.contacts.R
import com.revolut.kompot.sample.feature.contacts.databinding.ScreenContactListBinding
import com.revolut.kompot.sample.feature.contacts.di.ContactsApiProvider
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListContract.UIState
import com.revolut.kompot.sample.ui_common.RowDelegate

class ContactListViewController : ViewController<IOData.EmptyOutput>(),
    ModalHostUIListStatesController<UIState> {

    override val layoutId: Int = R.layout.screen_contact_list
    private val binding by viewBinding(ScreenContactListBinding::bind)

    override val fitStatusBar: Boolean = true
    private val rowDelegate = RowDelegate()

    override val component by lazy(LazyThreadSafetyMode.NONE) {
        ContactsApiProvider.component
            .getContactListComponentBuilder()
            .controller(this)
            .build()
    }
    override val controllerModel by lazy(LazyThreadSafetyMode.NONE) {
        component.model
    }
    override val modelBinding by lazy(LazyThreadSafetyMode.NONE) {
        ModelBinding(
            model = controllerModel,
            delegates = listOf(rowDelegate)
        )
    }

    override fun onShown(view: View) {
        binding.btnAction.setOnClickListener {
            controllerModel.onActionClick()
        }
    }

}