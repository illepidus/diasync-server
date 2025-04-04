package ru.krotarnya.diasync.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {
    @GetMapping("ping")
    @ApiResponses(value = @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(example = "pong"))))
    public String ping() {
        return "pong";
    }
}
