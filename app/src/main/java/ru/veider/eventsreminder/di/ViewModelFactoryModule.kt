package ru.veider.eventsreminder.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.veider.eventsreminder.presentation.ui.dashboard.DashboardViewModel
import ru.veider.eventsreminder.presentation.ui.myevents.MyEventsViewModel

/*
* модуль предоставляет доступ к фабрике вьюмодели.
* */
@Module
interface ViewModelFactoryModule {
    @Binds
    fun bindsViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
    @Binds
    @IntoMap
    @ViewModelKey(DashboardViewModel::class)
    fun bindDashboardViewModel(viewModel: DashboardViewModel): ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(MyEventsViewModel::class)
    fun bindMyEventsViewModel(viewModel: MyEventsViewModel): ViewModel
}
