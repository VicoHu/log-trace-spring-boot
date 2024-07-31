package com.vicohu.trace.instrument.gateway;

import com.vicohu.trace.Constants;
import com.vicohu.trace.TraceContentFactory;
import com.vicohu.trace.TraceLogProperties;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 响应 TraceId 处理器
 *
 * @author L.cm
 */
public class TraceGatewayFilter implements GlobalFilter, Ordered {
	private final TraceLogProperties traceLogProperties;

    public TraceGatewayFilter(TraceLogProperties traceLogProperties) {
        this.traceLogProperties = traceLogProperties;
    }


    @Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// 1. 传递 新的header
		Map<String, String> copyOfContextMap = TraceContentFactory.assemblyTraceContentStatic();
		ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
		builder.headers(httpHeaders -> {
			for (Map.Entry<String, String> copyOfContext : copyOfContextMap.entrySet()) {
				httpHeaders.set(copyOfContext.getKey(), copyOfContext.getValue());
			}
		});
		ServerHttpResponse response = exchange.getResponse();
		// 2. 获取 traceId
		String traceId = MDC.get(Constants.LEGACY_TRACE_ID_NAME);
		// 3. 处理响应的 header traceId
		HttpHeaders responseHeaders = response.getHeaders();
		// 判断是否需要响应 traceId，如果需要且 traceId 不为空则在响应头中设置
		if (traceLogProperties.isShowResponseHeaderTraceId() && traceId != null) {
			responseHeaders.set(Constants.LEGACY_TRACE_ID_NAME, traceId);
		}
		return chain.filter(exchange.mutate().request(builder.build()).build());
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}

}
