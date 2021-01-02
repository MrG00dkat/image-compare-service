package de.brakhageit.imagecompareservice.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class CompareController {

    private final ImageComparesService imageComparesService;

    @Autowired
    public CompareController(ImageComparesService imageComparesService) {
        this.imageComparesService = imageComparesService;
    }

    @PostMapping("/compare")
    public CompareResult compare(@RequestBody @Valid CompareRequest request) {
        return imageComparesService.compare(request);
    }


}
