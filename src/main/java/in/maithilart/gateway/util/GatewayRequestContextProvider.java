package in.maithilart.gateway.util;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import in.maithilart.common.context.provider.RequestContextProvider;

@Component
public class GatewayRequestContextProvider implements RequestContextProvider {

	private final ServerWebExchange exchange;

	public GatewayRequestContextProvider(  ObjectProvider<ServerWebExchange> provider ) {
		this.exchange = provider.getIfAvailable();
	}

	@SuppressWarnings("null")
	@Override
	public String getHeader(String name) {
		return exchange.getRequest().getHeaders().getFirst(name);
	}

	@Override
	public String getRequestUri() {
		return exchange.getRequest().getURI().getPath();
	}

	@Override
	public String getMethod() {
		return exchange.getRequest().getMethod().name();
	}

	@SuppressWarnings("null")
	@Override
	public String getClientIp() {

		String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");

		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}

		if (exchange.getRequest().getRemoteAddress() == null) {
			return "unknown";
		}

		return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
	}

	@Override
	public String getQueryString() {
		return exchange.getRequest().getURI().getQuery();
	}

	@Override
	public String getUserAgent() {
		return exchange.getRequest().getHeaders().getFirst("User-Agent");
	}

	@SuppressWarnings("null")
	@Override
	public String getContentType() {

		return exchange.getRequest().getHeaders().getContentType() == null ? null
				: exchange.getRequest().getHeaders().getContentType().toString();
	}

	@Override
	public String getRequestId() {
		return exchange.getRequest().getHeaders().getFirst("X-Request-Id");
	}
}