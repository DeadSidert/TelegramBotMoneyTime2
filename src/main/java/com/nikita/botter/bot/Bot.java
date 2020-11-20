package com.nikita.botter.bot;

import com.nikita.botter.bot.builder.MessageBuilder;
import com.nikita.botter.model.Channel;
import com.nikita.botter.model.User;
import com.nikita.botter.service.ChannelService;
import com.nikita.botter.service.UserService;
import com.nikita.botter.util.TelegramUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String token;

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.adminId}")
    private String adminId;

    private final ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

    private final UserService userService;
    private final ChannelService channelService;

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
        String[] txt;
        User user;
        SendMessage sendMessage;
        int userId = 0;
        String userIdString;
        String arguments;

        // если приходит текст
        if (isMessageWithText(update)){
            userId = update.getMessage().getFrom().getId();
            userIdString = String.valueOf(userId);
            command = TelegramUtil.extractCommand(update.getMessage().getText());
            txt = update.getMessage().getText().split(" ");


            // проверка существования юзера
             user = existUser(userId);

             if ("back".equalsIgnoreCase(user.getPosition())){
                 executeWithExceptionCheck(fabricPositions(update, user.getPosition()));
             }

           // проверка пришел ли юзер через реф ссылку
           if ("/start".equalsIgnoreCase(command) && txt.length > 1){
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
                   ref.setCountRefs(ref.getCountRefs() + 1);
                   userService.update(user);
                   userService.update(ref);
                   log.info("Юзер {} привел реферала id: ", userId);
               }
           }
           // команда /start
           else if ("/start".equalsIgnoreCase(command)){

              if (!user.isAuth()){
                  executeWithExceptionCheck(checkUserInStartChannels(user));
              }

              else {
                  createUserMenu();
              }
              MessageBuilder messageBuilder = MessageBuilder.create(user);
              messageBuilder.line("Добро пожаловать");
              sendMessage = messageBuilder.build();
              sendMessage.setReplyMarkup(keyboardMarkup);
              log.info("Приветственное сообщение юзеру {}", userIdString);
              executeWithExceptionCheck(sendMessage);
           }
           // войти в админ меню
           else if ("/admin_menu".equalsIgnoreCase(command)){
               if (userIdString.equalsIgnoreCase(adminId)){
                   createAdminMenu();
                   MessageBuilder messageBuilder = MessageBuilder.create(adminId);
                   sendMessage = messageBuilder.build();
                   sendMessage.setReplyMarkup(keyboardMarkup);
                   executeWithExceptionCheck(sendMessage);
               }
               else {
                   MessageBuilder messageBuilder = MessageBuilder.create(user);
                   messageBuilder.line("Вы не админ");
                   executeWithExceptionCheck(messageBuilder.build());
               }
           }
           // добавить канал
           else if ("Добавить канал".equalsIgnoreCase(command)){
               if (userIdString.equalsIgnoreCase(adminId)){
                   sendMessage = addChannel(update);
                   executeWithExceptionCheck(sendMessage);
               }
               else {
                   MessageBuilder messageBuilder = MessageBuilder.create(user);
                   messageBuilder.line("Вы не админ");
                   executeWithExceptionCheck(messageBuilder.build());
               }
           }
           // удалить канал
           else if ("Удалить канал".equalsIgnoreCase(command)){
               if (userIdString.equalsIgnoreCase(adminId)){
                   sendMessage = deleteChannel(update);
                   executeWithExceptionCheck(sendMessage);
               }
               else {
                   MessageBuilder messageBuilder = MessageBuilder.create(user);
                   messageBuilder.line("Вы не админ");
                   executeWithExceptionCheck(messageBuilder.build());
               }
           }


        }

        // если приходит Callback
        else if (update.hasCallbackQuery()){
            userId = update.getCallbackQuery().getFrom().getId();
            command = TelegramUtil.extractCommand(update.getCallbackQuery().getData());

            // проверка существования юзера
            user = existUser(userId);

            // проверка на подписку в стартовых каналах
            if ("/start_continue".equalsIgnoreCase(command)){
                executeWithExceptionCheck(checkStartImpl(user));
            }
            else if ("/cancel".equalsIgnoreCase(command)){
                executeWithExceptionCheck(cancel(user));
            }
        }
    }

    public SendMessage fabricPositions(Update update, String position){
      if ("add_channel".equalsIgnoreCase(position)){
          return addChannelImpl(update);
      }
      else if ("delete_channel".equalsIgnoreCase(position)){
            return deleteChannelImpl(update);
      }
      return new SendMessage();
    }

    private void executeWithExceptionCheck(SendMessage sendMessage){
        try {
            log.info("Отправили сообщение юзеру {}", sendMessage.getChatId());
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    // проверка на текст
    private boolean isMessageWithText(Update update) {
        return update != null && !update.hasCallbackQuery() && update.hasMessage() && update.getMessage().hasText();
    }

    // проверка на подписку в стартовых каналах
    private SendMessage checkUserInStartChannels(User user){
         int userId = user.getId();
         List<String> statuses;

         List<Channel> channelsStart = channelService.findAll(true);

         // собираем статусы во всех стартовых каналах
         statuses = checkStart(channelsStart, userId);

         // проверяем лист статусов на отсутствие в канале
         if (statuses.contains("left")){
             MessageBuilder messageBuilder = MessageBuilder.create(user);
             messageBuilder
                     .line("Вам нужно подписаться на каналы снизу:");
             for (Channel channel : channelsStart){
                 messageBuilder
                         .row()
                         .buttonWithUrl("Подписаться", channel.getUrl());
             }
             messageBuilder
                     .row()
                     .button("Продолжить", "/startContinue");

             log.info("Юзер {} не подписан на стартовые каналы", userId);
             return messageBuilder.build();
         }

         return checkStartImpl(user);
    }

    public List<String> checkStart(List<Channel> c, int userId){
        GetChatMember getChatMember = new GetChatMember();
        List<String> statuses = new ArrayList<>();
        getChatMember.setUserId(userId);

        ChatMember chatMember = null;

        for (Channel channel : c){
            getChatMember.setChatId(channel.getId());
            try{
                chatMember = execute(getChatMember);
                statuses.add(chatMember.getStatus());
            }catch (TelegramApiException e){
                log.error("Ошибка при execute getChatMember id: {}", channel.getId());
            }
        }
        return statuses;
    }

    public SendMessage checkStartImpl(User user){
        int userId = user.getId();

        // заполняем из бд каналами
        List<Channel> channelsStart = channelService.findAll(true);
        List<String> statuses = checkStart(channelsStart, userId);

        if (statuses.contains("left")){
           MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
           messageBuilder.line("Вы подписались не на все каналы");
           return messageBuilder.build();
        }

        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        messageBuilder.line("Вы успешно авторизовались");
        SendMessage sendMessage = messageBuilder.build();

        log.info("Юзер {} подписался на стартовые каналы", userId );
        user.setAuth(true);
        userService.update(user);

        createUserMenu();
        sendMessage.setReplyMarkup(keyboardMarkup);

        return sendMessage;
    }

    // добавление канала
    public SendMessage addChannel(Update update){
        int userId = update.getMessage().getFrom().getId();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        User user = userService.findById(userId);
        user.setPosition("add_channel");
        userService.update(user);
        log.info("Позиция юзера {} add_channel", userId);

        return messageBuilder
                .line("Введите @id url price start(0 or 1)")
                .row()
                .button("Отмена", "/cancel")
                .build();
    }

    // удаление канала
    public SendMessage deleteChannel(Update update){
        int userId = update.getMessage().getFrom().getId();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        User user = userService.findById(userId);
        user.setPosition("delete_channel");
        userService.update(user);
        log.info("Позиция юзера {} delete_channel", userId);

        return messageBuilder
                .line("Введите @id")
                .row()
                .button("Отмена", "/cancel")
                .build();
    }

    // удаление канала Impl
    public SendMessage deleteChannelImpl(Update update){
        int userId = update.getMessage().getFrom().getId();
        String channelId = update.getMessage().getText();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        User user = userService.findById(userId);
        user.setPosition("back");
        userService.update(user);

        log.info("Юзер {} удалил канал", userId);

        if (!channelService.isExist(channelId)){
            return messageBuilder
                    .line("Такого канала не существует, введите заново")
                    .build();
        }
        channelService.delete(channelId);

        return messageBuilder
                .line("Канал удален")
                .build();
    }

    public SendMessage addChannelImpl(Update update){
        int userId = update.getMessage().getFrom().getId();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        User user = userService.findById(userId);

        String[] arguments = update.getMessage().getText().split(" ");

        if (arguments.length < 4){
            log.error("Неправильный размер аргументов при добавлении канала");
            return messageBuilder.line("Вы указали не 4 аргумента").build();
        }

        String id =  arguments[0];
        String url = arguments[1];
        double price = 0;

        try {
            price = Double.parseDouble(arguments[2]);
        }catch (Exception e){
            log.error("Неправильный price при добавлении канала");
            return messageBuilder.line("price должно быть числом").build();
        }
        boolean start = arguments[3].equals("1");

        Channel channel = new Channel(id, url, price, start);
        user.setPosition("back");

        channelService.update(channel);
        userService.update(user);
        log.info("Юзер {} добавил канал", userId);

        return messageBuilder.line("Канал успешно добавлен").build();
    }

    // создание меню для юзеров
    public void createUserMenu(){
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        keyboardMarkup.setSelective(true);
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

    //Создание админ меню
    public void createAdminMenu(){
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        keyboardMarkup.setSelective(true);
        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("Добавить канал");
        keyboardRow.add("Удалить канал");

        KeyboardRow keyboardRow1 = new KeyboardRow();
        keyboardRow1.add("Список пользователей");

        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow2.add("Проверить запросы на выплаты");

        rowList.add(keyboardRow);
        rowList.add(keyboardRow1);
        rowList.add(keyboardRow2);

        keyboardMarkup.setKeyboard(rowList);
        log.info("Создали adminMenu");
    }

    // проверка существования юзера
    public User existUser(int userId){
        if (userService.userExist(userId)){
            User user = userService.findById(userId);
            log.info("Юзера достали из бд id: {}", userId);
            return user;
        }
        else {
            User user = new User(userId);
            userService.update(user);
            log.info("Юзера создали id: {}", userId);
            return user;
        }
    }

    public SendMessage cancel(User user){
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(user.getId()));
        user.setPosition("back");
        userService.update(user);
        return messageBuilder.line("Операция отменена").build();
    }
}
