package net.javadiscord.javabot2.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Functional interface for defining operations that use a Connection.
 */
@FunctionalInterface
public interface ConnectionConsumer {
	void consume(Connection con) throws SQLException;
}
