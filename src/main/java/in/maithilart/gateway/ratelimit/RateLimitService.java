package in.maithilart.gateway.ratelimit;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;

import org.slf4j.Logger;

import in.maithilart.common.cache.MaithilCacheManager;

@Service
public class RateLimitService {
	private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

	private final MaithilCacheManager cacheManager;

	private final RateLimitProperties properties;

	public RateLimitService(RateLimitProperties properties, MaithilCacheManager cacheManager) {
		this.cacheManager = cacheManager;
		this.properties = properties;

	}

	public boolean isAllowed(String key) {
		long count = cacheManager.increment(key);
		log.info("Key   -   count");
		log.info(key + "   -   " + count);
		if (count == 1) {
			cacheManager.expire(key, Duration.ofMinutes(properties.getWindow()));
		}

		return count <= properties.getMaxRequests();

	}
}
