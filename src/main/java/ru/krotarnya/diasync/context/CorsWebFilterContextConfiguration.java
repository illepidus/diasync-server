package ru.krotarnya.diasync.context;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

/**
 * @author ivblinov
 */
@Configuration
public class CorsWebFilterContextConfiguration {
    @Bean
    public WebFilter corsWebFilter() {
        return (exchange, chain) -> {
            Optional.ofNullable(exchange.getRequest().getHeaders().getOrigin())
                    .ifPresent(origin -> exchange.getResponse()
                            .getHeaders()
                            .add("Access-Control-Allow-Origin", origin));

            exchange.getResponse()
                    .getHeaders()
                    .add("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Connection, Upgrade");

            return chain.filter(exchange);
        };
    }
}
