package be.habran.platewatcher

class PlateStabilizer(
    private val requiredHits: Int,
    private val minDelayMs: Long
) {
    private val hits = mutableMapOf<String, Int>()
    private var lastSavedPlate: String? = null
    private var lastSavedAt: Long = 0L

    fun accept(candidate: PlateCandidate): Boolean {
        val now = System.currentTimeMillis()
        val key = candidate.plate
        hits[key] = (hits[key] ?: 0) + 1

        val enoughHits = (hits[key] ?: 0) >= requiredHits
        val notDuplicate = lastSavedPlate != key || now - lastSavedAt > minDelayMs

        return if (enoughHits && notDuplicate) {
            lastSavedPlate = key
            lastSavedAt = now
            hits.clear()
            true
        } else false
    }
}
