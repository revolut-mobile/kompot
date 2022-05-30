package com.revolut.kompot.navigable.flow

import android.os.Bundle
import android.os.Parcelable
import com.revolut.kompot.navigable.ControllerKey
import com.revolut.kompot.navigable.TransitionAnimation
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class FlowStateWrapper<STATE : FlowState, STEP : FlowStep>(
    val state: STATE,
    val step: STEP,
    val childFlowState: ChildFlowState? = null,
    val currentScreenState: Bundle? = null,
    val animation: TransitionAnimation = TransitionAnimation.NONE,
    val currentControllerKey: ControllerKey? = null
): Parcelable

@Parcelize
internal data class ChildFlowState(
    val stateWrapper: FlowStateWrapper<*, *>,
    val backStack: List<FlowStateWrapper<*, *>>,
) : Parcelable