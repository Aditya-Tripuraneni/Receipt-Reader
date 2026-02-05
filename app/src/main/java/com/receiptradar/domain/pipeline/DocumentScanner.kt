package com.receiptradar.domain.pipeline

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentScanner @Inject constructor() {
    
    private val scannerOptions = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(true)
        .setPageLimit(1)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .build()
    
    private val scanner: GmsDocumentScanner = GmsDocumentScanning.getClient(scannerOptions)

    fun getScanner(): GmsDocumentScanner = scanner

    fun parseResult(resultCode: Int, data: Intent?): ScanResult {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return ScanResult.Cancelled
        }
        
        val result = GmsDocumentScanningResult.fromActivityResultIntent(data)
        val pages = result?.pages
        
        if (pages.isNullOrEmpty()) {
            return ScanResult.Error("No pages scanned")
        }
        
        val imageUri = pages.first().imageUri
        return ScanResult.Success(imageUri)
    }

    sealed class ScanResult {
        data class Success(val imageUri: android.net.Uri) : ScanResult()
        data object Cancelled : ScanResult()
        data class Error(val message: String) : ScanResult()
    }
}
