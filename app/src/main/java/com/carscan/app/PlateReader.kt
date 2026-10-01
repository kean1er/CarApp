package com.carscan.app

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object PlateReader {
    suspend fun read(ctx: Context, uri: Uri): String? = withContext(Dispatchers.Default) {
        try {
            val image = InputImage.fromFilePath(ctx, uri)
            val client = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val res: Text = suspendCancellableCoroutine { cont ->
                client.process(image)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            client.close()
            val lines = res.textBlocks.flatMap { b -> b.lines.map { l -> l.text } }
            lines.firstNotNullOfOrNull { Plate.findIn(it) }
                ?: lines.zipWithNext { a, b -> a + b }.firstNotNullOfOrNull { Plate.findIn(it) }
                ?: Plate.findIn(res.text)
        } catch (e: Exception) {
            null
        }
    }
}
