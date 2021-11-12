package net.javadiscord.javabot2.systems.moderation.dao;

import lombok.RequiredArgsConstructor;
import net.javadiscord.javabot2.systems.moderation.model.Mute;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MuteRepository {
	private final Connection con;

	public Mute insert(Mute mute) throws SQLException {
		try (var s = con.prepareStatement(
				"INSERT INTO mute (user_id, muted_by, reason, ends_at) VALUES (?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS
		)) {
			s.setLong(1, mute.getUserId());
			s.setLong(2, mute.getMutedBy());
			s.setString(3, mute.getReason());
			s.setTimestamp(4, Timestamp.valueOf(mute.getEndsAt()));
			s.executeUpdate();
			var rs = s.getGeneratedKeys();
			if (!rs.next()) throw new SQLException("No generated keys returned.");
			long id = rs.getLong(1);
			return findById(id).orElseThrow();
		}
	}

	public List<Mute> getActiveMutes(long userId) throws SQLException {
		try (var s = con.prepareStatement("SELECT * FROM mute WHERE user_id = ? AND discarded = FALSE AND ends_at > CURRENT_TIMESTAMP(0)")) {
			s.setLong(1, userId);
			var rs = s.executeQuery();
			List<Mute> mutes = new ArrayList<>();
			while (rs.next()) {
				mutes.add(read(rs));
			}
			return mutes;
		}
	}

	public List<Mute> getActiveMutes() throws SQLException {
		try (var s = con.prepareStatement("SELECT * FROM mute WHERE discarded = FALSE AND ends_at > CURRENT_TIMESTAMP(0)")) {
			var rs = s.executeQuery();
			List<Mute> mutes = new ArrayList<>();
			while (rs.next()) {
				mutes.add(read(rs));
			}
			return mutes;
		}
	}

	public Optional<Mute> findById(long id) throws SQLException {
		try (var s = con.prepareStatement("SELECT * FROM mute WHERE id = ?")) {
			s.setLong(1, id);
			var rs = s.executeQuery();
			if (rs.next()) return Optional.of(read(rs));
		}
		return Optional.empty();
	}

	public void discard(Mute mute) throws SQLException {
		try (var s = con.prepareStatement("UPDATE mute SET discarded = TRUE WHERE id = ?")) {
			s.setLong(1, mute.getId());
			s.executeUpdate();
		}
	}

	public void discardAllActive(long userId) throws SQLException {
		try (var s = con.prepareStatement("UPDATE mute SET discarded = TRUE WHERE user_id = ? AND discarded = FALSE AND ends_at > CURRENT_TIMESTAMP(0)")) {
			s.setLong(1, userId);
			s.executeUpdate();
		}
	}

	private Mute read(ResultSet rs) throws SQLException {
		Mute mute = new Mute();
		mute.setId(rs.getLong("id"));
		mute.setUserId(rs.getLong("user_id"));
		mute.setMutedBy(rs.getLong("muted_by"));
		mute.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		mute.setReason(rs.getString("reason"));
		mute.setEndsAt(rs.getTimestamp("ends_at").toLocalDateTime());
		mute.setDiscarded(rs.getBoolean("discarded"));
		return mute;
	}
}
