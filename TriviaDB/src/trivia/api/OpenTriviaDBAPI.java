package trivia.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import trivia.api.question.TriviaCategory;
import trivia.api.question.TriviaQuestion;	


public class OpenTriviaDBAPI {
	protected static String dbURL = "https://opentdb.com/";
	protected static String token = "";
	protected static int timeout = 5000;
	protected static String encoding = "url3986";
	protected static List<TriviaQuestion> questions;

	public static List<TriviaQuestion> getQuestions() {
		return questions;
	}

	public static List<TriviaCategory> getCategories() {
		return TriviaCategory.getCategories();
	}

	public void clearCategories() {
	    TriviaCategory.clearCategories();
	}

	public static TriviaCategory getCategory(int id) {
		return TriviaCategory.getCategory(id);
	}

	public static TriviaCategory getCategory(String desc) {
		return TriviaCategory.getCategory(desc);
	}

	public static void fetchQuestions(int amount, String type, String difficulty, String category) throws IOException {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("amount", Integer.toString(amount));
		parameters.put("type", type);
		parameters.put("difficulty", difficulty);
		parameters.put("category", category);

		questions = makeAPIRequest(parameters);
	}
	
	public static void fetchQuestions(int amount, String type, String difficulty) throws IOException {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("amount", Integer.toString(amount));
		parameters.put("type", type);
		parameters.put("difficulty", difficulty);

		questions = makeAPIRequest(parameters);
	}

	public static void fetchQuestions(int amount, String type) throws IOException {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("amount", Integer.toString(amount));
		parameters.put("type", type);

		questions = makeAPIRequest(parameters);
	}

	public static List<TriviaQuestion> makeAPIRequest(Map<String, String> parameters) throws IOException, JSONException {
		parameters.put("encode", encoding);
		parameters.put("token", token);

		String requestURL = dbURL + "api.php?encode=" + encoding + "&amount=10";

		try {
			requestURL = dbURL + "api.php?" + ParameterStringBuilder.getParamsString(parameters);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpsURLConnection dbConnection = makeDefaultConnection(requestURL);

        JSONObject responseJSON = new JSONObject(readStreamContent(dbConnection));
        JSONArray questionArray = responseJSON.getJSONArray("results");
        
		List<TriviaQuestion> fetchedQuestions = new ArrayList<TriviaQuestion>();

        
        for (int i = 0; i < questionArray.length(); i++) {
			JSONObject questionJSON = questionArray.getJSONObject(i);
			JSONArray incorrectAnswers = questionJSON.getJSONArray("incorrect_answers");

			List<String> wrongAnsList = new ArrayList<String>();

			for (int j=0; j < incorrectAnswers.length(); j++) {
				wrongAnsList.add(incorrectAnswers.getString(j));
			}

			fetchedQuestions.add(new TriviaQuestion(questionJSON.getString("type"), questionJSON.getString("difficulty"), questionJSON.getString("category"),
					questionJSON.getString("question"), questionJSON.getString("correct_answer"), wrongAnsList));
		}
        
        
        return fetchedQuestions;
	}

	public static void fetchCategories() throws IOException, JSONException {
		String requestURL = dbURL + "api_category.php";
		HttpsURLConnection dbConnection = makeDefaultConnection(requestURL);
        JSONObject categoriesJSON = new JSONObject(readStreamContent(dbConnection));
        JSONArray categoryArray = categoriesJSON.getJSONArray("trivia_categories");
        
        for (int i = 0; i < categoryArray.length(); i++) {
			JSONObject categoryJSON = categoryArray.getJSONObject(i);
			new TriviaCategory(categoryJSON.getInt("id"), categoryJSON.getString("name"));
		}   
	}

	public static void newToken() throws IOException, JSONException {
		String requestURL = dbURL + "api_token.php?command=request";
		HttpsURLConnection dbConnection = makeDefaultConnection(requestURL);
        JSONObject tokenJSON = new JSONObject(readStreamContent(dbConnection));
        token = tokenJSON.getString("token");
        
	}

	public static void resetToken() throws IOException, JSONException {
		String requestURL = dbURL + "api_token.php?command=reset&token=" + token;
		HttpsURLConnection dbConnection = makeDefaultConnection(requestURL);
        dbConnection.getResponseCode();
	}
	
	protected static HttpsURLConnection makeDefaultConnection(String requestURL) throws IOException {
		HttpsURLConnection dbConnection = (HttpsURLConnection) new URL(requestURL).openConnection();
        dbConnection.setRequestMethod("GET");
        dbConnection.setConnectTimeout(timeout);
        dbConnection.setReadTimeout(timeout);
        
        return dbConnection;
	}
	
	protected static String readStreamContent(HttpsURLConnection conn) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
        	content.append(inputLine);
        }
        
        in.close();
        return content.toString();
	}
}
