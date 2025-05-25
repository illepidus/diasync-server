package ru.krotarnya.diasync.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author ivblinov
 */

@Configuration
public class KioskWebConfigContextConfiguration {
    @Bean
    public RouterFunction<ServerResponse> kioskRouter() {
        return RouterFunctions
                .route(RequestPredicates.GET("/kiosk"),
                        req -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .bodyValue(new ClassPathResource("static/kiosk/index.html")));
    }
}
