package net.javadiscord.javabot2.systems.moderation.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Warn {
	private Long id;
	private long userId;
	private long warnedBy;
	private LocalDateTime createdAt;
	private String severity;
	private int severityWeight;
	private String reason;
	private boolean discarded;

	public Warn(long userId, long warnedBy, WarnSeverity severity, String reason) {
		this.userId = userId;
		this.warnedBy = warnedBy;
		this.severity = severity.name();
		this.severityWeight = severity.getWeight();
		this.reason = reason;
	}
}
