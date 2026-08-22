package in.maithilart.gateway.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GlobalGatewaySecurityFilter implements GlobalFilter, Ordered {
    private static final List<String> CLIENT_FORBIDDEN_HEADERS = List.of("X-Gateway-Auth", "X-Gateway-Secret",
            "X-Internal-Secret", "X-User-Id", "X-User-Email", "X-User-Full-Name", "X-Roles", "X-Token-Issuer");

    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Strip any client-supplied internal headers before adding the gateway-owned secret.
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> CLIENT_FORBIDDEN_HEADERS.forEach(headers::remove))
                .header("X-Gateway-Secret", gatewaySecret)
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        // Isko sabse pehle chalna chahiye (-1 priority)
        return -1;
    }
}
