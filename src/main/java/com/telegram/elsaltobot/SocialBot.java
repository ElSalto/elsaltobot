package com.telegram.elsaltobot;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class SocialBot extends TelegramLongPollingBot {

  private final static String AUTHORIZATION = "";
  private final static String API_URL = "";
  private final static String USER_AGENT = "Mozilla/5.0";
  private final static String ACCOUNTID = "";

  private final static String URL_TOPIC = "";

  @Override
  public String getBotUsername() {
    return "";
  }

  @Override
  public String getBotToken() {
    return "";
  }

  @Override
  public void onUpdateReceived(Update update) {

    // We check if the update has a message and the message has text
    if (update.hasMessage() && update.getMessage().hasText()) {
      // Set variables
      String message_text = update.getMessage().getText();
      long chat_id = update.getMessage().getChatId();

      if (message_text.equals("/start")) {
        try {
          SendMessage message = getShokesusMessage(chat_id, null);
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }

      } else if (message_text.equals("/projects")) {
        try {
          SendMessage message = getShokesusMessage(chat_id, null);
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }

      } else if (message_text.equals("/help")) {
        try {
          SendMessage message = new SendMessage().setChatId(chat_id)
              .setText("Los comandos a utilizar son:\n"
                  + "-/projects: muestra todos los proyectos\n"
                  + "-/projects_now: una vez seleccionado un proyecto te muestra las 10 últimas publicaciones\n"
                  + "-/projects_top: una vez seleccionado un proyecto te muestra las 10 perfiles más influyentes\n"
                  + "-/locals 40.440689 -3.7869853: muestra las 10 últimas publicaciones según la posición de la que se realiza la petición\n"
                  + "-/projects_engagement: una vez seleccionado un proyecto te muestra las 10 publicaciones con más engagement\n"
                  + "-/projects_topic: una vez seleccionado un proyecto te muestra la nube de palabras con las palabras más usadas");
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }

      } else if (message_text.equals("/projects_now")) {
        try {
          SendMessage message = getShokesusMessage(chat_id, "now");
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }

      } else if (message_text.equals("/projects_engagement")) {
        try {
          SendMessage message = getShokesusMessage(chat_id, "engagement");
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }

      } else if (message_text.equals("/projects_top")) {
        try {
          SendMessage message = getShokesusMessage(chat_id, "top");
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }
      } else if (message_text.equals("/projects_topic")) {
        try {
          SendMessage message = getShokesusMessage(chat_id, "topics");
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }
      } else if (message_text.contains("/locals")) {

        String[] geo = message_text.split(" ");

        if (geo.length == 3) {
          tratarResultadoPostsPuros(
              doGetRequest("shokesu/provider/local?latitude=" + geo[1] + "&longitude=" + geo[2],
                  null, false),
              chat_id);
        } else {
          SendMessage message = new SendMessage() // Create a message object object
              .setChatId(chat_id).setText("Unknown command");
          try {
            sendMessage(message); // Sending our message object to user
          } catch (TelegramApiException e) {
            e.printStackTrace();
          }
        }
      } else {
        SendMessage message = new SendMessage() // Create a message object object
            .setChatId(chat_id).setText("Unknown command");
        try {
          sendMessage(message); // Sending our message object to user
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }
      }
    } else if (update.hasCallbackQuery()) {
      // Set variables
      String call_data = update.getCallbackQuery().getData();
      long message_id = update.getCallbackQuery().getMessage().getMessageId();
      long chat_id = update.getCallbackQuery().getMessage().getChatId();

      String[] callData = call_data.split("_");

      String result = "";
      String answer = "";

      switch ((callData.length > 1 ? callData[1] : "")) {
      case "now":
        result = doPostRequest(
            "shokesu/site/" + callData[0] + "/posts?pageNumber=1&pageSize=10&sort=", null);
        tratarResultadoPosts(result, chat_id, message_id);
        try {
          SendMessage message = getShokesusOptions(chat_id, callData[0]);
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }
        break;
      case "topics":
        String url = "share/static/site/" + callData[0] + "/dashboard/cloud/graph/cloud";

        BufferedImage resultTopic = doGetPhotoRequest(url, null, true);
        if (resultTopic == null) {
          SendMessage message = new SendMessage().setChatId(chat_id)
              .setText("No ha sido posible obtener la nube de palabras");
          try {
            sendMessage(message);
          } catch (TelegramApiException e) {
            e.printStackTrace();
          }
          return;
        }
        try {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ImageIO.write(resultTopic, "png", baos);
          baos.flush();
          byte[] imageInByte = baos.toByteArray();
          baos.close();

          SendPhoto msg = new SendPhoto().setChatId(chat_id)
              .setNewPhoto("Nube de palabras", new ByteArrayInputStream(imageInByte))
              .setCaption("Topics del proyecto");

          sendPhoto(msg);
          SendMessage message = getShokesusOptions(chat_id, callData[0]);
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        return;
      case "bars":
        String urlBar = "share/static/site/" + callData[0] + "/dashboard/etiquetas/graph/etiquetas";

        BufferedImage resultTopicBar = doGetPhotoRequest(urlBar, null, true);
        if (resultTopicBar == null) {
          SendMessage message = new SendMessage().setChatId(chat_id)
              .setText("No ha sido posible obtener la nube de palabras");
          try {
            sendMessage(message);
          } catch (TelegramApiException e) {
            e.printStackTrace();
          }
          return;
        }
        try {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ImageIO.write(resultTopicBar, "png", baos);
          baos.flush();
          byte[] imageInByte = baos.toByteArray();
          baos.close();

          SendPhoto msg = new SendPhoto().setChatId(chat_id)
              .setNewPhoto("Nube de palabras", new ByteArrayInputStream(imageInByte))
              .setCaption("Topics del proyecto");

          sendPhoto(msg);
          SendMessage message = getShokesusOptions(chat_id, callData[0]);
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        return;
      case "top":
        result = doPostRequest("shokesu/site/" + callData[0]
            + "/getauthors?pageNumber=1&pageSize=10&sort=followers_desc", null);
        tratarResultadoUsuarios(result, chat_id, message_id);
        try {
          SendMessage message = getShokesusOptions(chat_id, callData[0]);
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }
        break;
      case "engagement":

        String filter = "{'user_term':[],'content_type':['post','photo','video','link'],'friends_count':{'from':0,'to':1000000},'posts_avg':{'from':0,'to':20},'monitoring':null,'verified':null,'content_term_excl':[],'favorite_count':{'from':0,'to':10000},'user_tags':[],'date':{'from':'2017-02-01T15:37:59.949Z','to':null},'lang_post':[],'provider':['twitter','youtube','instagram','googleplus','facebook'],'content_term':[],'shared_count':{'from':0,'to':10000},'tags':[],'post_type':['original'],'user_term_excl':[],'lang_user':[]}";

        result = doGetRequest("shokesu/site/" + callData[0] + "/graphic?filter=" + filter, null,
            false);

        tratarResultadoPostsEngagement(result, chat_id, message_id);
        try {
          SendMessage message = getShokesusOptions(chat_id, callData[0]);
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }
        break;
      case "projects":
        SendMessage messageProjects = getShokesusMessage(chat_id, null);
        try {
          sendMessage(messageProjects);
        } catch (TelegramApiException e1) {
          e1.printStackTrace();
        }
        break;
      case "":
        String filterEngagement = "{'user_term':[],'content_type':['post','photo','video','link'],'friends_count':{'from':0,'to':1000000},'posts_avg':{'from':0,'to':20},'monitoring':null,'verified':null,'content_term_excl':[],'favorite_count':{'from':0,'to':10000},'user_tags':[],'date':{'from':'2017-02-01T15:37:59.949Z','to':null},'lang_post':[],'provider':['twitter','youtube','instagram','googleplus','facebook'],'content_term':[],'shared_count':{'from':0,'to':10000},'tags':[],'post_type':['original'],'user_term_excl':[],'lang_user':[]}";

        result = doGetRequest("shokesu/site/" + callData[0] + "/graphic?filter=" + filterEngagement,
            null, false);

        tratarResultadoPostsEngagement(result, chat_id, message_id);
        SendMessage message = getShokesusOptions(chat_id, callData[0]);
        try {
          sendMessage(message);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }
        break;
      }
    }
  }

  private void tratarResultadoPostsEngagement(String result, long chat_id, long message_id) {

    JSONObject resultJSON = new JSONObject(result);

    JSONArray postsList = resultJSON.getJSONArray("values");

    StringBuffer responseMessage = null;

    for (int i = 0; i < postsList.length(); i++) {

      responseMessage = new StringBuffer();
      String postText = "";
      if (postsList.getJSONObject(i).has("description")
          && postsList.getJSONObject(i).opt("description") != JSONObject.NULL) {
        postText = postsList.getJSONObject(i).getString("description");
      }

      responseMessage.append("<b>Post número: " + (i + 1) + "</b>\n");
      // responseMessage.append("<a href='"
      // + postsList.getJSONObject(i).getJSONObject("user").getString("url") + "'>autor: "
      // + postsList.getJSONObject(i).getJSONObject("user").getString("name") + "</a>\n");
      responseMessage.append("mensaje: <a href='" + postsList.getJSONObject(i).getString("post_url")
          + "'>" + postText + "</a>\n");

      // if (postsList.getJSONObject(i).has("retweet_count")) {
      // responseMessage
      // .append("retweet count: " + postsList.getJSONObject(i).getInt("retweet_count") + "\n");
      // }

      SendMessage new_message = new SendMessage().setChatId(chat_id)
          .setText(responseMessage.toString()).setParseMode("HTML");
      try {
        sendMessage(new_message);
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }
    }

  }

  private void tratarResultadoUsuarios(String result, long chat_id, long message_id) {
    JSONArray postsList = new JSONArray(result);
    StringBuffer responseMessage = null;

    for (int i = 0; i < postsList.length(); i++) {
      responseMessage = new StringBuffer();
      JSONObject user = postsList.getJSONObject(i).getJSONObject("user");

      responseMessage.append("<b>Usuario número: " + (i + 1) + "</b>\n");

      if (!user.has("url")) {
        String provider = user.getString("provider");
        try {
          switch (provider) {
          case "twitter":
            responseMessage.append("<a href='" + "https://twitter.com/"
                + URLEncoder.encode(user.getString("screenname"), "UTF-8") + "'>nombre: "
                + user.getString("name") + "</a>\n");
            break;
          case "instagram":
            responseMessage
                .append("<a href='" + "https://www.instagram.com/" + user.getString("name")
                    + "'>nombre: " + URLEncoder.encode(user.getString("name"), "UTF-8") + "</a>\n");
            break;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        responseMessage.append(
            "<a href='" + user.getString("url") + "'>nombre: " + user.getString("name") + "</a>\n");
      }
      if (user.has("friends_count")) {
        responseMessage.append("friends count: " + user.getInt("friends_count") + "\n");
      }
      if (user.has("favourites_count")) {
        responseMessage.append("likes: " + user.getInt("favourites_count") + "\n");
      }
      if (user.has("followers_count")) {
        responseMessage.append("followers count: " + user.getInt("followers_count") + "\n");
      }

      String userPhotoUrl = user.getString("photo");

      BufferedImage resultTopic = doGetPhotoRequest(userPhotoUrl, null, false);
      try {

        if (resultTopic != null) {

          // resize(resultTopic, 50, 50);
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ImageIO.write(resultTopic, "png", baos);
          baos.flush();
          byte[] imageInByte = baos.toByteArray();
          baos.close();

          SendPhoto msg = new SendPhoto().setChatId(chat_id).setNewPhoto("Foto perfil",
              new ByteArrayInputStream(imageInByte));

          sendPhoto(msg);
        }
        SendMessage new_message = new SendMessage().setChatId(chat_id)
            .setText(responseMessage.toString()).setParseMode("HTML");

        sendMessage(new_message);
      } catch (TelegramApiException | IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void tratarResultadoPosts(String result, long chat_id, long message_id) {
    JSONArray postsList = new JSONArray(result);

    StringBuffer responseMessage = null;

    for (int i = 0; i < postsList.length(); i++) {

      responseMessage = new StringBuffer();
      JSONObject postText = new JSONObject();
      if (postsList.getJSONObject(i).has("title")
          && postsList.getJSONObject(i).opt("title") != JSONObject.NULL) {
        postText = postsList.getJSONObject(i).getJSONObject("title");
      } else {
        postText = postsList.getJSONObject(i).getJSONObject("body");
      }

      JSONArray keys = postText.names();

      responseMessage.append("<b>Post número: " + (i + 1) + "</b>\n");
      // responseMessage.append("<a href='"
      // + postsList.getJSONObject(i).getJSONObject("user").getString("url") + "'>autor: "
      // + postsList.getJSONObject(i).getJSONObject("user").getString("name") + "</a>\n");
      responseMessage.append("mensaje: <a href='" + postsList.getJSONObject(i).getString("url")
          + "'>" + postText.getString(keys.getString(0)) + "</a>\n");

      // if (postsList.getJSONObject(i).has("retweet_count")) {
      // responseMessage
      // .append("retweet count: " + postsList.getJSONObject(i).getInt("retweet_count") + "\n");
      // }
      if (postsList.getJSONObject(i).has("favorite_count")) {
        responseMessage
            .append("likes: " + postsList.getJSONObject(i).getInt("favorite_count") + "\n");
      }
      SendMessage new_message = new SendMessage().setChatId(chat_id)
          .setText(responseMessage.toString()).setParseMode("HTML");
      try {
        sendMessage(new_message);
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }
    }
  }

  private void tratarResultadoPostsPuros(String result, long chat_id) {
    JSONArray postsList = new JSONArray(result);

    for (int i = 0; i < postsList.length(); i++) {
      StringBuffer responseMessage = new StringBuffer();

      String postText = null;
      if (postsList.getJSONObject(i).has("title")
          && postsList.getJSONObject(i).opt("title") != JSONObject.NULL) {
        postText = postsList.getJSONObject(i).getString("title");
      } else {
        postText = postsList.getJSONObject(i).getString("body");
      }

      responseMessage.append("<b>Post número: " + (i + 1) + "</b>\n");
      responseMessage.append("<a href='"
          + postsList.getJSONObject(i).getJSONObject("user").getString("url") + "'>autor: "
          + postsList.getJSONObject(i).getJSONObject("user").getString("name") + "</a>\n");
      responseMessage.append("<a href='" + postsList.getJSONObject(i).getString("url")
          + "'>mensaje: " + postText + "</a>\n");

      // if (postsList.getJSONObject(i).has("retweet_count")) {
      // responseMessage
      // .append("retweet count: " + postsList.getJSONObject(i).getInt("retweet_count") + "\n");
      // }
      if (postsList.getJSONObject(i).has("favorite_count")) {
        responseMessage
            .append("likes: " + postsList.getJSONObject(i).getInt("favorite_count") + "\n");
      }

      SendMessage new_message = new SendMessage().setChatId(chat_id)
          .setText(responseMessage.toString()).setParseMode("HTML");
      try {
        sendMessage(new_message);
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }

    }
  }

  private SendMessage getShokesusMessage(long chat_id, String type) {
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("size", "xs");
    String result = doGetRequest("shokesu/account/" + ACCOUNTID + "/user/site", params, false);

    if (result == null) {
      return null;
    }

    JSONArray sites = new JSONArray(result);

    SendMessage message = new SendMessage() // Create a message object object
        .setChatId(chat_id).setText("Lista de proyectos");
    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
    List<InlineKeyboardButton> rowInline = new ArrayList<>();

    for (int i = 0; i < sites.length(); i++) {

      if (type != null) {
        rowInline.add(new InlineKeyboardButton().setText(sites.getJSONObject(i).getString("title"))
            .setCallbackData(sites.getJSONObject(i).getString("id") + "_" + type));
      } else {
        rowInline.add(new InlineKeyboardButton().setText(sites.getJSONObject(i).getString("title"))
            .setCallbackData(sites.getJSONObject(i).getString("id")));
      }
      if (i % 2 == 0) {
        rowsInline.add(rowInline);
        rowInline = new ArrayList<>();
      }
    }
    if (rowInline.size() > 0) {
      rowsInline.add(rowInline);
    }

    // Add it to the message
    markupInline.setKeyboard(rowsInline);
    message.setReplyMarkup(markupInline);
    return message;
  }

  private SendMessage getShokesusOptions(long chat_id, String callData) {
    SendMessage message = new SendMessage() // Create a message object object
        .setChatId(chat_id).setText("Lista de opciones");
    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
    List<InlineKeyboardButton> rowInline = new ArrayList<>();

    rowInline.add(new InlineKeyboardButton().setText("Últimos 10 mensajes")
        .setCallbackData(callData + "_now"));

    rowInline.add(new InlineKeyboardButton().setText("Palabras mencionadas")
        .setCallbackData(callData + "_topics"));

    rowsInline.add(rowInline);

    rowInline = new ArrayList<>();

    rowInline.add(new InlineKeyboardButton().setText("10 usuarios más influyentes")
        .setCallbackData(callData + "_top"));

    rowsInline.add(rowInline);

    rowInline = new ArrayList<>();

    rowInline.add(new InlineKeyboardButton().setText("10 publicaciones con más engagement")
        .setCallbackData(callData + "_engagement"));

    rowsInline.add(rowInline);

    rowInline = new ArrayList<>();

    if ("3d4b6bee-9aa4-4dbf-973d-7163690c72ca".equals(callData)
        || "c0aaf852-a5cb-43af-917a-37f9a599d234".equals(callData)) {
      rowInline.add(new InlineKeyboardButton().setText("Mostrar histograma")
          .setCallbackData(callData + "_bars"));

      rowsInline.add(rowInline);

      rowInline = new ArrayList<>();
    }

    rowInline.add(new InlineKeyboardButton().setText("Ver todos los proyectos")
        .setCallbackData(callData + "_projects"));

    rowsInline.add(rowInline);
    // Add it to the message
    markupInline.setKeyboard(rowsInline);
    message.setReplyMarkup(markupInline);
    return message;
  }

  private String doPostRequest(String url, HashMap<String, String> params) {
    try {
      URL obj = new URL(API_URL + url);

      HttpURLConnection con = (HttpURLConnection) obj.openConnection();

      // optional default is GET
      con.setRequestMethod("POST");

      // add request header
      con.setRequestProperty("User-Agent", USER_AGENT);
      con.setRequestProperty("Authorization", AUTHORIZATION);
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestProperty("Accept", "application/json");
      con.setRequestProperty("charset", "utf-8");
      con.setDoInput(true);
      con.setDoOutput(true);

      if (params != null) {
        for (String key : params.keySet()) {
          con.setRequestProperty(key, params.get(key));
        }
      }

      JSONObject filter = new JSONObject();
      filter.put("filter", new JSONObject(
          "{'user_term':[],'content_type':['post','photo','video','link'],'friends_count':{'from':0,'to':1000000},'posts_avg':{'from':0,'to':20},'monitoring':null,'verified':null,'content_term_excl':[],'favorite_count':{'from':0,'to':10000},'user_tags':[],'date':{'from':'2017-02-01T15:37:59.949Z','to':null},'lang_post':[],'provider':['twitter','youtube','instagram','googleplus','facebook'],'content_term':[],'shared_count':{'from':0,'to':10000},'tags':[],'post_type':['original'],'user_term_excl':[],'lang_user':[]}"));

      OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
      wr.write(filter.toString());
      wr.flush();
      wr.close();

      int responseCode = con.getResponseCode();
      System.out.println("\nSending 'POST' request to URL : " + API_URL + url);
      System.out.println("Post parameters : " + filter.toString());
      System.out.println("Response Code : " + responseCode);

      InputStream gzipStream = new GZIPInputStream(con.getInputStream());
      Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
      BufferedReader in = new BufferedReader(decoder);

      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      // print result
      return response.toString();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  private String doGetRequest(String url, HashMap<String, String> params, boolean useTopicURL) {
    try {

      URL obj = new URL(API_URL + url);
      if (useTopicURL) {
        obj = new URL(URL_TOPIC + url);
      }

      HttpURLConnection con = (HttpURLConnection) obj.openConnection();

      // optional default is GET
      con.setRequestMethod("GET");

      // add request header
      con.setRequestProperty("User-Agent", USER_AGENT);
      con.setRequestProperty("Authorization", AUTHORIZATION);

      if (params != null) {
        for (String key : params.keySet()) {
          con.setRequestProperty(key, params.get(key));
        }
      }

      int responseCode = con.getResponseCode();

      System.out.println("\nSending 'GET' request to URL : " + API_URL + url);
      System.out.println("Response Code : " + responseCode);

      InputStream gzipStream = new GZIPInputStream(con.getInputStream());
      Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
      BufferedReader in = new BufferedReader(decoder);

      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      // print result
      return response.toString();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public static BufferedImage resize(BufferedImage img, int newW, int newH) {
    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

    Graphics2D g2d = dimg.createGraphics();
    g2d.drawImage(tmp, 0, 0, null);
    g2d.dispose();

    return dimg;
  }

  private BufferedImage doGetPhotoRequest(String url, HashMap<String, String> params,
      boolean useTopicURL) {
    try {

      URL urlPhoto = null;
      if (useTopicURL) {
        urlPhoto = new URL(URL_TOPIC + url);
      } else {
        urlPhoto = new URL(url);
      }

      BufferedImage in = ImageIO.read(urlPhoto);

      return in;

    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
