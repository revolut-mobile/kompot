package com.revolut.kompot.common.service

import com.revolut.kompot.navigable.Controller

internal data class ScreenAddedEvent(val screen: Controller, val parentFlow: Controller, val animated: Boolean) : ServiceEvent()