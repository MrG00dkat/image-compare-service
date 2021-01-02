package de.brakhageit.imagecompareservice.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ImageMagicService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ImageMagicService.class);

    private final CmdExecutor cmdExecutor;

    @Autowired
    public ImageMagicService(CmdExecutor cmdExecutor) {
        this.cmdExecutor = cmdExecutor;
    }

    public List<File> convertToPng(String correlationId, byte[] fileData, String filename) {

        Path inputFilePath = Paths.get("/tmp", filename);
        String inputFilePathStr = inputFilePath.toAbsolutePath().toString();

        try {
            Files.write(inputFilePath, fileData);

            if (filename.toLowerCase().endsWith(".png")) {
                return new ArrayList<File>() {{
                    add(inputFilePath.toFile());
                }};
            }

            cmdExecutor.run(correlationId, "convert", "-density", "300", inputFilePathStr,
                    "PNG:" + StringUtils.substringBeforeLast(inputFilePathStr, ".") + ".png");


            return Arrays.stream(Objects.requireNonNull(Paths.get("/tmp").toFile()
                    .listFiles(f -> isConvertResultFile(f, filename))))
                    .sorted((f1, f2) -> StringUtils.compare(f1.getName(), f2.getName()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("error on converting to png", e);
        }
    }

    public void convertToPdf(String correlationId, List<String> files, String outputFileName) {
        // convert -units PixelsPerInch image1.png image2.png image3.png -density 96 output.pdf

        int cmdPos = 0;
        String[] cmd = new String[6 + files.size()];
        cmd[cmdPos++] = "convert";
        cmd[cmdPos++] = "-units";
        cmd[cmdPos++] = "PixelsPerInch";
        for (String fileName : files) {
            cmd[cmdPos++] = fileName;
        }
        cmd[cmdPos++] = "-density";
        cmd[cmdPos++] = "96";
        cmd[cmdPos] = outputFileName;


        try {
            cmdExecutor.run(correlationId, cmd);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("error on converting to pdf", e);
        }
    }

    public double compare(String correlationId, String filename1, String filename2, String filenameResult) {
        try {
            CmdResult result = cmdExecutor.run(correlationId, "compare", "-metric", "RMSE", filename1, filename2, filenameResult);
            if (result.getExitCode() == 0) {
                LOGGER.info("{} and {} are equal", filename1, filename2);
                return 0.0;

            } else if (result.getExitCode() == 1) {
                String percentageDifferenceStr = StringUtils.substringBetween(result.getLines().get(0), "(", ")");
                double percentageDifference = Double.parseDouble(percentageDifferenceStr);
                LOGGER.info("{} and {} are not equal; difference: {}%", filename1, filename2, percentageDifference);

                return percentageDifference;
            } else {
                throw new RuntimeException("error on comparing files");
            }
        } catch (Exception e) {
            throw new RuntimeException("error on comparing files", e);
        }
    }

    private boolean isConvertResultFile(File file, String originalFileName) {
        String fileName = file.getName().toLowerCase();
        return fileName.startsWith(StringUtils.substringBeforeLast(originalFileName, ".").toLowerCase())
                && fileName.endsWith(".png");
    }
}
