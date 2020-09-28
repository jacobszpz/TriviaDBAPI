package trivia.api;
import java.io.IOException;

import org.json.JSONObject;

public class Trivia {
	JSONObject questions;

	public static void main(String[] args) {

		try {
			OpenTriviaDBAPI.newToken();
			OpenTriviaDBAPI.fetchCategories();
			OpenTriviaDBAPI.fetchQuestions(10, "multiple");
			System.out.println(OpenTriviaDBAPI.getQuestions());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	
}
