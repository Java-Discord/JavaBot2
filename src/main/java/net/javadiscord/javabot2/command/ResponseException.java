package net.javadiscord.javabot2.command;

import lombok.Getter;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.util.function.Supplier;

/**
 * An exception which can be thrown so that the bot can respond with a well
 * formatted error or warning response, while still allowing you to take
 * advantage of throwing an exception up the call stack.
 */
public class ResponseException extends Exception {
	@Getter
	private final InteractionImmediateResponseBuilder responseBuilder;

	public ResponseException(InteractionImmediateResponseBuilder responseBuilder) {
		this.responseBuilder = responseBuilder;
	}

	public static Supplier<ResponseException> warning(String message) {
		return () -> new ResponseException(Responses.deferredWarningBuilder().message(message).build());
	}

	public static Supplier<ResponseException> error(String message) {
		return () -> new ResponseException(Responses.deferredErrorBuilder().message(message).build());
	}
}
