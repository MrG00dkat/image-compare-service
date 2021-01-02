package de.brakhageit.imagecompareservice.rest;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageComparesService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ImageComparesService.class);

    public static final String WORKING_DIR = "/tmp";

    private final Tika tika = new Tika();
    private final ImageMagicService imageMagic;
    private final CleanupService cleanupService;

    @Autowired
    public ImageComparesService(ImageMagicService imageMagic, CleanupService cleanupService) {
        this.imageMagic = imageMagic;
        this.cleanupService = cleanupService;
    }

    public CompareResult compare(CompareRequest compareRequest) {

        final String correlationId = compareRequest.getCorrelationId();

        try {

            String extension1 = StringUtils.substringAfter(tika.detect(compareRequest.getFile1()), "/");
            String extension2 = StringUtils.substringAfter(tika.detect(compareRequest.getFile2()), "/");

            LOGGER.info("[{}] file 1 is a {}", correlationId, extension1);
            LOGGER.info("[{}] file 2 is a {}", correlationId, extension2);

            List<File> pages1 = imageMagic.convertToPng(correlationId, compareRequest.getFile1(),
                    correlationId + "_file1." + extension1);
            List<File> pages2 = imageMagic.convertToPng(correlationId, compareRequest.getFile2(),
                    correlationId + "_file2." + extension2);

            double percentageDifference = 0.0;

            List<String> outputFiles = new ArrayList<>();
            int amountOfPages = Integer.max(pages1.size(), pages2.size());
            for (int page = 0; page < amountOfPages; page++) {
                String filename1 = getFilenameOrWhitePage(correlationId, pages1, page);
                String filename2 = getFilenameOrWhitePage(correlationId, pages2, page);

                String outputFile = String.format(WORKING_DIR + "/" + correlationId + "_result_%05d.png", page);
                percentageDifference += imageMagic.compare(correlationId, filename1, filename2, outputFile);
                outputFiles.add(outputFile);
            }

            String resultFile = WORKING_DIR + "/" + correlationId + "_result.pdf";
            imageMagic.convertToPdf(correlationId, outputFiles, resultFile);

            percentageDifference = percentageDifference * 100.0 / amountOfPages;

            return new CompareResult(correlationId, percentageDifference, Files.readAllBytes(Paths.get(resultFile)));
        } catch (IOException e) {
            throw new RuntimeException("error on comparing", e);
        } finally {
            cleanupService.deleteWorkFiles(correlationId);
        }
    }

    private String getFilenameOrWhitePage(String correlationId, List<File> pages, int pageIndex) throws IOException {
        if (pageIndex < pages.size()) {
            return pages.get(pageIndex).getAbsolutePath();
        } else {
            String filename = WORKING_DIR + "/" + correlationId + "_white-page.png";
            createEmptyPngPage(filename);
            return filename;
        }
    }

    private void createEmptyPngPage(String filename) throws IOException {
        int width = 1;
        int height = 1;

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // fill all the image with white
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);

        // Disposes of this graphics context and releases any system resources that it is using.
        g2d.dispose();

        // Save as PNG
        File file = new File(filename);
        ImageIO.write(bufferedImage, "png", file);
    }


}
