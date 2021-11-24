package net.javadiscord.javabot2.systems.activity.qotw.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Question {
	private Long id;
	private long createdBy;
	private LocalDateTime createdAt;
	private String question;
	private int priority;
	private boolean active;
	private LocalDateTime activatedAt;
	private boolean used;

	public Question(long createdBy, String question, int priority) {
		this.createdBy = createdBy;
		this.question = question;
		this.priority = priority;
	}
}
