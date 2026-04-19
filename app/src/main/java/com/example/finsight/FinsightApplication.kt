package com.example.finsight

import android.app.Application
import com.example.finsight.utils.SeedDataUtil
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FinsightApplication : Application() {

    @Inject
    lateinit var seedDataUtil: SeedDataUtil

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            seedDataUtil.seedIfEmpty()
        }
    }
}
