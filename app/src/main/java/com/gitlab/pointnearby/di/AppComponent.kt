package com.gitlab.pointnearby.di

import com.gitlab.pointnearby.PointNearbyApp
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ActivityModule::class,
        AppModule::class,
        ViewModelModule::class
    ]
)
interface AppComponent: AndroidInjector<PointNearbyApp> {
    override fun inject(instance: PointNearbyApp?)

    @Component.Builder
    interface Builder{
        @BindsInstance
        fun application(application: PointNearbyApp): Builder

        fun build(): AppComponent
    }
}