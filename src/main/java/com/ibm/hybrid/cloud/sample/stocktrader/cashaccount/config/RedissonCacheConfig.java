/*
       Copyright 2023 Kyndryl, All Rights Reserved
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ibm.hybrid.cloud.sample.stocktrader.cashaccount.config;

import java.util.HashMap;
import java.util.Map;

import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class RedissonCacheConfig {
    @Autowired
    private RedissonClient redissonClient;

    @Bean
    public CacheManager cacheManager() {
        Map<String, CacheConfig> configMap = new HashMap<>();
        CacheConfig config = new CacheConfig();
        // TTL counts in milliseconds, this converts to a day
        config.setTTL(1000 * 60 * 60 * 24);
        configMap.put("currencyExchangeCache", config);

        RedissonSpringCacheManager cacheManager = new RedissonSpringCacheManager(redissonClient, configMap);
        return cacheManager;
    }
}
