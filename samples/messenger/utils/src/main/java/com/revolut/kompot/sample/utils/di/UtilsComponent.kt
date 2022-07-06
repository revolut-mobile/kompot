package com.revolut.kompot.sample.utils.di

import com.revolut.kompot.sample.utils.LazySingletonHolder
import com.revolut.kompot.sample.utils.api.UtilsApi
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [UtilsModule::class])
interface UtilsComponent : UtilsApi {
    @Component.Factory
    interface Factory {
        fun create(): UtilsComponent
    }
}

class CoreUtilsApiProvider {

    companion object : LazySingletonHolder<UtilsApi, Unit>({
        DaggerUtilsComponent
            .factory()
            .create()
    })

}