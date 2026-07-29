package in.maithilart.gateway.security;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import in.maithilart.common.constants.MaithilConstants;
import reactor.core.publisher.Mono;

@Component
public class RequestTracer implements GlobalFilter, Ordered {
	

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		String correlationId = exchange.getRequest().getHeaders().getFirst(MaithilConstants.CORRELATION_ID_HEADER);

		if (correlationId != null && !correlationId.isBlank()) {

			try {
				UUID.fromString(correlationId);
			} catch (IllegalArgumentException ex) {
				correlationId = null;
			}
		}

		if (correlationId == null) {
			correlationId = UUID.randomUUID().toString();
		}
		// 1. Header downstream bhej rahe hain
		ServerHttpRequest request = exchange.getRequest().mutate().header(MaithilConstants.CORRELATION_ID_HEADER, correlationId).build();

		// 2. Response mein bhi bhej dete hain (Debugging ke liye easy hota hai)
		exchange.getResponse().getHeaders().set(MaithilConstants.CORRELATION_ID_HEADER, correlationId);

		return chain.filter(exchange.mutate().request(request).build());
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE; // Sabse pehle chalna chahiye
	}
}