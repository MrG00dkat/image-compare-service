package de.brakhageit.imagecompareservice.rest;

public class CompareResult {

    private String correlationId;
    private double percentageDifference;
    private byte[] compareResultFile;

    public CompareResult(String correlationId, double percentageDifference, byte[] compareResultFile) {
        this.correlationId = correlationId;
        this.percentageDifference = percentageDifference;
        this.compareResultFile = compareResultFile;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public byte[] getCompareResultFile() {
        return compareResultFile;
    }

    public void setCompareResultFile(byte[] compareResultFile) {
        this.compareResultFile = compareResultFile;
    }

    public double getPercentageDifference() {
        return percentageDifference;
    }

    public void setPercentageDifference(double percentageDifference) {
        this.percentageDifference = percentageDifference;
    }
}
