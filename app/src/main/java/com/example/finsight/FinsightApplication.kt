package com.example.finsight

import android.app.Application
import com.example.finsight.presentation.screens.settings.SettingsDataStore
import com.example.finsight.utils.SeedDataUtil
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FinsightApplication : Application()

//@HiltAndroidApp
//class FinsightApplication : Application() {
//
//    @Inject
//    lateinit var seedDataUtil: SeedDataUtil
//
//    @Inject
//    lateinit var settingsDataStore: SettingsDataStore
//
//    override fun onCreate() {
//        super.onCreate()
//        CoroutineScope(Dispatchers.IO).launch {
//            settingsDataStore.userName.first().let { name ->
//                if (name == "Guest") {
//                    seedDataUtil.seedIfEmpty()
//                }
//            }
//        }
//    }
//}
