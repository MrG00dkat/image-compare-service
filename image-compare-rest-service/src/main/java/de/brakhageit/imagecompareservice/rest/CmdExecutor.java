package de.brakhageit.imagecompareservice.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

@Component
public class CmdExecutor {

    private final static Logger LOGGER = LoggerFactory.getLogger(CmdExecutor.class);

    CmdResult run(String correlationId, String... cmd) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();

        builder.command(cmd);

        LOGGER.info("[{}] start executing command: {}", correlationId, join(builder.command(), ' '));

        //builder.directory(new File("/Users/mario/dev/pdf-comperator/"));
        Process process = builder.start();

        List<String> lines = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }

        reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }

        int exitCode = process.waitFor();

        lines.forEach(l -> LOGGER.info("[{}] > {}", correlationId, l));

        return new CmdResult(exitCode, lines);
    }

}
