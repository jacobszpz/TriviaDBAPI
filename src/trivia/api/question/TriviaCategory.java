package trivia.api.question;

import java.util.ArrayList;
import java.util.List;

public class TriviaCategory {
	protected int identifier;
	protected String description;
	protected static List<TriviaCategory> categories = new ArrayList<TriviaCategory>();
	
	public TriviaCategory(int id, String desc) {
		this.identifier = id;
		this.description = desc;
		
		addCategory(this);
	}

	public int getIdentifier() {
		return identifier;
	}

	public String getDescription() {
		return description;
	}
	
	public static TriviaCategory getCategory(int id) {
		for (TriviaCategory triviaCategory : categories) {
			if (triviaCategory.getIdentifier() == id) {
				return triviaCategory;
			}
		}

		return null;
	}
	
	public static TriviaCategory getCategory(String desc) {
		for (TriviaCategory triviaCategory : categories) {

			if (triviaCategory.getDescription().equals(desc)) {
				return triviaCategory;
			}
		}
		
		return null;
	}
	
	public static void addCategory(TriviaCategory newCategory) {
		categories.add(newCategory);
	}

	public static void clearCategories() {
		categories.clear();
	}
	

	public static List<TriviaCategory> getCategories() {
		return categories;
	}
	
	public String toString() {
		return identifier + ": " + description;
	}
}
