package com.receiptradar

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.receiptradar.worker.InsightComputeWorker
import dagger.hilt.android.HiltAndroidApp
import org.opencv.android.OpenCVLoader
import javax.inject.Inject

/**
 * Application class for Receipt Radar.
 * Initializes Hilt and WorkManager.
 */
@HiltAndroidApp
class ReceiptRadarApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize OpenCV for CLAHE thermal receipt enhancement
        if (OpenCVLoader.initDebug()) {
            android.util.Log.d("ReceiptRadarApp", "OpenCV initialized successfully")
        } else {
            android.util.Log.e("ReceiptRadarApp", "OpenCV initialization failed")
        }
        
        // Schedule periodic insight computation
        InsightComputeWorker.enqueuePeriodicWork(this)
    }
}
