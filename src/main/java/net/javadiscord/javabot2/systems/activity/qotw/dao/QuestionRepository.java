package net.javadiscord.javabot2.systems.activity.qotw.dao;

import lombok.RequiredArgsConstructor;
import net.javadiscord.javabot2.systems.activity.qotw.model.Question;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@RequiredArgsConstructor
public class QuestionRepository {
	private final Connection con;

	public Question insert(Question question) throws SQLException {
		try (var s = con.prepareStatement("""
			INSERT INTO qotw_question(created_by, question, priority) VALUES (?, ?, ?);
		""", Statement.RETURN_GENERATED_KEYS)) {
			s.setLong(1, question.getCreatedBy());
			s.setString(2, question.getQuestion());
			s.setInt(3, question.getPriority());
			s.executeUpdate();
			var rs = s.getGeneratedKeys();
			if (!rs.next()) throw new SQLException("No generated keys returned.");
			long id = rs.getLong(1);
			return findById(id).orElseThrow();
		}
	}

	public Optional<Question> findById(long id) throws SQLException {
		try (var s = con.prepareStatement("SELECT * FROM qotw_question WHERE id = ?")) {
			s.setLong(1, id);
			var rs = s.executeQuery();
			if (rs.next()) return Optional.of(read(rs));
		}
		return Optional.empty();
	}

	private Question read(ResultSet rs) throws SQLException {
		Question q = new Question();
		q.setId(rs.getLong("id"));
		q.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		q.setCreatedBy(rs.getLong("created_by"));
		q.setQuestion(rs.getString("question"));
		q.setPriority(rs.getInt("priority"));
		q.setActive(rs.getBoolean("active"));
		q.setUsed(rs.getBoolean("used"));
		var ts = rs.getTimestamp("activated_at");
		q.setActivatedAt(ts == null ? null : ts.toLocalDateTime());
		return q;
	}
}
