package com.revolut.kompot.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.revolut.kompot.entry_point.fragment.KompotConfig
import com.revolut.kompot.entry_point.fragment.KompotFragment
import com.revolut.kompot.sample.ui.flows.root.RootFlowImpl

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

class AppKompotFragment : KompotFragment() {

    override fun config(): KompotConfig = KompotConfig(
        rootFlow = RootFlowImpl(),
    )
}