// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.sanchez.fourwcapi;
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
import hideandseek.HideAndSeek;
import hideandseek.objects.HSMap;
import hideandseek.objects.Player;
import hideandseek.objects.Ticket;
import hideandseek.objects.PositionInfo;
import hideandseek.objects.Status;
import hideandseek.objects.InGamePlayer;
import hideandseek.objects.StateOfGame;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;

/**
* Component wrapper of the HideAndSeek library, an
* interface to connect with the 4 Week Challenge API,
* a UCLAN initiative
*
* © Jacob Sanchez, 2020
*/

@DesignerComponent(version = 1,
description = "Component to interface with the 4 Week Challenge API",
category = ComponentCategory.EXTENSION,
nonVisible = true,
iconName = "images/extension.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "hideandseek.jar")
public class FourWCAPI extends AndroidNonvisibleComponent implements Component {
  private final Activity activity;
  private final ComponentContainer container;

  protected String playerName;

  public FourWCAPI(ComponentContainer container) {
    super(container.$form());
    activity = container.$context();
    this.container = container;
  }

  public static final int ERROR_NULL_POINTER = 0;
  public static final int ERROR_INDEX_OUT_OF_RANGE = 6;
  public static final int ERROR_GENERAL = 1;

  public static String getMsg(final int code) {
    switch (code) {
        case ERROR_NULL_POINTER:
            return "Object is null";
        case ERROR_GENERAL:
            return "General exception";
        case ERROR_INDEX_OUT_OF_RANGE:
            return "Index out of range error";
        default:
            return "General exception";
    }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
  description = "Name to use when creating/joining games.")
  public String PlayerName() {
    return playerName;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
  defaultValue = "Player")
  @SimpleProperty
  public void PlayerName(String name) {
    this.playerName = name;
  }

  @SimpleFunction(description = "Asks the web service to fetch the game maps.")
  public void FetchMaps() {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        fetchMapsRunnable();
      }
    });
  }

  public void fetchMapsRunnable() {
    final String fName = "FetchMaps";
    try {
      final Status opStatus = HideAndSeek.fetchMaps();

      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          GotMaps(Status.toString(opStatus), opStatus.getMsg());
        }
      });
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } catch (Exception e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_GENERAL, "FourWCAPI", getMsg(ERROR_GENERAL));
    }
  }

  @SimpleEvent(description = "Triggered when web request is complete.")
  public void GotMaps(String opStatus, String msg) {
    EventDispatcher.dispatchEvent(this, "GotMaps", opStatus, msg);
  }

  public HSMap getMapByIndex(int index) {
    return HideAndSeek.getMaps().get(index);
  }

  public Player getPlayerByIndex(int index) {
    return HideAndSeek.getPlayers().get(index);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public int MapCount() {
    final String fName = "MapCount";

    try {
        return HideAndSeek.getMapCount();
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return 0;
  }

  @SimpleFunction(description = "Retrieves name of map if maps have been fetched.")
  public String GetMapNameAtIndex(int index) {
    final String fName = "GetMapNameAtIndex";

    try {
        return getMapByIndex(index).getName();
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } 

    return null;
  }

  @SimpleFunction(description = "Retrieves list of rounds of map if maps have been fetched.")
  public YailList GetMapRoundsAtIndex(int index) {
    final String fName = "GetMapRoundsAtIndex";

    try {
        return YailList.makeList(getMapByIndex(index).getRounds());
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } 

    return null;
  }

  @SimpleFunction(description = "Asks the web service to create a new game.")
  public void CreateGame(final int mapIndex, final int roundsIndex) {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        createGameRunnable(playerName, mapIndex, roundsIndex);
      }
    });
  }

  public void createGameRunnable(final String playerName, final int mapIndex, final int roundsIndex) {
    final String fName = "CreateGame";

    try {
      final Status opStatus = HideAndSeek.createGame(playerName, mapIndex, roundsIndex);

      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          CreatedGame(Status.toString(opStatus), opStatus.getMsg());
        }
      });
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } catch (Exception e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_GENERAL, "FourWCAPI", getMsg(ERROR_GENERAL));
    }
  }

  @SimpleEvent(description = "Triggered when web request is complete.")
  public void CreatedGame(String opStatus, String msg) {
    EventDispatcher.dispatchEvent(this, "CreatedGame", opStatus, msg);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String SessionID() {
    final String fName = "SessionID";

    try {
        return HideAndSeek.getSessionID();
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return null;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String GameID() {
    final String fName = "GameID";

    try {
        return HideAndSeek.getGameID();
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return null;
  }

  @SimpleProperty
  public void SessionID(String sessID) {
    HideAndSeek.setSessionID(sessID);
  }

  @SimpleProperty
  public void GameID(String gameID) {
    HideAndSeek.setGameID(gameID);
  }

  @SimpleFunction(description = "Asks the web service to join a game.")
  public void JoinGame(final String gameID) {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        joinGameRunnable(playerName, gameID);
      }
    });
  }

  public void joinGameRunnable(final String playerName, final String gameID) {
    final String fName = "JoinGame";

    try {
      final Status opStatus = HideAndSeek.joinGame(playerName, gameID);

      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          JoinedGame(Status.toString(opStatus), opStatus.getMsg());
        }
      });
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } catch (Exception e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_GENERAL, "FourWCAPI", getMsg(ERROR_GENERAL));
    }
  }

  @SimpleEvent(description = "Triggered when web request is complete.")
  public void JoinedGame(String opStatus, String msg) {
    EventDispatcher.dispatchEvent(this, "JoinedGame", opStatus, msg);
  }

  @SimpleFunction(description = "Asks the web service to fetch players.")
  public void FetchPlayers() {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        fetchPlayersRunnable();
      }
    });
  }

  public void fetchPlayersRunnable() {
    final String fName = "FetchPlayers";

    try {
      final Status opStatus = HideAndSeek.fetchPlayers();

      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          GotPlayers(Status.toString(opStatus), opStatus.getMsg());
        }
      });
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } catch (Exception e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_GENERAL, "FourWCAPI", getMsg(ERROR_GENERAL));
    }
  }

  @SimpleEvent(description = "Triggered when web request is complete.")
  public void GotPlayers(String opStatus, String msg) {
    EventDispatcher.dispatchEvent(this, "GotPlayers", opStatus, msg);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public int PlayerCount() {
    final String fName = "PlayerCount";

    try {
        return HideAndSeek.getPlayerCount();
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return 0;
  }

  @SimpleFunction(description = "Retrieves name of player if players have been fetched.")
  public String GetPlayerNameAtIndex(int index) {
    final String fName = "GetPlayerNameAtIndex";

    try {
        return getPlayerByIndex(index).getName();
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return null;
  }

  @SimpleFunction(description = "Retrieves colour of player if it's been fetched.")
  public String GetPlayerColourAtIndex(int index) {
    final String fName = "GetPlayerColourAtIndex";

    try {
        return getPlayerByIndex(index).getColour();
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return null;
  }

  @SimpleFunction(description = "Asks the web service to start a game.")
  public void StartGame() {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        startGameRunnable();
      }
    });
  }

  public void startGameRunnable() {
    final String fName = "StartGame";

    try {
      final Status opStatus = HideAndSeek.startGame();

      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          StartedGame(Status.toString(opStatus), opStatus.getMsg());
        }
      });
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } catch (Exception e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_GENERAL, "FourWCAPI", getMsg(ERROR_GENERAL));
    }
  }

  @SimpleEvent(description = "Triggered when web request is complete.")
  public void StartedGame(String opStatus, String msg) {
    EventDispatcher.dispatchEvent(this, "StartedGame", opStatus, msg);
  }

  @SimpleFunction(description = "Asks the web service to make a move.")
  public void MakeMove(final int destination, final String colour) {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        makeMoveRunnable(destination, colour);
      }
    });
  }

  public void makeMoveRunnable(final int destination, final String colour) {
    final String fName = "MakeMove";

    try {
      Ticket ticket = Ticket.fromString(colour);
      final Status opStatus = HideAndSeek.makeMove(destination, ticket);

      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          MadeMove(Status.toString(opStatus), opStatus.getMsg());
        }
      });
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } catch (Exception e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_GENERAL, "FourWCAPI", getMsg(ERROR_GENERAL));
    }
  }

  @SimpleEvent(description = "Triggered when web request is complete.")
  public void MadeMove(String opStatus, String msg) {
    EventDispatcher.dispatchEvent(this, "MadeMove", opStatus, msg);
  }

  @SimpleFunction(description = "Asks the web service to fetch position.")
  public void FetchPosition() {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        fetchPositionRunnable();
      }
    });
  }

  public void fetchPositionRunnable() {
    final String fName = "FetchPosition";

    try {
      final Status opStatus = HideAndSeek.fetchPosition();

      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          GotPosition(Status.toString(opStatus), opStatus.getMsg());
        }
      });
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } catch (Exception e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_GENERAL, "FourWCAPI", getMsg(ERROR_GENERAL));
    }
  }

  @SimpleEvent(description = "Triggered when web request is complete.")
  public void GotPosition(String opStatus, String msg) {
    EventDispatcher.dispatchEvent(this, "GotPosition", opStatus, msg);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String PositionLocation() {
    final String fName = "PositionLocation";

    try {
        return HideAndSeek.getPosition().getLocation();
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }
    return null;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public int PositionYellowT() {
    final String fName = "PositionYellowT";

    try {
        return HideAndSeek.getPosition().getYellow();
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return 0;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public int PositionGreenT() {
    final String fName = "PositionGreenT";

    try {
        return HideAndSeek.getPosition().getGreen();
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return 0;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public int PositionRedT() {
    final String fName = "PositionRedT";

    try {
        return HideAndSeek.getPosition().getRed();
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return 0;
  }

  @SimpleFunction(description = "Asks the web service to fetch gamestate.")
  public void FetchGameState() {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        fetchStateRunnable();
      }
    });
  }

  public void fetchStateRunnable() {
    final String fName = "FetchGameState";

    try {
      final Status opStatus = HideAndSeek.fetchGameState();

      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          GotGameState(Status.toString(opStatus), opStatus.getMsg());
        }
      });
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } catch (Exception e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_GENERAL, "FourWCAPI", getMsg(ERROR_GENERAL));
    }
  }

  @SimpleEvent(description = "Triggered when web request is complete.")
  public void GotGameState(String opStatus, String msg) {
    EventDispatcher.dispatchEvent(this, "GotGameState", opStatus, msg);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public int CurrentRound() {
    final String fName = "CurrentRound";
    try {
        return HideAndSeek.getGameState().getRound();
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return 0;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String CurrentMsg() {
    final String fName = "CurrentMsg";
    try {
        return HideAndSeek.getGameState().getMsg();
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return null;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String CurrentState() {
    final String fName = "CurrentState";
    try {
        return StateOfGame.toString(HideAndSeek.getGameState().getState());
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return null;
  }

  @SimpleFunction(description = "Asks the web service to fetch the dr x log.")
  public void FetchDrXLog() {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        fetchDrXLogRunnable();
      }
    });
  }

  public void fetchDrXLogRunnable() {
    final String fName = "FetchDrXLog";
    try {
      final Status opStatus = HideAndSeek.fetchDrXLog();

      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          GotDrXLog(Status.toString(opStatus), opStatus.getMsg());
        }
      });
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } catch (Exception e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_GENERAL, "FourWCAPI", getMsg(ERROR_GENERAL));
    }
  }

  @SimpleEvent(description = "Triggered when web request is complete.")
  public void GotDrXLog(String opStatus, String msg) {
    EventDispatcher.dispatchEvent(this, "GotDrXLog", opStatus, msg);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public int DrXLogCount() {
    final String fName = "DrXLogCount";

    try {
        return HideAndSeek.getDrXLogCount();
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return 0;
  }

  @SimpleFunction(description = "Retrieves name of map if maps have been fetched.")
  public String GetDrXTicketAtIndex(int index) {
    final String fName = "GetDrXTicketAtIndex";

    try {
        return Ticket.toString(HideAndSeek.getDrXLog().get(index));
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } 

    return null;
  }

  @SimpleFunction(description = "Asks the web service to fetch the player details.")
  public void FetchPlayerDetails() {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        fetchPlayerDetailsRunnable();
      }
    });
  }

  public void fetchPlayerDetailsRunnable() {
    final String fName = "FetchPlayerDetails";
    try {
      final Status opStatus = HideAndSeek.fetchPlayerDetails();

      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          GotPlayerDetails(Status.toString(opStatus), opStatus.getMsg());
        }
      });
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } catch (Exception e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_GENERAL, "FourWCAPI", getMsg(ERROR_GENERAL));
    }
  }

  @SimpleEvent(description = "Triggered when web request is complete.")
  public void GotPlayerDetails(String opStatus, String msg) {
    EventDispatcher.dispatchEvent(this, "GotPlayerDetails", opStatus, msg);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public int PlayerDetailsCount() {
    final String fName = "PlayerDetailsCount";

    try {
        return HideAndSeek.getPlayerDetailsCount();
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    }

    return 0;
  }

  public InGamePlayer getInGamePlayerByIndex(int index) {
    return HideAndSeek.getPlayerDetails().get(index);
  }

  @SimpleFunction(description = "Retrieves name of player.")
  public String GetPlayerDNameAtIndex(int index) {
    final String fName = "GetPlayerDNameAtIndex";

    try {
        return getInGamePlayerByIndex(index).getName();
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } 

    return null;
  }

  @SimpleFunction(description = "Retrieves colour of player.")
  public String GetPlayerDColourAtIndex(int index) {
    final String fName = "GetPlayerDColourAtIndex";

    try {
        return getInGamePlayerByIndex(index).getColour();
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } 

    return null;
  }

  @SimpleFunction(description = "Retrieves location of player.")
  public String GetPlayerDLocationAtIndex(int index) {
    final String fName = "GetPlayerDLocationAtIndex";

    try {
        return getInGamePlayerByIndex(index).getLocation();
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } 

    return null;
  }

  @SimpleFunction(description = "Retrieves yellow of player.")
  public int GetPlayerDYellowAtIndex(int index) {
    final String fName = "GetPlayerDYellowAtIndex";

    try {
        return getInGamePlayerByIndex(index).getYellow();
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } 

    return 0;
  }

  @SimpleFunction(description = "Retrieves green of player.")
  public int GetPlayerDGreenAtIndex(int index) {
    final String fName = "GetPlayerDGreenAtIndex";

    try {
        return getInGamePlayerByIndex(index).getGreen();
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } 

    return 0;
  }

  @SimpleFunction(description = "Retrieves red of player.")
  public int GetPlayerDRedAtIndex(int index) {
    final String fName = "GetPlayerDRedAtIndex";

    try {
        return getInGamePlayerByIndex(index).getRed();
    } catch (IndexOutOfBoundsException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INDEX_OUT_OF_RANGE, "FourWCAPI", getMsg(ERROR_INDEX_OUT_OF_RANGE));
    } catch (NullPointerException e) {
      form.dispatchErrorOccurredEvent(FourWCAPI.this, fName,
      ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_NULL_POINTER, "FourWCAPI", getMsg(ERROR_NULL_POINTER));
    } 

    return 0;
  }
}
