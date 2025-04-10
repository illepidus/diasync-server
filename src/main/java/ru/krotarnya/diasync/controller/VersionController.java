package ru.krotarnya.diasync.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.krotarnya.diasync.service.AppVersionProvider;

@RestController
public final class VersionController extends RestApiController {
    private final AppVersionProvider versionService;

    public VersionController(AppVersionProvider versionService) {
        this.versionService = versionService;
    }

    @GetMapping("version")
    public String version() {
        return versionService.getVersion();
    }
}
