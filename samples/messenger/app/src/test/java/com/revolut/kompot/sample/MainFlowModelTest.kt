package com.revolut.kompot.sample

import com.revolut.kompot.common.IOData
import com.revolut.kompot.core.test.assertion.test
import com.revolut.kompot.sample.ui.flows.main.MainFlowContract
import com.revolut.kompot.sample.ui.flows.main.MainFlowModel
import org.junit.jupiter.api.Test

class MainFlowModelTest {

    private val flowModel = MainFlowModel()

    @Test
    fun `initial step is chat list`() {
        flowModel.test()
            .assertStep(
                step = MainFlowContract.Step.ChatList,
                result = IOData.EmptyOutput
            )
    }

    @Test
    fun `should go to contacts list after tab switched`() {
        flowModel.test()
            .also {
                flowModel.onTabSelected("CONTACTS_TAB_ID")
            }
            .assertStep(
                step = MainFlowContract.Step.ContactList,
                result = IOData.EmptyOutput
            )
    }

}