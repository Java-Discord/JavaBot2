package net.javadiscord.javabot2.systems.moderation.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

	public Mute(long userId, long mutedBy, String reason, LocalDateTime endsAt) {
		this.userId = userId;
		this.mutedBy = mutedBy;
		this.reason = reason;
		this.endsAt = endsAt;
	}
}
