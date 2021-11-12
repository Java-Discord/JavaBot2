package net.javadiscord.javabot2.systems.moderation.dao;

import lombok.RequiredArgsConstructor;
import net.javadiscord.javabot2.systems.moderation.model.Warn;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
public class WarnRepository {
	private final Connection con;

	public Warn insert(Warn warn) throws SQLException {
		try (var s = con.prepareStatement(
				"INSERT INTO warn (user_id, warned_by, severity, severity_weight, reason) VALUES (?, ?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS
		)) {
			s.setLong(1, warn.getUserId());
			s.setLong(2, warn.getWarnedBy());
			s.setString(3, warn.getSeverity());
			s.setInt(4, warn.getSeverityWeight());
			s.setString(5, warn.getReason());
			s.executeUpdate();
			var rs = s.getGeneratedKeys();
			if (!rs.next()) throw new SQLException("No generated keys returned.");
			long id = rs.getLong(1);
			return findById(id).orElseThrow();
		}
	}

	public Optional<Warn> findById(long id) throws SQLException {
		Warn warn = null;
		try (var s = con.prepareStatement("SELECT * FROM warn WHERE id = ?")) {
			s.setLong(1, id);
			var rs = s.executeQuery();
			if (rs.next()) {
				warn = read(rs);
			}
			rs.close();
		}
		return Optional.ofNullable(warn);
	}

	public int getTotalSeverityWeight(long userId, LocalDateTime cutoff) throws SQLException {
		try (var s = con.prepareStatement("SELECT SUM(severity_weight) FROM warn WHERE user_id = ? AND discarded = FALSE AND created_at > ?")) {
			s.setLong(1, userId);
			s.setTimestamp(2, Timestamp.valueOf(cutoff));
			var rs = s.executeQuery();
			int sum = 0;
			if (rs.next()) {
				sum = rs.getInt(1);
			}
			rs.close();
			return sum;
		}
	}

	private Warn read(ResultSet rs) throws SQLException {
		Warn warn = new Warn();
		warn.setId(rs.getLong("id"));
		warn.setUserId(rs.getLong("user_id"));
		warn.setWarnedBy(rs.getLong("warned_by"));
		warn.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		warn.setSeverity(rs.getString("severity"));
		warn.setSeverityWeight(rs.getInt("severity_weight"));
		warn.setReason(rs.getString("reason"));
		warn.setDiscarded(rs.getBoolean("discarded"));
		return warn;
	}
}
