package io.switchbit;

public class FeatureNotAvailableException extends RuntimeException {

	public FeatureNotAvailableException(String message, Object... args) {
		super(String.format(message, args));
	}
}
