package net.javadiscord.javabot2.util;

/**
 * A simple tuple of two objects.
 * @param first The first object.
 * @param second The second object.
 * @param <A> The first object's type.
 * @param <B> The second object's type.
 */
public record Pair<A, B>(A first, B second) {}
