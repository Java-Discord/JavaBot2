package net.javadiscord.javabot2.db;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionConsumer {
	void consume(Connection con) throws SQLException;
}
