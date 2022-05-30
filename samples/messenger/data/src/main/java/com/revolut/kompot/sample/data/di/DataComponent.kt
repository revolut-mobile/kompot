package com.revolut.kompot.sample.data.di

import android.app.Application
import com.revolut.kompot.sample.data.api.DataApi
import com.revolut.kompot.sample.utils.LazySingletonHolder
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DataModule::class])
interface DataComponent : DataApi {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ) : DataComponent
    }
}

data class DataArguments(
    internal val application: Application
)

class DataApiProvider {

    companion object : LazySingletonHolder<DataApi, DataArguments>({ args ->
        DaggerDataComponent
            .factory()
            .create(
                application = args.application
            )
    })

}