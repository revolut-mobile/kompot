package com.revolut.kompot.navigable

import android.content.Intent

interface ControllerExtension {
    fun onCreate() = Unit

    fun onDestroy() = Unit

    fun onAttach() = Unit

    fun onDetach() = Unit

    fun handleBack(): Boolean = false

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = Unit

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = Unit
}