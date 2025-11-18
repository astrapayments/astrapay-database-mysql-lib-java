package com.astrapay.entity;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AstraErrorMessage {

	private String message;
	private String error;
	private int httpStatus;
	private String timestamp;
	private Map<String, String> errors;
	private String stackTrace;

}
