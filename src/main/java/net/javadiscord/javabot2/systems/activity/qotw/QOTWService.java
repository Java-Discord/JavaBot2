package net.javadiscord.javabot2.systems.activity.qotw;

import net.javadiscord.javabot2.db.DbActions;
import net.javadiscord.javabot2.systems.activity.qotw.dao.QuestionRepository;
import net.javadiscord.javabot2.systems.activity.qotw.model.Question;
import org.javacord.api.entity.user.User;

import java.util.concurrent.CompletableFuture;

public class QOTWService {
	/**
	 *
	 * @param createdBy
	 * @param questionStr
	 * @param priority
	 */
	public CompletableFuture<Question> saveNewQuestion(User createdBy, String questionStr, int priority) {
		return DbActions.doAction(c -> {
			var dao = new QuestionRepository(c);
			return dao.insert(new Question(createdBy.getId(), questionStr, priority));
		});
	}
}
