package de.brakhageit.imagecompareservice.rest;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

@Component
public class CleanupService {

    @Async
    public void deleteWorkFiles(String correlationId) {
        Arrays.stream(Objects.requireNonNull(Paths.get(ImageComparesService.WORKING_DIR).toFile()
                .listFiles(f -> f.getName().startsWith(correlationId))))
                .forEach(File::delete);
    }

}
