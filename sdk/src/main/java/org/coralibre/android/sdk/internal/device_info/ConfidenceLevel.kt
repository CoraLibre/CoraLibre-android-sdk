package org.coralibre.android.sdk.internal.device_info

enum class ConfidenceLevel {
    NONE, LOW, MEDIUM, HIGH;

    override fun toString(): String {
        return when (this) {
            LOW -> LOW_STR
            MEDIUM -> MEDIUM_STR
            HIGH -> HIGH_STR
            else -> NONE_STR
        }
    }

    companion object {
        private const val LOW_INT = 1
        private const val MEDIUM_INT = 2
        private const val HIGH_INT = 3
        private const val LOW_STR = "LOW"
        private const val MEDIUM_STR = "MEDIUM"
        private const val HIGH_STR = "HIGH"
        private const val NONE_STR = "NONE"

        @JvmStatic
        fun getConfidenceLevel(raw: Int): ConfidenceLevel {
            return when (raw) {
                LOW_INT -> LOW
                MEDIUM_INT -> MEDIUM
                HIGH_INT -> HIGH
                else -> NONE
            }
        }

        @JvmStatic
        @Throws(RuntimeException::class)
        fun getConfidenceLevel(raw: String): ConfidenceLevel {
            return when (raw) {
                NONE_STR -> NONE
                LOW_STR -> LOW
                MEDIUM_STR -> MEDIUM
                HIGH_STR -> HIGH
                else -> throw RuntimeException("Unknown option: $raw")
            }
        }
    }
}
