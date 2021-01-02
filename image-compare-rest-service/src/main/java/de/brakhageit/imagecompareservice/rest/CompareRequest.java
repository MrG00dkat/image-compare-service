package de.brakhageit.imagecompareservice.rest;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@Valid
public class CompareRequest {

    private String correlationId = UUID.randomUUID().toString();

    @NotEmpty
    private byte[] file1;

    @NotEmpty
    private byte[] file2;

    public CompareRequest() {
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public byte[] getFile1() {
        return file1;
    }

    public void setFile1(byte[] file1) {
        this.file1 = file1;
    }

    public byte[] getFile2() {
        return file2;
    }

    public void setFile2(byte[] file2) {
        this.file2 = file2;
    }
}
