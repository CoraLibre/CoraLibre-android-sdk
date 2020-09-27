package org.coralibre.android.sdk.internal.database.model;

public class MeasuredExposure {
    private final DiagnosisKey diagnosisKey;
    private final CapturedData capturedData;

    public MeasuredExposure(final DiagnosisKey diagnosisKey, final CapturedData capturedData) {
        this.diagnosisKey = diagnosisKey;
        this.capturedData = capturedData;
    }

    public DiagnosisKey getDiagnosisKey() {
        return diagnosisKey;
    }

    public CapturedData getCapturedData() {
        return capturedData;
    }
}
