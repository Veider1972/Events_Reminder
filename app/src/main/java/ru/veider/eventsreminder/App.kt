package ru.veider.eventsreminder

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import ru.veider.eventsreminder.di.EventsReminderComponent
import ru.veider.eventsreminder.di.WidgetModule

class App : DaggerApplication() {
    companion object {
        lateinit var eventsReminderComponent: EventsReminderComponent
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        eventsReminderComponent =
            ru.veider.eventsreminder.di.DaggerEventsReminderComponent
                .builder()
                .withApp(this)
                .withContext(applicationContext)
                .widgetModule(WidgetModule(applicationContext))
                .build()
        return eventsReminderComponent
    }

}