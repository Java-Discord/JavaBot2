package net.javadiscord.javabot2.systems.moderation.dao;

import lombok.RequiredArgsConstructor;
import net.javadiscord.javabot2.systems.moderation.model.Mute;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for interacting with the collection of stored {@link Mute} objects.
 */
@RequiredArgsConstructor
public class MuteRepository {
	private final Connection con;

	/**
	 * Inserts a new mute into the database. Note that this ignores the mute's
	 * {@link Mute#isDiscarded()}, {@link Mute#getId()}, and {@link Mute#getCreatedAt()}.
	 * @param mute The mute to save.
	 * @return The mute that was saved.
	 * @throws SQLException If an error occurs.
	 */
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

	/**
	 * Gets the list of active mutes for a user, or those which are not
	 * discarded, and whose ending date is some time in the future.
	 * @param userId The id of the user to get active mutes for.
	 * @return A list of mutes.
	 * @throws SQLException If an error occurs.
	 */
	public List<Mute> getActiveMutes(long userId) throws SQLException {
		try (var s = con.prepareStatement("""
			SELECT * FROM mute
			WHERE user_id = ? AND discarded = FALSE AND ends_at > CURRENT_TIMESTAMP(0)""")) {
			s.setLong(1, userId);
			var rs = s.executeQuery();
			List<Mute> mutes = new ArrayList<>();
			while (rs.next()) {
				mutes.add(read(rs));
			}
			return mutes;
		}
	}

	/**
	 * Determines if a user has at least one active mute.
	 * @param userId The user to check.
	 * @return True if there is at least one active mute for the user.
	 * @throws SQLException If an error occurs.
	 */
	public boolean hasActiveMutes(long userId) throws SQLException {
		try (var s = con.prepareStatement("""
			SELECT COUNT(id)
			FROM mute
			WHERE user_id = ? AND discarded = FALSE AND ends_at > CURRENT_TIMESTAMP(0)""")) {
			s.setLong(1, userId);
			var rs = s.executeQuery();
			return rs.next() && rs.getLong(1) > 0;
		}
	}

	/**
	 * Gets a list of expired mutes, which are those that are not yet discarded,
	 * but whose ending time is in the past.
	 * @return The list of mutes.
	 * @throws SQLException If an error occurs.
	 */
	public List<Mute> getExpiredMutes() throws SQLException {
		try (var s = con.prepareStatement("""
			SELECT * FROM mute
			WHERE discarded = FALSE AND ends_at < CURRENT_TIMESTAMP(0)""")) {
			var rs = s.executeQuery();
			List<Mute> mutes = new ArrayList<>();
			while (rs.next()) {
				mutes.add(read(rs));
			}
			return mutes;
		}
	}

	/**
	 * Finds a mute by its id.
	 * @param id The id of the mute to fetch.
	 * @return An optional that contains the mute, if it was found.
	 * @throws SQLException If an error occurs.
	 */
	public Optional<Mute> findById(long id) throws SQLException {
		try (var s = con.prepareStatement("SELECT * FROM mute WHERE id = ?")) {
			s.setLong(1, id);
			var rs = s.executeQuery();
			if (rs.next()) return Optional.of(read(rs));
		}
		return Optional.empty();
	}

	/**
	 * Discards a mute.
	 * @param mute The mute to discard.
	 * @throws SQLException If an error occurs.
	 */
	public void discard(Mute mute) throws SQLException {
		try (var s = con.prepareStatement("UPDATE mute SET discarded = TRUE WHERE id = ?")) {
			s.setLong(1, mute.getId());
			s.executeUpdate();
		}
	}

	/**
	 * Discards all currently active mutes for a given user.
	 * @param userId The id of the user whose active mutes to discard.
	 * @throws SQLException If an error occurs.
	 */
	public void discardAllActive(long userId) throws SQLException {
		try (var s = con.prepareStatement("""
			UPDATE mute
			SET discarded = TRUE
			WHERE user_id = ? AND discarded = FALSE AND ends_at > CURRENT_TIMESTAMP(0)""")) {
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
