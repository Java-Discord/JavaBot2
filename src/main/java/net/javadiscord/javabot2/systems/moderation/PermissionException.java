package net.javadiscord.javabot2.systems.moderation;

/**
 * An exception that's thrown when a user doesn't have permission to do something.
 */
public class PermissionException extends Exception {
	/**
	 * Constructs a new exception with the specified detail message.  The
	 * cause is not initialized, and may subsequently be initialized by
	 * a call to {@link #initCause}.
	 *
	 * @param message the detail message. The detail message is saved for
	 *                later retrieval by the {@link #getMessage()} method.
	 */
	public PermissionException(String message) {
		super(message);
	}
}
