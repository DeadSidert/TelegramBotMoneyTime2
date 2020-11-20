package com.nikita.botter.bot;

import com.nikita.botter.model.Channel;
import com.nikita.botter.model.User;
import com.nikita.botter.util.TelegramUtil;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String token;

    @Value("${bot.name}")
    private String botName;
    private final ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    private final HashMap<Integer, User> userHashMap = new HashMap<>();
    private final List<Channel> channels;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String command;
        String[] txt = null;
        User user;
        SendMessage sendMessage;
        int userId = 0;

        if (isMessageWithText(update)){
            userId = update.getMessage().getFrom().getId();
            command = TelegramUtil.extractCommand(update.getMessage().getText());

            // проверка существования юзера
           if (userHashMap.containsKey(userId)){
               user = userHashMap.get(userId);
           } else {

           }

           if ("/start".equalsIgnoreCase(command)){

           }
        }
    }

    // проверка на текст
    private boolean isMessageWithText(Update update) {
        return !update.hasCallbackQuery() && update.hasMessage() && update.getMessage().hasText();
    }

    private boolean checkUserInChannels(int userId){
         // заполняем из бд каналами
         if (channels == null || channels.isEmpty()){

         }


         return true;
    }

    // создание меню для юзеров
    public void createUserMenu(){
        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("➕ Подписки");
        keyboardRow.add("\uD83D\uDD06 Бонус");

        KeyboardRow keyboardRow1 = new KeyboardRow();
        keyboardRow1.add("\uD83D\uDCB5 Баланс");
        keyboardRow1.add("\uD83D\uDC54 Партнерам");

        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow2.add("\uD83D\uDCC6 Информация");

        rowList.add(keyboardRow);
        rowList.add(keyboardRow1);
        rowList.add(keyboardRow2);

        keyboardMarkup.setKeyboard(rowList);
    }
}
