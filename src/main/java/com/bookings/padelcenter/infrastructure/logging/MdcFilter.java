package com.bookings.padelcenter.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class MdcFilter extends OncePerRequestFilter {

	private static final String TRACE_ID = "traceId";
	private static final String REQUEST_ID = "requestId";
	private static final String REQUEST_METHOD = "httpMethod";
	private static final String REQUEST_URI = "httpUri";

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		try {
			// traceId: usa X-Trace-Id del header si viene
			// del gateway, genera uno nuevo si no
			String traceId = Optional
				.ofNullable(request.getHeader("X-Trace-Id"))
				.orElse(UUID.randomUUID().toString());

			MDC.put(TRACE_ID, traceId);
			MDC.put(REQUEST_ID, UUID.randomUUID().toString());
			MDC.put(REQUEST_METHOD, request.getMethod());
			MDC.put(REQUEST_URI, request.getRequestURI());

			// Propaga el traceId en la respuesta
			response.setHeader("X-Trace-Id", traceId);

			filterChain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}
}
