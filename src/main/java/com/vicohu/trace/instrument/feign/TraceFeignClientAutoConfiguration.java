package com.vicohu.trace.instrument.feign;

import com.vicohu.trace.TraceContentFactory;
import feign.Client;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author purgeyao
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ Client.class, FeignAutoConfiguration.class, TraceContentFactory.class })
@AutoConfigureBefore(FeignAutoConfiguration.class)
public class TraceFeignClientAutoConfiguration {

	@Bean
	public TraceFeignRequestInterceptor basicAuthRequestInterceptor(TraceContentFactory traceContentFactory) {
		return new TraceFeignRequestInterceptor(traceContentFactory);
	}

}
