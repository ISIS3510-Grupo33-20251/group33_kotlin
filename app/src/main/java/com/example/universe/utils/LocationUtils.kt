object LocationUtils {
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    fun formatDistance(meters: Float): String {
        return when {
            meters < 1000 -> "${meters.toInt()} m"
            else -> String.format("%.1f km", meters / 1000)
        }
    }
}