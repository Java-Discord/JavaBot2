package net.javadiscord.javabot2.db;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionFunction<T> {
	T apply(Connection c) throws SQLException;
}
