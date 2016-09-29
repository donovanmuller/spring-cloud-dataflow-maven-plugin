package io.switchbit;

public class DeploymentFailedException extends RuntimeException {

	public DeploymentFailedException(String message) {
		super(message);
	}

	public DeploymentFailedException(String message, Object... args) {
		super(String.format(message, args));
	}
}
