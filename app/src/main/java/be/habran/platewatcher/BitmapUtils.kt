package be.habran.platewatcher

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object BitmapUtils {
    fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 85, out)
        val bytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun cropSafely(bitmap: Bitmap, rect: Rect, padding: Int = 12): Bitmap {
        val left = max(0, rect.left - padding)
        val top = max(0, rect.top - padding)
        val right = min(bitmap.width, rect.right + padding)
        val bottom = min(bitmap.height, rect.bottom + padding)
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }

    fun forwardVehicleRoi(bitmap: Bitmap): Bitmap {
        // Zone centrale : évite le ciel/tableau de bord et accélère l’OCR.
        val left = (bitmap.width * 0.12f).toInt()
        val top = (bitmap.height * 0.28f).toInt()
        val width = (bitmap.width * 0.76f).toInt()
        val height = (bitmap.height * 0.52f).toInt()
        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }

    fun saveJpeg(bitmap: Bitmap, directory: File, fileName: String): String {
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, fileName)
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out) }
        return file.absolutePath
    }
}
