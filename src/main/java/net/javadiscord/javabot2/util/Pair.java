package net.javadiscord.javabot2.util;

/**
 * A simple tuple of two objects.
 * @param <A> The first object's type.
 * @param <B> The second object's type.
 */
public record Pair<A, B>(A first, B second) {}
