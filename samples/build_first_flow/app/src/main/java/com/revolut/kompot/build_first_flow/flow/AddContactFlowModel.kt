package com.revolut.kompot.build_first_flow.flow

import com.revolut.kompot.build_first_flow.flow.AddContactFlowContract.State
import com.revolut.kompot.build_first_flow.flow.AddContactFlowContract.Step
import com.revolut.kompot.build_first_flow.screen.input.InputScreen
import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract.InputData
import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract.InputType
import com.revolut.kompot.build_first_flow.screen.text.TextScreen
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.root.BaseRootFlowModel
import javax.inject.Inject

internal class AddContactFlowModel @Inject constructor() : BaseRootFlowModel<State, Step>(),
    AddContactFlowContract.FlowModelApi {

    override val initialStep = Step.InputFirstName
    override val initialState = State()

    override fun getController(step: Step): Controller = when (step) {
        is Step.InputFirstName -> InputScreen(InputData(InputType.FIRST_NAME)).apply {
            onScreenResult = { output ->
                currentState = currentState.copy(firstName = output.text)
                next(Step.InputLastName, addCurrentStepToBackStack = true)
            }
        }
        is Step.InputLastName -> InputScreen(InputData(InputType.LAST_NAME)).apply {
            onScreenResult =  { output ->
                val firstName = currentState.firstName.orEmpty()
                val lastName = output.text
                next(Step.Success(firstName, lastName), addCurrentStepToBackStack = true)
            }
        }
        is Step.Success -> TextScreen(getSuccessText(step.firstName, step.lastName))
    }

    private fun getSuccessText(firstName: String, lastName: String) =
        "New contact created: $firstName $lastName"

}