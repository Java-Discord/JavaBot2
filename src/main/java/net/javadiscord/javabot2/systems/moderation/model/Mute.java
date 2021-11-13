package net.javadiscord.javabot2.systems.moderation.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a single instance of someone being muted.
 */
@Data
@NoArgsConstructor
public class Mute {
	private Long id;
	private long userId;
	private long mutedBy;
	private LocalDateTime createdAt;
	private String reason;
	private LocalDateTime endsAt;
	private boolean discarded;

	/**
	 * Constructs a new mute.
	 * @param userId The id of the user being muted.
	 * @param mutedBy The id of the user who's muting them.
	 * @param reason The reason for the mute.
	 * @param endsAt The date and time at which the mute ends.
	 */
	public Mute(long userId, long mutedBy, String reason, LocalDateTime endsAt) {
		this.userId = userId;
		this.mutedBy = mutedBy;
		this.reason = reason;
		this.endsAt = endsAt;
	}
}
