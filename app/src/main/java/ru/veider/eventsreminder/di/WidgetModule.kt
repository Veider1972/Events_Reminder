package ru.veider.eventsreminder.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.veider.eventsreminder.widget.MyWidgetRemoteViewsService
import ru.veider.eventsreminder.widget.WidgetRemoteViewsFactory
import javax.inject.Inject
import javax.inject.Singleton

@Module
class WidgetModule
    @Inject
    constructor(val context:Context) {
    @Singleton
    @Provides
    fun provideMyWidgetRemoteViewsService() = MyWidgetRemoteViewsService()
    @Singleton
    @Provides
    fun provideWidgetRemoteViewsFactory() = WidgetRemoteViewsFactory(context)

}


