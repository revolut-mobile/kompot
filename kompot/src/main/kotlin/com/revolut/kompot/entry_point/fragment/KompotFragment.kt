/*
 * Copyright (C) 2022 Revolut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolut.kompot.entry_point.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.revolut.kompot.R
import com.revolut.kompot.entry_point.KompotDelegate
import com.revolut.kompot.navigable.hooks.ControllerHook

abstract class KompotFragment : Fragment() {

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            isEnabled = false
            kompotDelegate.onBackPressed()
            isEnabled = true
        }
    }

    abstract fun config(): KompotConfig

    internal val kompotDelegate: KompotDelegate by lazy(LazyThreadSafetyMode.NONE) {
        config().createDelegate()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.kompot_root, container, false)

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        kompotDelegate.onViewCreated(this)

        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    @CallSuper
    override fun onDestroyView() {
        onBackPressedCallback.remove()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        kompotDelegate.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        kompotDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}

fun KompotFragment.registerHook(hook: ControllerHook, key: ControllerHook.Key<*>) {
    kompotDelegate.registerHook(hook, key)
}