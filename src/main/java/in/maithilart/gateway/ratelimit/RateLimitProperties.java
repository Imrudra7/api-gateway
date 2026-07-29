package in.maithilart.gateway.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RateLimitProperties {

	@Value("${maithil.rate-limit.max-requests}")
	private long max_requests;
	@Value("${maithil.rate-limit.window}")
	private long window;
	public long getMaxRequests() {
		return max_requests;
	}
	public long getWindow() {
		return window;
	}
	
	
	
}
