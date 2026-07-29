package in.maithilart.gateway.ratelimit;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

	private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

	private final RateLimitKeyGenerator keyGenerator;
	private final RateLimitService rateLimitService;
	private static final List<String> EXCLUDED_PATHS = List.of("/actuator", "/v3/api-docs", "/swagger-ui");

	public RateLimitFilter(RateLimitKeyGenerator keyGenerator, RateLimitService rateLimitService) {
		this.keyGenerator = keyGenerator;
		this.rateLimitService = rateLimitService;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		String path = exchange.getRequest().getURI().getPath();
		log.info("path: " + path);
		if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
			return chain.filter(exchange);
		}

		String key = keyGenerator.generate(exchange);
		log.info("key="+key);
		if (rateLimitService.isAllowed(key)) {
			log.info("ALLOWED");
			return chain.filter(exchange);
		}

		exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		String body = """
				{
				    "status":"FAILED",
				    "code":"RATE_LIMIT_EXCEEDED",
				    "message":"Too many requests. Please try again later."
				}
				""";

		return exchange.getResponse()
				.writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes())));
	}

	@Override
	public int getOrder() {
		return -90;
	}
}