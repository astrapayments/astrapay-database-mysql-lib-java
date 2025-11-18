package com.astrapay.logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.astrapay.entity.AstraErrorMessage;
import com.astrapay.entity.LogEntry;
import com.astrapay.entity.LogEntry.ErrorDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class AstraLogger {

	@Autowired
	private Environment environment;

	@Value("${spring.application.name}")
	private String appName;

	@Value("${spring.profiles.active}")
	private String profile;

	@Async
	public void info(LogEntry logEntry) {

		var logStream = appName + "-" + profile;

		logEntry.setAppName(appName);
		logEntry.setLogStream(logStream);
		logEntry.setTimestamp(formatTimeNow());

		logAws(logEntry);
	}

	@Async
	public void info(String message, Object... args) {
		var formateedMessage = String.format(message, args);
		logInfo(formateedMessage);
	}

	@Async
	public void info(String message) {

		logInfo(message);
	}

	public void logInfo(String message) {

		var logStream = appName + "-" + profile;

		LogEntry logEntry = new LogEntry();

		logEntry.setMessage(message);
		logEntry.setAppName(appName);

		logEntry.setLogStream(logStream);

		logEntry.setLevel("INFO");
		logEntry.setTimestamp(formatTimeNow());
		logAws(logEntry);

	}

	@Async
	public void error(AstraErrorMessage error) {

		var logStream = appName + "-" + profile;

		LogEntry logEntry = new LogEntry();

		logEntry.setLevel("ERROR");

		logEntry.setMessage(error.getMessage());
		logEntry.setAppName(appName);
		logEntry.setLogStream(logStream);
		logEntry.setTimestamp(formatTimeNow());
		logEntry.setHttpStatus(error.getHttpStatus());

		ErrorDetails oErrorDetails = new ErrorDetails();

		oErrorDetails.setCode(error.getHttpStatus());
		oErrorDetails.setMessage(error.getMessage());
		oErrorDetails.setStacktrace(error.getStackTrace());

		logEntry.setError(oErrorDetails);

		logAws(logEntry);

	}

	@Async
	public void error(String message) {

		var logStream = appName + "-" + profile;

		LogEntry logEntry = new LogEntry();

		logEntry.setLevel("ERROR");

		logEntry.setMessage(message);
		logEntry.setAppName(appName);
		logEntry.setLogStream(logStream);
		logEntry.setTimestamp(formatTimeNow());

		ErrorDetails oErrorDetails = new ErrorDetails();

		oErrorDetails.setMessage(message);

		logEntry.setError(oErrorDetails);

		logAws(logEntry);

	}

	@Async
	public void httpError(String message, HttpServerErrorException ex) {

		var logStream = appName + "-" + profile;

		LogEntry logEntry = new LogEntry();

		logEntry.setLevel("ERROR");

		logEntry.setMessage(message);
		logEntry.setAppName(appName);
		logEntry.setLogStream(logStream);
		logEntry.setTimestamp(formatTimeNow());

		ErrorDetails oErrorDetails = new ErrorDetails();

		oErrorDetails.setMessage(ex.getResponseBodyAsString());
		oErrorDetails.setStacktrace(ExceptionUtils.getStackTrace(ex));

		logEntry.setError(oErrorDetails);

		logAws(logEntry);

	}

	@Async
	public void httpError(String message, HttpClientErrorException ex) {

		var logStream = appName + "-" + profile;

		LogEntry logEntry = new LogEntry();

		logEntry.setLevel("ERROR");

		logEntry.setMessage(message);
		logEntry.setAppName(appName);
		logEntry.setLogStream(logStream);
		logEntry.setTimestamp(formatTimeNow());

		ErrorDetails oErrorDetails = new ErrorDetails();

		oErrorDetails.setMessage(ex.getResponseBodyAsString());
		oErrorDetails.setStacktrace(ExceptionUtils.getStackTrace(ex));

		logEntry.setError(oErrorDetails);

		logAws(logEntry);

	}

	@Async
	public void error(String message, Exception ex) {

		var logStream = appName + "-" + profile;

		LogEntry logEntry = new LogEntry();

		logEntry.setLevel("ERROR");

		logEntry.setMessage(message);
		logEntry.setAppName(appName);
		logEntry.setLogStream(logStream);
		logEntry.setTimestamp(formatTimeNow());

		ErrorDetails oErrorDetails = new ErrorDetails();

		oErrorDetails.setMessage(message);
		oErrorDetails.setStacktrace(ExceptionUtils.getStackTrace(ex));

		logEntry.setError(oErrorDetails);

		logAws(logEntry);

	}

	private String convertObjectToJson(Object obj, boolean format) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (format) {
				objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			}
			Map<String, Object> objectMap = objectMapper.convertValue(obj, Map.class);

			// Remove entradas com valores nulos
			objectMap.entrySet().removeIf(entry -> entry.getValue() == null);
			return objectMapper.writeValueAsString(objectMap);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String formatTimeNow() {

		String gmtMinus3 = "GMT-3";
		ZonedDateTime localDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(gmtMinus3));

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
		String formatedTime = localDateTime.format(formatter);

		return formatedTime;

	}

	private void logAws(LogEntry logEntry) {

		var format = profile.equals("lcl");
		var jsonMessage = convertObjectToJson(logEntry, format);

		System.out.println("Thread: " + Thread.currentThread().getName());
		System.out.println(jsonMessage);

	}
}
