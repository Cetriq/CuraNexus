package se.curanexus.integration.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Configuration
public class GatewayConfig {

    @Bean
    @Order(-1)
    public GlobalFilter requestTimingFilter() {
        return (exchange, chain) -> {
            long startTime = System.currentTimeMillis();
            exchange.getAttributes().put("requestStartTime", startTime);

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                Long start = exchange.getAttribute("requestStartTime");
                if (start != null) {
                    long duration = System.currentTimeMillis() - start;
                    exchange.getResponse().getHeaders().add("X-Response-Time", duration + "ms");
                }
            }));
        };
    }

    @Bean
    @Order(-2)
    public GlobalFilter requestIdFilter() {
        return (exchange, chain) -> {
            String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
            if (requestId == null) {
                requestId = java.util.UUID.randomUUID().toString();
            }

            exchange.getAttributes().put("requestId", requestId);
            exchange.getResponse().getHeaders().add("X-Request-ID", requestId);

            return chain.filter(exchange);
        };
    }

    @Bean
    @Order(-3)
    public GlobalFilter timestampFilter() {
        return (exchange, chain) -> {
            exchange.getResponse().getHeaders().add("X-Gateway-Timestamp", Instant.now().toString());
            return chain.filter(exchange);
        };
    }
}
