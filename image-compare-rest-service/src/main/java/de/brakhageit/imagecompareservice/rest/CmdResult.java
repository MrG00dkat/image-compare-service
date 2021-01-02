package de.brakhageit.imagecompareservice.rest;

import java.util.List;

public class CmdResult {

    private final int exitCode;
    private final List<String> lines;

    public CmdResult(int exitCode, List<String> lines) {
        this.exitCode = exitCode;
        this.lines = lines;
    }

    public int getExitCode() {
        return exitCode;
    }

    public List<String> getLines() {
        return lines;
    }
}
