package se.curanexus.medication.adapter.fass;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Fass API integration.
 *
 * Requires:
 * - Agreement with Lif (fass@lif.se)
 * - API credentials (provided by Lif)
 *
 * Configuration in application.yml:
 * <pre>
 * curanexus:
 *   fass:
 *     enabled: true
 *     base-url: https://api.fass.se
 *     api-key: ${FASS_API_KEY}
 *     connect-timeout: 10s
 *     read-timeout: 30s
 *     cache-ttl: 15m
 * </pre>
 */
@Configuration
@EnableConfigurationProperties(FassApiProperties.class)
public class FassConfiguration {

    @Bean
    public WebClient.Builder fassWebClientBuilder(FassApiProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) properties.getConnectTimeout().toMillis())
                .responseTimeout(properties.getReadTimeout())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(
                                properties.getReadTimeout().toSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(
                                properties.getReadTimeout().toSeconds(), TimeUnit.SECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
