package trivia.api.question;

public enum QuestionDifficulty {
	EASY, MEDIUM, HARD;

	public static QuestionDifficulty fromString(String difficulty) {
		switch (difficulty) {
			case "easy":
				return QuestionDifficulty.EASY;
			case "medium":
				return QuestionDifficulty.MEDIUM;
			case "hard":
				return QuestionDifficulty.HARD;
			default:
				return QuestionDifficulty.MEDIUM;
		}
	}
}
