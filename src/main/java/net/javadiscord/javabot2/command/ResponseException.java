package net.javadiscord.javabot2.command;

import lombok.Getter;

import java.util.function.Supplier;

/**
 * An exception which can be thrown so that the bot can respond with a well
 * formatted error or warning response, while still allowing you to take
 * advantage of throwing an exception up the call stack.
 */
public class ResponseException extends Exception {
	/**
	 * The response builder that's used to respond to an interaction in the case
	 * of an exception.
	 */
	@Getter
	private final Responses.ResponseBuilder responseBuilder;

	/**
	 * Constructs the exception.
	 * @param responseBuilder The response builder to use.
	 */
	public ResponseException(Responses.ResponseBuilder responseBuilder) {
		super(responseBuilder.getMessage());
		this.responseBuilder = responseBuilder;
	}

	/**
	 * Gets a supplier that supplies a response exception with a warning
	 * response. This is especially useful with {@link java.util.Optional#orElseThrow(Supplier)}.
	 * @param message The warning message.
	 * @return The exception supplier.
	 */
	public static Supplier<ResponseException> warning(String message) {
		return () -> new ResponseException(Responses.deferredWarningBuilder().message(message));
	}

	/**
	 * Gets a supplier that supplies a response exception with an error
	 * response. This is especially useful with {@link java.util.Optional#orElseThrow(Supplier)}.
	 * @param message The error message.
	 * @return The exception supplier.
	 */
	public static Supplier<ResponseException> error(String message) {
		return () -> new ResponseException(Responses.deferredErrorBuilder().message(message));
	}
}
