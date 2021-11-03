package net.javadiscord.javabot2.config;

import lombok.Getter;

/**
 * Exception that's thrown when a property cannot be found in an object.
 */
@Getter
public class UnknownPropertyException extends Exception {
	/**
	 * The name of the property that's unknown.
	 */
	private final String propertyName;

	/**
	 * The parent class that was searched for the property.
	 */
	private final Object parentClass;

	/**
	 * Constructs a new exception.
	 * @param propertyName The property which couldn't be found.
	 * @param parentClass The class in which the property couldn't be found.
	 */
	public UnknownPropertyException(String propertyName, Class<?> parentClass) {
		super(String.format("No property named \"%s\" could be found for class %s.", propertyName, parentClass));
		this.propertyName = propertyName;
		this.parentClass = parentClass;
	}
}
