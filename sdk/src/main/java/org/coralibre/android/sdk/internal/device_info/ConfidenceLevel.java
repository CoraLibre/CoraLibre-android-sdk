package org.coralibre.android.sdk.internal.device_info;

public enum ConfidenceLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH;

    private static final int LOW_INT = 1;
    private static final int MEDIUM_INT = 2;
    private static final int HIGH_INT = 3;
    private static final String LOW_STR = "LOW";
    private static final String MEDIUM_STR = "MEDIUM";
    private static final String HIGH_STR = "HIGH";
    private static final String NONE_STR = "NONE";

    public static ConfidenceLevel getConfidenceLevel(int raw) {
        switch (raw) {
            case LOW_INT:
                return ConfidenceLevel.LOW;
            case MEDIUM_INT:
                return ConfidenceLevel.MEDIUM;
            case HIGH_INT:
                return ConfidenceLevel.HIGH;
            default:
                return ConfidenceLevel.NONE;
        }
    }

    public static ConfidenceLevel getConfidenceLevel(String raw) throws RuntimeException {
        switch (raw) {
            case NONE_STR:
                return ConfidenceLevel.NONE;
            case LOW_STR:
                return ConfidenceLevel.LOW;
            case MEDIUM_STR:
                return ConfidenceLevel.MEDIUM;
            case HIGH_STR:
                return ConfidenceLevel.HIGH;
            default:
                throw new RuntimeException("Unknown option: " + raw);
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case LOW:
                return LOW_STR;
            case MEDIUM:
                return MEDIUM_STR;
            case HIGH:
                return HIGH_STR;
            default:
                return NONE_STR;
        }
    }
}
