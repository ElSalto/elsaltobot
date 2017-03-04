package com.telegram.elsaltobot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Hello world!
 *
 */
public class App {
  public static void main(String[] args) {
    ApiContextInitializer.init();

    TelegramBotsApi botsApi = new TelegramBotsApi();

    try {
      botsApi.registerBot(new ShokesuBot());
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }
}
