package in.maithilart.gateway.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
// 1. AbstractGatewayFilterFactory extend karna zaroori hai
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {
	private static final Logger log = LoggerFactory.getLogger(JwtAuthGatewayFilterFactory.class);
	private final JwtService jwtService;
	private final ReactiveRedisTemplate<String, String> redisTemplate;

	public JwtAuthGatewayFilterFactory(JwtService jwtService, ReactiveRedisTemplate<String, String> redisTemplate) {
		super(Config.class); // 2. Config class pass karna zaroori hai
		this.jwtService = jwtService;
		this.redisTemplate = redisTemplate;

	}

	private boolean shouldNotFilter(ServerWebExchange exchange) {
		String path = exchange.getRequest().getURI().getPath();

		// Infra / swagger ko skip
		return path.startsWith("/swagger") || path.startsWith("/v3/api-docs") || path.startsWith("/actuator");
	}

	@Override
	public GatewayFilter apply(Config config) {

		log.info("Inside GatewayFilter");

		return (exchange, chain) -> {
			if (shouldNotFilter(exchange)) {
				return chain.filter(exchange);
			}
			ServerHttpRequest request = exchange.getRequest();
			String token = null;
			String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

			log.info("Getting authHeader:" + authHeader);

			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				token = authHeader.substring(7);
			}
			log.info("token:" + token);
			if (token == null && request.getCookies().getFirst("accessToken") != null) {
				token = request.getCookies().getFirst("accessToken").getValue();
				log.info("Token found in Cookie: " + token);
			}
			log.info("token:" + token);

			if (token == null) {
				log.info("No token found in Header or Cookie");
				return unauthorized(exchange);
			}

			final String finalToken = token;
			String jti = jwtService.extractId(finalToken);

			return redisTemplate.hasKey(jti).onErrorResume(ex -> {
				log.info("Redis unavailable. Skipping blacklist check." + ex);
				return Mono.just(false);
			}).flatMap(isBlacklisted -> {

				// 2. Agar blacklisted hai, toh yahi se bahar nikal jao
				if (isBlacklisted != null && isBlacklisted) {
					return unauthorized(exchange);
				}

				try {
					Claims claims = jwtService.validateAndGetClaims(finalToken);
					log.info("Claims found: " + claims);
					// Builder banake production headers set karo
					ServerHttpRequest.Builder builder = request.mutate();

					builder.header("X-Gateway-Auth", "verified");
					builder.header("X-User-Id", claims.getSubject());
					builder.header("X-User-Email", claims.get("email", String.class));
					builder.header("X-User-Full-Name", claims.get("fullName", String.class));
					builder.header("X-Token-Issuer", claims.getIssuer());
					// builder.header("X-Trace-Id", UUID.randomUUID().toString());

					// Roles handle karo
					List<String> roles = claims.get("roles", List.class);
					if (roles != null) {
						log.info("Gateway: X_roles-found: " + roles);
						builder.header("X-Roles", String.join(",", roles));
					}

					return chain.filter(exchange.mutate().request(builder.build()).build());

				} catch (Exception e) {
					return unauthorized(exchange);
				}

			});
		};
	}

	private Mono<Void> unauthorized(ServerWebExchange exchange) {
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		return exchange.getResponse().setComplete();
	}

	public static class Config {
		// Ise khaali chhod sakte hain
	}
}