package io.github.xandewe.pricewatch.store;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
class StoreConfiguration {

	@Bean
	Clock storeClock() {
		return Clock.systemUTC();
	}
}
