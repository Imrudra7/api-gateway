package in.maithilart.gateway.security;

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

    @Value("${gateway.secret}")
    private String gatewaySecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Har request ko mutate karke secret header add karo
        ServerHttpRequest request = exchange.getRequest().mutate()
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