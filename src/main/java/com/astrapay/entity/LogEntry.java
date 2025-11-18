package com.astrapay.entity;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class LogEntry {
	private String level;
	private String timestamp;

	private String message;

	private Headers headers;

	private String logStream;
	private String appName;
	private String userId;
	private String xTransactionId;
	private String responseTimeMs;

	private String responseLength;
	private String path;
	private int httpStatus;
	private String httpMethod;
	private ErrorDetails error;
	private DataDetails data;

	@Data
	public static class Headers {
		private String xTransactionId;
		private String jwt;
	}

	@Data
	public static class ErrorDetails {
		private int code;
		private String message;
		private String stacktrace;
	}

	@Data
	public static class DataDetails {
		private JsonNode request;
		private JsonNode response;

	}
}