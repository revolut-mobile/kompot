package com.revolut.kompot.build_first_screen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.revolut.kompot.build_first_screen.flow.RootFlowImpl
import com.revolut.kompot.entry_point.fragment.KompotConfig
import com.revolut.kompot.entry_point.fragment.KompotFragment

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