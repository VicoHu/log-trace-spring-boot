package com.pig4cloud.trace;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * 链路追踪配置
 *
 * @author <a href="mailto:yaoonlyi@gmail.com">purgeyao</a>
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.lite-trace.log")
public class TraceLogProperties implements InitializingBean {

	/**
	 * 是否显示请求头中的traceId，默认为true
	 */
	private boolean showResponseHeaderTraceId = true;

	/**
	 * 日志格式顺序
	 */
	private Set<String> format = new HashSet<>();

	public Set<String> getFormat() {
		return format;
	}

	public void setFormat(Set<String> format) {
		this.format = format;
	}

	/**
	 * 是否显示请求头中的traceId
	 *
	 * @return 是否显示请求头中的traceId
	 */
	public boolean isShowResponseHeaderTraceId() {
		return this.showResponseHeaderTraceId;
	}

	/**
	 * X-B3-TraceId,X-B3-ParentName
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (0 == format.size()) {
			format.add(Constants.LEGACY_TRACE_ID_NAME);
			format.add(Constants.LEGACY_PARENT_SERVICE_NAME);
		}
	}

}
