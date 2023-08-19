package ru.veider.eventsreminder.di


import dagger.Binds
import dagger.Module
import ru.veider.eventsreminder.repo.Repo
import ru.veider.eventsreminder.repo.RepoImpl
import ru.veider.eventsreminder.repo.cache.CacheRepo
import ru.veider.eventsreminder.repo.cache.CacheRepoImpl
import ru.veider.eventsreminder.repo.local.LocalRepo
import ru.veider.eventsreminder.repo.local.LocalRepoImp
import ru.veider.eventsreminder.repo.remote.PhoneCalendarRepo
import ru.veider.eventsreminder.repo.remote.PhoneCalendarRepoImpl
import ru.veider.eventsreminder.repo.remote.PhoneContactsRepo
import ru.veider.eventsreminder.repo.remote.PhoneContactsRepoImpl

/*
* предоставляет возможность работать с Repo, IPhoneCalendarRepo и
* PhoneContactsRepo
* */
@Module(includes = [StorageModule::class])
interface EventsReminderDataModule {

    @Binds
    fun bindLocalRepo(
        localRepo: LocalRepoImp
    ): LocalRepo

    @Binds
    fun bindCacheRepo(
        cacheRepo : CacheRepoImpl
    ): CacheRepo

    @Binds
    fun bindPhoneCalendarRepo(
        iPhoneCalendarRepo: PhoneCalendarRepoImpl
    ): PhoneCalendarRepo

    @Binds
    fun bindPhoneContactsRepo(
        phoneContactsRepo: PhoneContactsRepoImpl
    ): PhoneContactsRepo

    @Binds
    fun bindsRepo(
        repo : RepoImpl
    ): Repo


}
