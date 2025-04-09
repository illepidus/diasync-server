package ru.krotarnya.diasync.service;

import java.util.Optional;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

@Service
public class AppVersionProvider {
    private final BuildProperties buildProperties;

    public AppVersionProvider(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public String getVersion() {
        return Optional.ofNullable(buildProperties)
                .map(BuildProperties::getVersion)
                .orElse("");
    }
}
