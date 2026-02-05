package com.receiptradar.domain.pipeline

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.receiptradar.domain.model.BoundingBox
import com.receiptradar.domain.model.OcrLine
import com.receiptradar.domain.model.OcrResult
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class OcrEngine @Inject constructor() {
    
    private val recognizer: TextRecognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    suspend fun recognize(bitmap: Bitmap): OcrResult = suspendCancellableCoroutine { continuation ->
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                android.util.Log.d("OcrEngine", "OCR: ${visionText.textBlocks.size} blocks, ${visionText.text.length} chars")
                
                val lines = mutableListOf<OcrLine>()
                var lineNumber = 0
                
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        val boundingBox = line.boundingBox?.let { rect ->
                            BoundingBox(rect.left, rect.top, rect.right, rect.bottom)
                        }
                        
                        val confidence = line.elements.mapNotNull { it.confidence }
                            .takeIf { it.isNotEmpty() }
                            ?.average()?.toFloat() ?: 0.8f
                        
                        lines.add(OcrLine(
                            text = line.text,
                            confidence = confidence,
                            boundingBox = boundingBox,
                            lineNumber = lineNumber++
                        ))
                    }
                }
                
                val sortedLines = lines.sortedBy { it.boundingBox?.top ?: 0 }
                    .mapIndexed { index, line -> line.copy(lineNumber = index) }
                
                val overallConfidence = sortedLines.map { it.confidence }.average().toFloat().takeIf { !it.isNaN() } ?: 0f
                
                continuation.resume(OcrResult(
                    rawText = visionText.text,
                    lines = sortedLines,
                    confidence = overallConfidence
                ))
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("OcrEngine", "OCR failed: ${exception.message}")
                continuation.resumeWithException(exception)
            }
    }

    fun close() {
        recognizer.close()
    }
}
