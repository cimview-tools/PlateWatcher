package be.habran.platewatcher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var statusText: TextView
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private lateinit var db: PlateDatabase

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startCamera() else statusText.text = "Permission caméra refusée"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = PlateDatabase.create(this)

        previewView = PreviewView(this)
        statusText = TextView(this).apply {
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x88000000.toInt())
            text = "Initialisation…"
            setPadding(24, 24, 24, 24)
        }
        val root = FrameLayout(this)
        root.addView(previewView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        root.addView(statusText, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        setContentView(root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, PlateAnalyzer(
                        plateImageDirectory = filesDir.resolve("plate-crops"),
                        onPlateDetected = { candidate ->
                            runOnUiThread {
                                statusText.text = "${candidate.plate}  •  ${candidate.country ?: "Pays ?"}  •  ${(candidate.confidence * 100).toInt()}%"
                            }
                        },
                        onStablePlate = { candidate, cropPath ->
                            val now = System.currentTimeMillis()
                            db.plateDao().deleteOlderThan(now - 7L * 24L * 60L * 60L * 1000L)
                            db.plateDao().insert(
                                PlateRecord(
                                    plate = candidate.plate,
                                    country = candidate.country,
                                    confidence = candidate.confidence,
                                    detectedAt = now,
                                    imagePath = cropPath
                                )
                            )
                        }
                    ))
                }

            provider.unbindAll()
            provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analyzer
            )
            statusText.text = "Caméra active — recherche plaque…"
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
