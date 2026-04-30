package be.habran.platewatcher

import android.graphics.Bitmap
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class PlateAnalyzer(
    private val plateImageDirectory: File,
    private val onPlateDetected: (PlateCandidate) -> Unit,
    private val onStablePlate: (PlateCandidate, String?) -> Unit
) : ImageAnalysis.Analyzer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val busy = AtomicBoolean(false)
    private val stabilizer = PlateStabilizer(requiredHits = 3, minDelayMs = 5000)

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!busy.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val bitmap = BitmapUtils.imageProxyToBitmap(imageProxy)
        imageProxy.close()

        if (bitmap == null) {
            busy.set(false)
            return
        }

        val roi = BitmapUtils.forwardVehicleRoi(bitmap)
        val image = InputImage.fromBitmap(roi, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val candidate = extractBestPlateFromTextBlocks(visionText.textBlocks)
                if (candidate != null) {
                    onPlateDetected(candidate)
                    if (stabilizer.accept(candidate)) {
                        val cropPath = savePlateCropIfPossible(roi, candidate)
                        onStablePlate(candidate, cropPath)
                    }
                }
            }
            .addOnFailureListener { /* ignore frame */ }
            .addOnCompleteListener {
                busy.set(false)
                bitmap.recycle()
                roi.recycle()
            }
    }

    private fun extractBestPlateFromTextBlocks(blocks: List<com.google.mlkit.vision.text.Text.TextBlock>): PlateCandidate? {
        val candidates = mutableListOf<PlateCandidate>()

        for (block in blocks) {
            for (line in block.lines) {
                val tokens = line.text
                    .uppercase()
                    .split(Regex("[^A-Z0-9]+"))
                    .flatMap { token ->
                        val windows = mutableListOf(token)
                        if (token.length >= 7) windows += token.windowed(7, 1)
                        if (token.length >= 8) windows += token.windowed(8, 1)
                        windows
                    }
                    .distinct()

                for (token in tokens) {
                    val classified = PlateClassifier.classify(token)
                    if (classified != null) {
                        candidates += classified.copy(boundingBox = line.boundingBox)
                    }
                }
            }
        }

        return candidates.maxByOrNull { it.confidence }
    }

    private fun savePlateCropIfPossible(roi: Bitmap, candidate: PlateCandidate): String? {
        val box = candidate.boundingBox ?: return null
        return try {
            val crop = BitmapUtils.cropSafely(roi, box, padding = 24)
            val path = BitmapUtils.saveJpeg(
                crop,
                plateImageDirectory,
                "plate_${System.currentTimeMillis()}_${candidate.plate}.jpg"
            )
            crop.recycle()
            path
        } catch (_: Exception) {
            null
        }
    }
}
