	package in.maithilart.gateway.ratelimit;
	
	import org.springframework.stereotype.Component;
	import org.springframework.web.server.ServerWebExchange;
	
	@Component
	public class RateLimitKeyGenerator {
	
		public String generate(ServerWebExchange exchange) {
	
			String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
	
			if (userId != null) {
				return "rate:user:" + userId;
			}
	
			return "rate:ip:" + getClientIp(exchange);
		}
	
		@SuppressWarnings("null")
		private String getClientIp(ServerWebExchange exchange) {
	
			String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
	
			if (forwarded != null && !forwarded.isBlank()) {
				return forwarded.split(",")[0].trim();
			}
	
			if (exchange.getRequest().getRemoteAddress() == null) {
				return "unknown";
			}
	
			return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
		}
	}