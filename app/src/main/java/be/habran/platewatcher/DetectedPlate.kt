package be.habran.platewatcher

import android.graphics.Rect

data class PlateCandidate(
    val plate: String,
    val country: String?,
    val confidence: Float,
    val boundingBox: Rect?
)
