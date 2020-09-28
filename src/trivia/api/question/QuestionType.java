package trivia.api.question;

public enum QuestionType {
	MULTIPLE, TRUEFALSE;
	
	public static QuestionType fromString(String type) {
		switch (type) {
			case "multiple":
				return QuestionType.MULTIPLE;
			case "boolean":
				return QuestionType.TRUEFALSE;
			default:
				return QuestionType.MULTIPLE;
		}
	}
}
