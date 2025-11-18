package com.astrapay.interceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.astrapay.entity.LogEntry;
import com.astrapay.entity.LogEntry.DataDetails;
import com.astrapay.entity.LogEntry.Headers;
import com.astrapay.logger.AstraLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@WebFilter(urlPatterns = "/v1/*")
@Order(-999)
@RequiredArgsConstructor
public class AstraLoggerFilter extends OncePerRequestFilter {

	@Value("${spring.application.name}")
	private String appName;

	@Value("${spring.profiles.active}")
	private String profile;

	private final AstraLogger astraLogger;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (request.getRequestURI().contains("/v1") ) {
			LogEntry logEntry = new LogEntry();

			ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
			ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);

			String contentType = resp.getContentType();
			if (contentType != null && contentType.contains("application/json")) {

					long startTime = System.currentTimeMillis();

					filterChain.doFilter(req, resp);

					long endTime = System.currentTimeMillis();

					long elapsedTime = endTime - startTime;

					byte[] requestBody = req.getContentAsByteArray();
					byte[] responseBody = resp.getContentAsByteArray();

					DataDetails oDataDetails = new DataDetails();

					var responseBodyString = new String(responseBody, StandardCharsets.UTF_8);
					if (responseBodyString != null && !responseBodyString.isBlank()) {

						ObjectMapper objectMapper = new ObjectMapper();

						JsonNode jsonNode = objectMapper.readTree(responseBodyString);
						oDataDetails.setResponse(jsonNode);

					}

					var requestBodyString = new String(requestBody, StandardCharsets.UTF_8);
					if (requestBodyString != null && !requestBodyString.isBlank()) {

						ObjectMapper objectMapper = new ObjectMapper();

						JsonNode jsonNode = objectMapper.readTree(requestBodyString);
						oDataDetails.setRequest(jsonNode);

					}

					logEntry.setData(oDataDetails);

					Headers oHeaders = new Headers();

					String XTransactionId = ((HttpServletRequest) request).getHeader("x-transaction-id");

					oHeaders.setXTransactionId(XTransactionId);

					logEntry.setHeaders(oHeaders);

					logEntry.setHttpMethod(req.getMethod());
					logEntry.setHttpStatus(resp.getStatus());
					logEntry.setLevel("INFO");
					logEntry.setPath(req.getRequestURI());
					if (resp.getBufferSize() > 0) {
						logEntry.setResponseLength(String.valueOf(resp.getBufferSize()));
					}
					logEntry.setResponseTimeMs(String.valueOf(elapsedTime));

					logEntry.setTimestamp(LocalDateTime.now().toString());
					logEntry.setXTransactionId(XTransactionId);
					astraLogger.info(logEntry);

					resp.copyBodyToResponse();
				}else{
					filterChain.doFilter(request, response);

				}

		} else {
			filterChain.doFilter(request, response);

		}
	}
}