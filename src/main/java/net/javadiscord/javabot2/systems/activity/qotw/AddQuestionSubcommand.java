package net.javadiscord.javabot2.systems.activity.qotw;

import net.javadiscord.javabot2.command.ResponseException;
import net.javadiscord.javabot2.command.Responses;
import net.javadiscord.javabot2.command.SlashCommandHandler;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

public class AddQuestionSubcommand implements SlashCommandHandler {
	@Override
	public InteractionImmediateResponseBuilder handle(SlashCommandInteraction interaction) throws ResponseException {
		String question = interaction.getOptionStringValueByName("question")
				.orElseThrow(ResponseException.warning("Missing required question."));
		int priority = (int) interaction.getOptionLongValueByName("priority")
				.orElse(0L).longValue();
		var service = new QOTWService();
		service.saveNewQuestion(interaction.getUser(), question, priority)
				.thenAcceptAsync(q -> {
					interaction.getChannel().orElseThrow().sendMessage("Question **" + q.getId() + "** has been added to the queue.");
				})
				.exceptionallyAsync(throwable -> {
					interaction.getChannel().orElseThrow().sendMessage("An error occurred and the question could not be added to the queue.");
					return null;
				});
		return Responses.success(interaction, "Question Added", "Your question has been added to the QOTW queue.");
	}
}
