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

        requireActivity().onBackPressedDispatcher.addCallback(this.viewLifecycleOwner, getBackPressInterceptor())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        kompotDelegate.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        kompotDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getBackPressInterceptor(): OnBackPressedCallback {
        val enabled = true
        return object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                isEnabled = false
                kompotDelegate.onBackPressed()
                isEnabled = true
            }
        }
    }

}

fun KompotFragment.registerHook(hook: ControllerHook, key: ControllerHook.Key<*>) {
    kompotDelegate.registerHook(hook, key)
}