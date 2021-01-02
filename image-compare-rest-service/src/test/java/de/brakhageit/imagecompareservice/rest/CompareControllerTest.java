package de.brakhageit.imagecompareservice.rest;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CompareControllerTest {

    private static final Path OUTPUT_DIR = Paths.get("target", "junit-output-files");
    private static final String URL_COMPARE = "/api/v1/compare";

    @Autowired
    private MockMvc mockMvc;


    @BeforeAll
    public static void setUp() throws IOException {
        if (Files.notExists(OUTPUT_DIR)) {
            Files.createDirectories(OUTPUT_DIR);
        }
    }

    @ParameterizedTest(name = "[{index}] {0} compare to {1} should be {3}% equal")
    @CsvSource({
            "example_1.pdf,example_1.pdf,comparePdfWitExamplePdf1andPdf1.pdf,0.0",
            "example_1.pdf,example_2.pdf,comparePdfWitExamplePdf1andPdf2.pdf,6.28314",
            "example_1.pdf,example_3.pdf,comparePdfWitExamplePdf1andPdf3.pdf,36.629675",
            "example_1.pdf,example_2-0.png,comparePdfWitExamplePdf1andPng2-0.pdf,36.52259",
            "example_1.pdf,example_2-0.jpg,comparePdfWitExamplePdf1andJpg2-0.pdf,68.916",
            "example_1-0.jpg,example_2-0.jpg,comparePdfWitExampleJpg1-0andJpg2-0.pdf,3.60594",
            "example_1.tif,example_2.tif,comparePdfWitExampleTif1andTif2.pdf,9.325935",
            "example_1.tif,example_3.tif,comparePdfWitExampleTif1andTif3.pdf,11.841735",
    })
    void compare(String filename1, String filename2, String outputFileName, double expectedPercentageDifference) throws Exception {

        String pathFile1 = new File(getClass().getClassLoader().getResource(filename1).getFile()).getAbsolutePath();
        String pathFile2 = new File(getClass().getClassLoader().getResource(filename2).getFile()).getAbsolutePath();

        String json = "{\n" +
                "  \"correlationId\": \"" + outputFileName + "\",\n" +
                "  \"file1\" : \"" + new String(Base64.getEncoder().encode(Files.readAllBytes(Paths.get(pathFile1)))) + "\",\n" +
                "  \"file2\" : \"" + new String(Base64.getEncoder().encode(Files.readAllBytes(Paths.get(pathFile2)))) + "\"\n" +
                "}";

        MvcResult result = mockMvc.perform(post(URL_COMPARE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentageDifference", closeTo(expectedPercentageDifference, 0.000001)))
                .andExpect(jsonPath("$.compareResultFile", not(blankOrNullString())))
                .andExpect(jsonPath("$.correlationId", equalTo(outputFileName)))
                .andReturn();

        byte[] compareResultFile = Base64.getDecoder().decode((String) JsonPath.read(result.getResponse().getContentAsString(), "$.compareResultFile"));
        Files.write(OUTPUT_DIR.resolve(outputFileName), compareResultFile);
    }

    @Test
    @DisplayName("Compare with no correlationId should generate one")
    void compareWitNoCorrelationId() throws Exception {
        String fileContent = new String(Base64.getEncoder().encode(
                Files.readAllBytes(Paths.get(new File(getClass().getClassLoader().getResource("example_1.pdf")
                        .getFile()).getAbsolutePath()))));

        String json = "{\n" +
                "  \"file1\" : \"" + fileContent + "\",\n" +
                "  \"file2\" : \"" + fileContent + "\"\n" +
                "}";

        mockMvc.perform(post(URL_COMPARE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentageDifference", closeTo(0.0, 0.000001)))
                .andExpect(jsonPath("$.compareResultFile", not(blankOrNullString())))
                .andExpect(jsonPath("$.correlationId", not(blankOrNullString())));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("compareWithBadRequestProvider")
    void compareWithBadRequest(String displayName, String json) throws Exception {
        mockMvc.perform(post(URL_COMPARE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    static Stream<Arguments> compareWithBadRequestProvider() throws IOException {

        String fileContent = new String(Base64.getEncoder().encode(
                Files.readAllBytes(Paths.get(new File(CompareControllerTest.class.getClassLoader().getResource("example_1.pdf")
                        .getFile()).getAbsolutePath()))));

        return Stream.of(
                arguments("With file1 is missing", "{ \"file2\" : \"" + fileContent + "\"}"),
                arguments("With file2 is missing", "{ \"file1\" : \"" + fileContent + "\"}"),
                arguments("With file1 is null", "{ \"file1\" : null, \"file2\" : \"" + fileContent + "\"}"),
                arguments("With file2 is null", "{ \"file1\" : \"" + fileContent + "\", \"file2\" : null}"),
                arguments("With file1 is empty", "{ \"file1\" : \"\", \"file2\" : \"" + fileContent + "\""),
                arguments("With file2 is empty", "{ \"file1\" : \"" + fileContent + "\", \"file2\" : \"\"")
        );
    }

}