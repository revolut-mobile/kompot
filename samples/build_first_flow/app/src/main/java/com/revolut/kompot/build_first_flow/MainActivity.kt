package com.revolut.kompot.build_first_flow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.revolut.kompot.build_first_flow.flow.AddContactFlow
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
        rootFlow = AddContactFlow(),
    )
}