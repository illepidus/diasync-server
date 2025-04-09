package ru.krotarnya.diasync.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.krotarnya.diasync.service.AppVersionProvider;

@RestController
@RequestMapping("api/v1")
public class VersionController {
    private final AppVersionProvider versionService;

    public VersionController(AppVersionProvider versionService) {
        this.versionService = versionService;
    }

    @GetMapping("version")
    public String version() {
        return versionService.getVersion();
    }
}
