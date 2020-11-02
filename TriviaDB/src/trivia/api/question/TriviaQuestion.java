package trivia.api.question;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

public class TriviaQuestion {
	protected TriviaCategory category;
	protected QuestionType type;
	protected QuestionDifficulty difficulty;
	protected String question;
	protected List<String> wrong_answers;
	protected String correct_answer;
	
	public TriviaQuestion(String type, String difficulty, String category, String question, String correct_answer, List<String> wrong_answers) {
		this.type = QuestionType.fromString(type);
		this.difficulty = QuestionDifficulty.fromString(difficulty);
		this.correct_answer = correct_answer;
		this.question = question;
		
		try {
			this.category = TriviaCategory.getCategory(URLDecoder.decode(category, StandardCharsets.UTF_8.toString()));
            this.question = URLDecoder.decode(question, StandardCharsets.UTF_8.toString());
            this.correct_answer = URLDecoder.decode(correct_answer, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }

		List<String> decodeWrongAnswers = new ArrayList<String>();

		for (int i = 0; i < wrong_answers.size(); i++) {
			String new_wrong_choice = "";
			try {
            	new_wrong_choice = URLDecoder.decode(wrong_answers.get(i), StandardCharsets.UTF_8.toString());
	        } catch (UnsupportedEncodingException ex) {
	            throw new RuntimeException(ex.getCause());
	        }
			decodeWrongAnswers.add(new_wrong_choice);
		}

		this.wrong_answers = decodeWrongAnswers;
	}

	public TriviaCategory getCategory() {
		return category;
	}

	public void setCategory(TriviaCategory category) {
		this.category = category;
	}

	public QuestionType getType() {
		return type;
	}

	public QuestionDifficulty getDifficulty() {
		return difficulty;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public List<String> getWrong_answers() {
		return wrong_answers;
	}

	public void setWrong_answers(List<String> wrong_answers) {
		this.wrong_answers = wrong_answers;
	}

	public String getCorrect_answer() {
		return correct_answer;
	}

	public void setCorrect_answer(String correct_answer) {
		this.correct_answer = correct_answer;
	}
	
	public String toString() {
		return category + ": " + question;
	}
}
