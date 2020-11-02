// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.sanchez.trivia;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.EventDispatcher;

import android.Manifest;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;

import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;

import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.util.ErrorMessages;


import android.util.Log;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONException;
import trivia.api.OpenTriviaDBAPI;
import trivia.api.question.TriviaCategory;
import trivia.api.question.TriviaQuestion;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;

/**
* Trivia, an AppInventor component and library to
* download trivia questions and their respective answers.
* AppInventor can suck it.
*
* © Jacob Sanchez, 2020
*/

@DesignerComponent(version = 1,
description = "API for fetching random trivia questions and answers",
category = ComponentCategory.EXTENSION,
nonVisible = true,
iconName = "images/extension.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "json.jar, json-regular.jar, trivia-db.jar")
public class Trivia extends AndroidNonvisibleComponent implements Component {
  private final Activity activity;
  private final ComponentContainer container;

  protected boolean anyType = false;
  protected boolean multipleChoice = true;
  protected int questionCount = 10;

  public Trivia(ComponentContainer container) {
    super(container.$form());
    activity = container.$context();
    this.container = container;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public YailList Categories() {
    List<String> descriptionList = new ArrayList<String>();
    List<TriviaCategory> categoryList = OpenTriviaDBAPI.getCategories();

    for (int i = 0; i < categoryList.size(); i++) {
      descriptionList.add(categoryList.get(i).getDescription());
    }

    return YailList.makeList(descriptionList);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
  description = "Amount of questions to fetch every time.")
  public int QuestionCount() {
    return questionCount;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
  defaultValue = "10")
  @SimpleProperty
  public void QuestionCount(int count) {
    this.questionCount = count;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
  description = "If questions can be of any type. If not, multipleChoice is used as switch.")
  public boolean AnyType() {
    return anyType;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
  defaultValue = "false")
  @SimpleProperty
  public void AnyType(boolean any) {
    this.anyType = any;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
  description = "If anyType is disabled, this is used to know what type of questions to pull.")
  public boolean MultipleChoice() {
    return multipleChoice;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
  defaultValue = "true")
  @SimpleProperty
  public void MultipleChoice(boolean multiple) {
    this.multipleChoice = multiple;
  }

  protected String getTypeStr() {
    String type = "";

    if (!anyType) {
      if (multipleChoice) {
        type = "multiple";
      } else {
        type = "boolean";
      }
    }

    return type;
  }

  protected String getDifficultyStr(String spinner) {
    String difficulty = "";

    if (spinner.equalsIgnoreCase("Easy")) {
      difficulty = "easy";
    } else if (spinner.equalsIgnoreCase("Medium")){
      difficulty = "medium";
    } else {
      difficulty = "hard";
    }

    return difficulty;
  }

  protected String getCategoryStr(String category) {
    TriviaCategory gotCategory = OpenTriviaDBAPI.getCategory(category);

    if (gotCategory != null) {
      return Integer.toString(gotCategory.getIdentifier());
    }

    return "";
  }

  public TriviaQuestion getQuestionObjByIndex(int index) {
    return OpenTriviaDBAPI.getQuestions().get(index);
  }

  @SimpleFunction(description = "Get question from question obj at a particular index.")
  public String GetQuestionAtIndex(int index) {
    return getQuestionObjByIndex(index).getQuestion();
  }

  @SimpleFunction(description = "Get correct answer text for question at index.")
  public String GetCorrectAnswerAtIndex(int index) {
    return getQuestionObjByIndex(index).getCorrect_answer();
  }

  @SimpleFunction(description = "Get incorrect answer list for question at index.")
  public YailList GetIncorrectAnswersAtIndex(int index) {
    return YailList.makeList(getQuestionObjByIndex(index).getWrong_answers());
  }

  @SimpleFunction(description = "Asks the API to fetch questions. You must run FetchCategories before doing this.")
  public void FetchQuestions(final String difficulty, final String category) {
    final String type = getTypeStr();
    final String difficultyStr = getDifficultyStr(difficulty);
    final String categoryStr = getCategoryStr(category);


    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        fetchQuestionsRun(questionCount, type, difficultyStr, categoryStr);
      }
    });
  }

  public void fetchQuestionsRun(final int amount, final String type,
  final String difficulty, final String category) {
    try {
      this.questions = OpenTriviaDBAPI.fetchQuestions(amount, type, difficulty, category);
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          GotQuestions();
        }
      });
    } catch (IOException e) {
      form.dispatchErrorOccurredEvent(Trivia.this, "FetchCategories",
      ErrorMessages.ERROR_NETWORK);
    } catch (JSONException e) {
      form.dispatchErrorOccurredEvent(Trivia.this, "FetchCategories",
      ErrorMessages.ERROR_WEB_JSON_TEXT_DECODE_FAILED);
    }
  }

  @SimpleEvent(description = "Triggered when categories have loaded.")
  public void GotCategories() {
    EventDispatcher.dispatchEvent(this, "GotCategories");
  }

  @SimpleEvent(description = "Triggered when questions have been retrieved.")
  public void GotQuestions() {
    EventDispatcher.dispatchEvent(this, "GotQuestions");
  }

  @SimpleFunction(description = "Asks the API to fetch categories. Must be done before fetching questions.")
  public void FetchCategories() {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        fetchCategoriesRun();
      }
    });
  }

  public void fetchCategoriesRun() {
    try {
      OpenTriviaDBAPI.fetchCategories();
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          GotCategories();
        }
      });
    } catch (IOException e) {
      form.dispatchErrorOccurredEvent(Trivia.this, "FetchCategories",
      ErrorMessages.ERROR_NETWORK);
    } catch (JSONException e) {
      form.dispatchErrorOccurredEvent(Trivia.this, "FetchCategories",
      ErrorMessages.ERROR_WEB_JSON_TEXT_DECODE_FAILED);
    }
  }

  @SimpleFunction(description = "Asks the API to clear categories.")
  public void ClearCategories() {
    OpenTriviaDBAPI.clearCategories();
  }


  @SimpleFunction(description = "Asks for a new token which guarantees" +
  " unique questions for the duration of the session.")
  public void NewToken() {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        try {
          OpenTriviaDBAPI.newToken();
        } catch (IOException e) {
          form.dispatchErrorOccurredEvent(Trivia.this, "NewToken",
          ErrorMessages.ERROR_NETWORK);
        } catch (JSONException e) {
          form.dispatchErrorOccurredEvent(Trivia.this, "NewToken",
          ErrorMessages.ERROR_WEB_JSON_TEXT_DECODE_FAILED);
        }
      }
    });
  }
}
