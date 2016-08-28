package top.knos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import top.knos.support.cache.RedisCacheManager;
import top.knos.KnosProfiles;
import top.knos.cache.CachedRestClient;

import java.util.Arrays;
import java.util.stream.Collectors;


@Configuration
@EnableCaching(proxyTargetClass = true)
@Profile(KnosProfiles.STANDALONE)
class StandaloneCacheConfig {

    @Bean
    public CacheManager simpleCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(
                Arrays.asList(CachedRestClient.CACHE_NAME).stream()
                        .map(name -> new ConcurrentMapCache(name))
                        .collect(Collectors.toList()));
        return cacheManager;
    }

}

@Configuration
@EnableCaching(proxyTargetClass = true)
@Profile(KnosProfiles.CLOUDFOUNDRY)
class CloudFoundryCacheConfig extends AbstractCloudConfig {

    @Value(CachedRestClient.CACHE_TTL)
    protected Long cacheNetworkTimeToLive;



    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory,
                                          ObjectMapper objectMapper) {

        RedisCacheManager cacheManager = new RedisCacheManager(redisConnectionFactory);

        // Use the default redisTemplate for caching REST calls
        cacheManager.withCache(CachedRestClient.CACHE_NAME, this.cacheNetworkTimeToLive);

        // Use

        return cacheManager;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return connectionFactory().redisConnectionFactory();
    }

}
