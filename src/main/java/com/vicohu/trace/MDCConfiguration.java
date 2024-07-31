package com.vicohu.trace;

import org.slf4j.StaticMDCBinder;
import org.slf4j.spi.MDCAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

/**
 * MDC配置
 *
 * @author: VicoHu
 * @date: 2024/7/31
 */
@Configuration
public class MDCConfiguration {

    @Bean
    public StaticMDCBinder staticMDCBinder() {
        return StaticMDCBinder.getSingleton();
    }

    @Bean
    public MDCAdapter mdcAdapter() {
        return StaticMDCBinder.getSingleton().getMDCA();
    }
}
