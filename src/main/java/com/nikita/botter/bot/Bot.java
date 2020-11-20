package com.nikita.botter.bot;

import com.nikita.botter.bot.builder.MessageBuilder;
import com.nikita.botter.model.Channel;
import com.nikita.botter.model.User;
import com.nikita.botter.service.ChannelService;
import com.nikita.botter.service.UserService;
import com.nikita.botter.util.TelegramUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String token;

    @Value("${bot.name}")
    private String botName;
    private final ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    private final HashMap<Integer, User> userHashMap = new HashMap<>();
    private List<Channel> channelsStart;

    private UserService userService;
    private ChannelService channelService;

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
        User user;
        SendMessage sendMessage;
        int userId = 0;
        String userIdString;
        String arguments;

        if (isMessageWithText(update)){
            userId = update.getMessage().getFrom().getId();
            userIdString = String.valueOf(userId);
            command = TelegramUtil.extractCommand(update.getMessage().getText());
            arguments = TelegramUtil.extractArguments(update.getMessage().getText());

            // проверка существования юзера
             user = existUser(userId);

           // проверка пришел ли юзер через реф ссылку
           if ("/start".equalsIgnoreCase(command) && command.split(" ")[1] != null){
               if (user.getReferUrl() == null || user.getReferUrl().equals("")){
                   String refer = command.split(" ")[1];
                   user.setReferUrl(refer);
                   int referId = 0;
                   try {
                       referId = Integer.parseInt(refer);
                   }catch (Exception e){
                       log.error("Ошибка при получении реферала: {} ", refer);
                   }
                   User ref = existUser(referId);
                   ref.setMoney(ref.getMoney() + 3);
                   ref.setCountRefs(ref.getCountRefs() +1);
                   userService.update(user);
                   userService.update(ref);
                   log.info("Юзер {} привел реферала id: ", userId);
               }
           }

           else if ("/start".equalsIgnoreCase(command)){

              if (!user.isAuth()){
                  executeWithExceptionCheck(checkUserInStartChannels(user));
              }
              else {
                  createUserMenu();
              }
              sendMessage = new SendMessage();
              sendMessage.setChatId(userIdString);
              sendMessage.setText("Добро пожаловать");
              sendMessage.setReplyMarkup(keyboardMarkup);
              log.info("Приветственное сообщение юзеру {}", userIdString);
              executeWithExceptionCheck(sendMessage);
           }


        }
    }


    private void executeWithExceptionCheck(SendMessage sendMessage){
        try {
            log.info("Отправили сообщение юзеру {}", sendMessage.getChatId());
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // проверка на текст
    private boolean isMessageWithText(Update update) {
        return !update.hasCallbackQuery() && update.hasMessage() && update.getMessage().hasText();
    }

    // проверка на подписку в стартовых каналах
    private SendMessage checkUserInStartChannels(User user){
         int userId = user.getId();
         GetChatMember getChatMember = new GetChatMember();
         List<String> statuses = new ArrayList<>();

         // заполняем из бд каналами
         if (channelsStart == null || channelsStart.isEmpty()){
            channelsStart = channelService.findAll(true);
            log.info("Заполнили стартовые каналы");
         }
         getChatMember.setUserId(userId);
         ChatMember chatMember = null;

         for (Channel channel : channelsStart){
             getChatMember.setChatId(channel.getId());
             try{
                 chatMember = execute(getChatMember);
                 statuses.add(chatMember.getStatus());
             }catch (TelegramApiException e){
                 log.error("Ошибка при execute getChatMember id: {}", channel.getId());
             }
         }

         if (statuses.contains("left")){
             MessageBuilder messageBuilder = MessageBuilder.create(user);
             messageBuilder
                     .line("Вам нужно подписаться на каналы снизу:");
             for (Channel channel : channelsStart){
                 messageBuilder
                         .row()
                         .buttonWithUrl("Подписаться", channel.getUrl());
             }
             log.info("Юзер {} не подписан на стартовые каналы", userId);
             return messageBuilder.build();
         }
         MessageBuilder messageBuilder = MessageBuilder.create(user);
         messageBuilder.line("Добро пожаловать");

         SendMessage sendMessage = messageBuilder.build();
         sendMessage.setReplyMarkup(keyboardMarkup);

         return sendMessage;
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
        log.info("Создали userMenu");
    }

    // проверка существования юзера
    public User existUser(int userId){
        if (userHashMap.containsKey(userId)){
            log.info("Юзера достали из мапы id: {}", userId);
            return userHashMap.get(userId);
        } else if (userService.userExist(userId)){
            User user = userService.findById(userId);
            userHashMap.put(userId, user);
            log.info("Юзера достали из бд id: {}", userId);
            return user;
        }
        else {
            User user = new User(userId);
            userHashMap.put(userId, user);
            userService.update(user);
            log.info("Юзера создали id: {}", userId);
            return user;
        }
    }
}
