package net.javadiscord.javabot2.systems.activity.qotw;

import net.javadiscord.javabot2.command.DelegatingCommandHandler;

public class QOTWCommand extends DelegatingCommandHandler {
	public QOTWCommand() {
		addSubcommand("add-question", new AddQuestionSubcommand());
		addSubcommand("remove-question", new RemoveQuestionSubcommand());
		addSubcommand("list", new ListQuestionsSubcommand());
	}
}
