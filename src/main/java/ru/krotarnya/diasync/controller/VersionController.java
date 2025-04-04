package ru.krotarnya.diasync.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {
    @Value("${git.branch:unknown}")
    private String branch;

    @Value("${git.commit.id:unknown}")
    private String commitId;

    @GetMapping("version")
    public String version() {
        return branch + "@" + commitId;
    }
}
