package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts

import android.view.View
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.BaseRecyclerViewScreen
import com.revolut.kompot.sample.feature.contacts.R
import com.revolut.kompot.sample.feature.contacts.databinding.ScreenContactListBinding
import com.revolut.kompot.sample.feature.contacts.di.ContactsApiProvider
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListScreenContract.UIState
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.di.ContactListScreenComponent
import com.revolut.kompot.sample.ui_common.RowDelegate
import com.revolut.kompot.navigable.utils.viewBinding

class ContactListScreen : BaseRecyclerViewScreen<UIState, IOData.EmptyInput, IOData.EmptyOutput>(IOData.EmptyInput) {

    override val layoutId: Int = R.layout.screen_contact_list
    private val binding by viewBinding(ScreenContactListBinding::bind)

    override val fitStatusBar: Boolean = true

    private val rowDelegate = RowDelegate()

    override val delegates = listOf(rowDelegate)

    override val screenComponent: ContactListScreenComponent by lazy(LazyThreadSafetyMode.NONE) {
        ContactsApiProvider.component
            .getContactListScreenComponentBuilder()
            .screen(this)
            .inputData(inputData)
            .build()
    }

    override val screenModel by lazy {
        screenComponent.screenModel
    }

    override fun onScreenViewAttached(view: View) {
        super.onScreenViewAttached(view)

        binding.btnAction.setOnClickListener {
            screenModel.onActionClick()
        }
    }

}