package com.nikita.botter.bot;

import com.nikita.botter.bot.builder.MessageBuilder;
import com.nikita.botter.model.*;
import com.nikita.botter.service.*;
import com.nikita.botter.util.TelegramUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    @Value("${bot.adminUrl}")
    private String adminUrl;

    private final ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

    private final UserService userService;
    private final ChannelService channelService;
    private final PaymentService paymentService;
    private final ChannelCheckService channelCheckService;
    private final BonusChannelService bonusChannelService;

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
        Usr user;
        SendMessage sendMessage;
        int userId = 0;
        String userIdString;

        // если приходит текст
        if (isMessageWithText(update)){
            userId = update.getMessage().getFrom().getId();
            userIdString = String.valueOf(userId);
            command = TelegramUtil.extractCommand(update.getMessage().getText());
            txt = update.getMessage().getText().split(" ");


            // проверка существования юзера
             user = existUser(userId);

             if (!"back".equalsIgnoreCase(user.getPosition())){
                 executeWithExceptionCheck(fabricPositions(update, user.getPosition()));
             }

           // проверка пришел ли юзер через реф ссылку
           if ("/start".equalsIgnoreCase(command) && txt.length > 1){
               if (user.getReferId() == 0){
                   String refer = command.split(" ")[1];
                   int referId = 0;
                   try {
                       referId = Integer.parseInt(refer);
                   }catch (Exception e){
                       log.error("Ошибка при получении реферала: {} ", refer);
                   }
                   user.setReferId(referId);

                   Usr ref = existUser(referId);
                   ref.setMoney(ref.getMoney() + 3);
                   ref.setMoneyFromPartners(ref.getMoneyFromPartners() + 3);
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
                  MessageBuilder messageBuilder = MessageBuilder.create(user);
                  messageBuilder.line("Добро пожаловать");
                  sendMessage = messageBuilder.build();
                  sendMessage.setReplyMarkup(keyboardMarkup);
                  log.info("Приветственное сообщение юзеру {}", userIdString);
                  executeWithExceptionCheck(sendMessage);
              }
           }
           // информация в меню
           else if ("\uD83D\uDCC6 Информация".equalsIgnoreCase(command)){
               executeWithExceptionCheck(info(update));
           }
           // партнерам в меню
           else if ("\uD83D\uDC54 Партнерам".equalsIgnoreCase(command)){
               executeWithExceptionCheck(partners(update));
           }
           // баланс в меню
           else if ("\uD83D\uDCB5 Баланс".equalsIgnoreCase(command)){
               executeWithExceptionCheck(balance(update));
           }
           // бонус в меню
           else if ("\uD83D\uDD06 Бонус".equalsIgnoreCase(command)){
               executeWithExceptionCheck(bonus(update));
           }
           else if ("➕ Подписки".equalsIgnoreCase(command)){
               executeWithExceptionCheck(joined(update));
           }
           // войти в админ меню
           else if ("админМеню".equalsIgnoreCase(command)){
               if (userIdString.equalsIgnoreCase(adminId)){
                   createAdminMenu();
                   MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
                   messageBuilder.line("Вход в админ панель");
                   sendMessage = messageBuilder.build();
                   sendMessage.setReplyMarkup(keyboardMarkup);
                   log.info("Вход в админ панель юзером {}", userIdString);
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
           // проверка запросов на выплаты
           else if ("Проверить запросы на выплаты".equalsIgnoreCase(command)){
               if (userIdString.equalsIgnoreCase(adminId)){
                   sendMessage = checkPayments(update);
                   executeWithExceptionCheck(sendMessage);
               }
               else {
                   MessageBuilder messageBuilder = MessageBuilder.create(user);
                   messageBuilder.line("Вы не админ");
                   executeWithExceptionCheck(messageBuilder.build());
               }
           }
           // добавить бонусный канал
           else if ("Добавить бонусный канал".equalsIgnoreCase(command)){
               if (userIdString.equalsIgnoreCase(adminId)){
                    executeWithExceptionCheck(addBonusChannel(update));
               }
               else {
                   MessageBuilder messageBuilder = MessageBuilder.create(user);
                   messageBuilder.line("Вы не админ");
                   executeWithExceptionCheck(messageBuilder.build());
               }
           }
           // удалить бонусный канал
           else if ("Удалить бонусный канал".equalsIgnoreCase(command)){
               if (userIdString.equalsIgnoreCase(adminId)){
                   executeWithExceptionCheck(deleteBonusChannel(update));
               }
               else {
                   MessageBuilder messageBuilder = MessageBuilder.create(user);
                   messageBuilder.line("Вы не админ");
                   executeWithExceptionCheck(messageBuilder.build());
               }
           }
           // список бонусных каналов
           else if ("Бонусные каналы".equalsIgnoreCase(command)){
               if (userIdString.equalsIgnoreCase(adminId)){
                   executeWithExceptionCheck(findBonusChannel(update));
               }
               else {
                   MessageBuilder messageBuilder = MessageBuilder.create(user);
                   messageBuilder.line("Вы не админ");
                   executeWithExceptionCheck(messageBuilder.build());
               }
           }
           // выйти в обычное меню
           else if ("Выйти".equalsIgnoreCase(command)){
               if (userIdString.equalsIgnoreCase(adminId)){
                   sendMessage = MessageBuilder.create(userIdString).line("Вы вышли в главное меню").build();
                   createUserMenu();
                   sendMessage.setReplyMarkup(keyboardMarkup);
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
           // список каналов
           else if ("Список каналов".equalsIgnoreCase(command)){
               if (userIdString.equalsIgnoreCase(adminId)){
                   sendMessage = allChannels(update);
                   executeWithExceptionCheck(sendMessage);
               }
               else {
                   MessageBuilder messageBuilder = MessageBuilder.create(user);
                   messageBuilder.line("Вы не админ");
                   executeWithExceptionCheck(messageBuilder.build());
               }
           }
           // список каналов
           else if ("Список пользователей".equalsIgnoreCase(command)){
               if (userIdString.equalsIgnoreCase(adminId)){
                   sendMessage = allUsers(update);
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
            if ("/startContinue".equalsIgnoreCase(command)){
                executeWithExceptionCheck(checkStartImpl(user));
            }
            // отмена действия
            else if ("/cancel".equalsIgnoreCase(command)){
                executeWithExceptionCheck(cancel(user));
            }
            // установка киви юзером
            else if ("/setQiwi".equalsIgnoreCase(command)){
                executeWithExceptionCheck(setQiwi(update));
            }
            // ввод суммы на вывод
            else if ("/withMoney".equalsIgnoreCase(command)){
                executeWithExceptionCheck(withMoney(update));
            }
            // ежедневный бонус
            else if ("/dailyBonus".equalsIgnoreCase(command)){
                executeWithExceptionCheck(dailyBonus(update));
            }
            else if ("/getGift".equalsIgnoreCase(TelegramUtil.extractCommand(command))){
                executeWithExceptionCheck(getGift(update));
            }
            // вывод успешен
            else if ("/success".equalsIgnoreCase(TelegramUtil.extractCommand(command))){
                success(update).forEach(this::executeWithExceptionCheck);
            }
            // вывод отменен
            else if ("/notSuc".equalsIgnoreCase(TelegramUtil.extractCommand(command))){
                notSuccess(update).forEach(this::executeWithExceptionCheck);
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
      else if ("киви".equalsIgnoreCase(position)){
          executeWithExceptionCheck(setQiwiImpl(update));
      }
      else if ("вывод".equalsIgnoreCase(position)){
          executeWithExceptionCheck(withMoneyImpl(update));
      }
      else if ("addBonus".equalsIgnoreCase(position)){
          executeWithExceptionCheck(addBonusChannelImpl(update));
      }
      else if ("deleteBonus".equalsIgnoreCase(position)){
          executeWithExceptionCheck(deleteBonusChannelImpl(update));
      }
      return new SendMessage();
    }

    private void executeWithExceptionCheck(SendMessage sendMessage){
        try {
            log.info("Юзер {} отправил смс {}", sendMessage.getChatId(), sendMessage.getText());
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.getStackTrace();
        }
    }

    // проверка на текст
    private boolean isMessageWithText(Update update) {
        return update != null && !update.hasCallbackQuery() && update.hasMessage() && update.getMessage().hasText();
    }

    // проверка на подписку в стартовых каналах
    private SendMessage checkUserInStartChannels(Usr user){
         int userId = user.getId();
         List<String> statuses;

         List<Channel> channelsStart = channelService.findAllByStart(true);

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

    public SendMessage checkStartImpl(Usr user){
        int userId = user.getId();

        // заполняем из бд каналами
        List<Channel> channelsStart = channelService.findAllByStart(true);
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
        Usr user = userService.findById(userId);
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
        Usr user = userService.findById(userId);
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
        Usr user = userService.findById(userId);
        user.setPosition("back");
        userService.update(user);

        log.info("Юзер {} удалил канал", userId);

        return messageBuilder
                .line("Канал удален")
                .build();
    }

    public SendMessage addChannelImpl(Update update){
        int userId = update.getMessage().getFrom().getId();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        Usr user = userService.findById(userId);

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
        keyboardRow1.add("Список каналов");

        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow2.add("Проверить запросы на выплаты");
        keyboardRow2.add("Бонусные каналы");

        KeyboardRow keyboardRow3 = new KeyboardRow();
        keyboardRow3.add("Добавить бонусный канал");
        keyboardRow3.add("Удалить бонусный канал");

        KeyboardRow keyboardRow4 = new KeyboardRow();
        keyboardRow4.add("Выйти");

        rowList.add(keyboardRow);
        rowList.add(keyboardRow1);
        rowList.add(keyboardRow2);
        rowList.add(keyboardRow3);
        rowList.add(keyboardRow4);

        keyboardMarkup.setKeyboard(rowList);
        log.info("Создали adminMenu");
    }

    // проверка существования юзера
    public Usr existUser(int userId){
            Usr user = userService.findById(userId);
            log.info("Юзера создали или достали id: {}", userId);
            return user;
    }

    public SendMessage cancel(Usr user){
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(user.getId()));
        user.setPosition("back");
        userService.update(user);
        return messageBuilder.line("Операция отменена").build();
    }

    public SendMessage allChannels(Update update){
        int userId = update.getMessage().getFrom().getId();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        List<Channel> channels = channelService.getChannels();
        if (channels.isEmpty()){
            return messageBuilder.line("Каналов нет").build();
        }
        channels.forEach(e ->{
            messageBuilder
                    .line("\nid: " + e.getId() + " | url: " + e.getUrl() + " | price: " + e.getPrice() + " | start " + e.isStart());
        });
        SendMessage sendMessage = messageBuilder.build();
        sendMessage.disableWebPagePreview();
        return sendMessage;
    }

    public SendMessage allUsers(Update update){
        int userId = update.getMessage().getFrom().getId();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        List<Usr> users = userService.findAll();
        users.forEach(e ->{
            messageBuilder
                    .line("\nid: " + e.getId() + " | money: " + e.getMoney() + " | qiwi: " + e.getQiwi() + " | position: " + e.getPosition());
        });
        SendMessage sendMessage = messageBuilder.build();
        sendMessage.disableWebPagePreview();
        return sendMessage;
    }

    public SendMessage info(Update update){
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(update.getMessage().getFrom().getId()));
        log.info("Юзер {} вызвал Партнерам", update.getMessage().getFrom().getId());
        messageBuilder
                .line("\uD83C\uDF93 Попал в бота, но не знаешь, что делать?\n" +
                        "\n" +
                        "✅ Бот является честным, и реально выплачивает своим пользователям!\n" +
                        "\n" +
                        "\uD83C\uDF34 Начни получать доход только в проверенных ботах, от нашей денежной компании.\n" +
                        "\n" +
                        "Связь с админом: @" + adminUrl);
        return messageBuilder.build();
    }

    public SendMessage partners(Update update){
        Usr user = userService.findById(update.getMessage().getFrom().getId());
        log.info("Юзер {} вызвал Партнерам", user.getId());
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(update.getMessage().getFrom().getId()));
        messageBuilder
                .line("\uD83D\uDC54 Приглашая друзей или просто людей в робота, по вашей реферальной ссылке вы увеличиваете ваш доход:\n" +
                        "\n" +
                        "\uD83D\uDCB0 За приглашение друга: 3.0₽\n" +
                        "\n" +
                        "\uD83D\uDCA1 Ваша ссылка: "+user.getRefUrl()+"\n" +
                        "\n" +
                        "\uD83D\uDC8E Всего заработано: "+user.getMoneyFromPartners()+"₽");
        SendMessage sendMessage = messageBuilder.build();
        sendMessage.disableWebPagePreview();
        return sendMessage;
    }

    public SendMessage balance(Update update){
        int userId = update.getMessage().getFrom().getId();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        Usr user = userService.findById(userId);
        messageBuilder.line("\uD83D\uDC54 Ваш кабинет\n" +
                "⚙️ Ваш ID: "+userId+"\n" +
                "\n" +
                "\uD83D\uDCB0 Ваш личный баланс: "+user.getMoney()+"₽\n" +
                "\uD83D\uDCB2 Заработано с партнеров: "+user.getMoneyFromPartners()+"₽\n" +
                "\n" +
                "\uD83D\uDC65 Всего партнеров: "+userService.countPartners(userId)+" человек \n" +
                "\uD83D\uDCB0 Ваш Qiwi: " + user.getQiwi())
                .row()
                .button("\uD83D\uDCB0 Ваши реквизиты", "/setQiwi")
                .row()
                .button("\uD83D\uDCB0 Вывести деньги", "/withMoney");
        return messageBuilder.build();
    }

    public SendMessage setQiwi(Update update){
        int userId = update.getCallbackQuery().getFrom().getId();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        Usr user = userService.findById(userId);
        user.setPosition("киви");
        userService.update(user);

        messageBuilder
                .line("Внимание! Проверяйте свой номер, чтобы избежать неполадок при выводе\n")
                .line("Укажите ваш номер qiwi:")
                .row()
                .button("Отмена", "/cancel");
        return messageBuilder.build();
    }

    public SendMessage setQiwiImpl(Update update){
        int userId = update.getMessage().getFrom().getId();
        String qiwi = update.getMessage().getText();
        Usr user = userService.findById(userId);
        MessageBuilder messageBuilder = MessageBuilder.create(user);

        user.setQiwi(qiwi);
        user.setPosition("back");
        userService.update(user);

        log.info("Юзер {} установил qiwi", userId);
        return messageBuilder.line("Номер " + qiwi + " установлен").build();
    }

    public SendMessage withMoney(Update update){
        int userId = update.getCallbackQuery().getFrom().getId();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        Usr user = userService.findById(userId);
        user.setPosition("вывод");
        userService.update(user);

        messageBuilder
                .line("Внимание! Проверяйте, что у вас установлены реквизиты\n")
                .line("Укажите сумму для вывода:")
                .row()
                .button("Отмена", "/cancel");
        return messageBuilder.build();
    }

    public SendMessage withMoneyImpl(Update update){
        int userId = update.getMessage().getFrom().getId();
        String sum = update.getMessage().getText();
        Usr user = userService.findById(userId);
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        double money = 0;

        try {
            money = Double.parseDouble(sum);
        }catch (Exception e){
            log.error("Юзер {} ввел неправильную сумму", userId);
            return messageBuilder.line("Вы ввели не число").build();
        }

        if (money < 40){
            return messageBuilder.line("Вывод от 40 рублей").build();
        }
        if (money > user.getMoney()){
            return messageBuilder.line("У вас недостаточно средств для вывода").build();
        }
        if (user.getQiwi().equalsIgnoreCase("Qiwi не установлен")){
            return messageBuilder.line("Вы не установили Qiwi кошелек").build();
        }

        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setSuccessful(false);
        payment.setSum(money);
        payment.setDate(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
        payment.setTimePayment(new SimpleDateFormat("HH:mm").format(new Date()));

        user.setMoney(user.getMoney() - money);
        user.setPosition("back");
        userService.update(user);
        paymentService.update(payment);

        log.info("Юзер {} запросил вывод", userId);
        return messageBuilder.line("Запрос на вывод создан, обработка 1-3 дня").build();
    }

    public SendMessage checkPayments(Update update){
        int userId = update.getMessage().getFrom().getId();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        List<Payment> payments = paymentService.findAllByNotSuccessful();
        if (payments.isEmpty()){
            return messageBuilder.line("Запросов нет").build();
        }
        for (Payment p : payments){
            Usr user = userService.findById(p.getUserId());
            messageBuilder.line("Id: " + p.getId() + " | userId: " + p.getUserId()
                    + " | сумма: " + p.getSum() + " \nQiwi: " + user.getQiwi() + " | Дата: " + p.getDate() + " | Время: " + p.getTimePayment())
                    .row()
                    .button("Выплачено", "/success_" + p.getId())
                    .row()
                    .button("Неверный Qiwi", "/notSuc_" + p.getId());
        }
        return messageBuilder.build();
    }

    public List<SendMessage> success(Update update){
        String text = update.getCallbackQuery().getData().split("_")[1];
        int paymentId = 0;
        List<SendMessage> messages = new ArrayList<>();
        MessageBuilder messageBuilder;

        try {
            paymentId = Integer.parseInt(text);
        }catch (Exception e){
            log.error("Ошибка при парсинге id payment");
        }
        Payment payment = paymentService.findById(paymentId);
        payment.setSuccessful(true);
        int userId = payment.getUserId();
        paymentService.update(payment);

        messageBuilder = MessageBuilder.create(String.valueOf(userId));
        messageBuilder.line("Вам выплачено "+payment.getSum() + " руб.");
        messages.add(messageBuilder.build());

        messageBuilder = MessageBuilder.create(adminId);
        messageBuilder.line("Транзакция "+ payment.getId()+ " выполнена успешно");
        messages.add(messageBuilder.build());

        return messages;
    }

    public List<SendMessage> notSuccess(Update update){
        String text = update.getCallbackQuery().getData().split("_")[1];
        int paymentId = 0;
        List<SendMessage> messages = new ArrayList<>();
        MessageBuilder messageBuilder;

        try {
            paymentId = Integer.parseInt(text);
        }catch (Exception e){
            log.error("Ошибка при парсинге id payment");
        }
        Payment payment = paymentService.findById(paymentId);
        payment.setSuccessful(true);
        int userId = payment.getUserId();
        paymentService.update(payment);

        Usr usr = userService.findById(userId);
        usr.setMoney(usr.getMoney() + payment.getSum());
        userService.update(usr);

        messageBuilder = MessageBuilder.create(String.valueOf(userId));
        messageBuilder.line("Вам возвращена сумма "+payment.getSum() + " руб.\n" +
                "Ваш Qiwi неверен, проверьте и создайте запрос вновь");
        messages.add(messageBuilder.build());

        messageBuilder = MessageBuilder.create(adminId);
        messageBuilder.line("Транзакция "+ payment.getId()+ " отменена, деньги возвращены юзеру");
        messages.add(messageBuilder.build());

        return messages;
    }

    public SendMessage joined(Update update){
        int userId = 0;
        if (update.hasMessage()){
            userId = update.getMessage().getFrom().getId();
        }else {
            userId = update.getCallbackQuery().getFrom().getId();
        }

        List<Channel> channels = channelService.findAllByStart(false);
        List<ChannelCheck> channelCheck = channelCheckService.findAll(userId);
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));

        for (Channel c : channels){
            if (!join(channelCheck, c)){
                return messageBuilder
                        .line("➕ Подпишись на канал, для получение денежной награды, и просмотри несколько постов, выше по ленте\n" +
                                "\n" +
                                "\uD83D\uDCB0 Награда за подписку: "+ c.getPrice() +"₽")
                        .row()
                        .buttonWithUrl("➕ Подписаться", c.getUrl())
                        .row()
                        .button("\uD83D\uDCB0 Получить награду", "/getGift_" + c.getId())
                        .build();
            }
        }
        return messageBuilder
                .line("Каналов для подписки не найдено")
                .build();
    }

    public SendMessage getGift(Update update){
        int userId = update.getCallbackQuery().getFrom().getId();
        String channelId = update.getCallbackQuery().getData().split("_")[1];
        Usr user = userService.findById(userId);
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        Channel channel = channelService.findById(channelId);

        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(channelId);
        getChatMember.setUserId(userId);
        ChatMember chatMember = null;

        List<ChannelCheck> channelChecks = channelCheckService.findAll(userId);
        try {
            chatMember = execute(getChatMember);
        }catch (TelegramApiException e){

        }
        if ("left".equalsIgnoreCase(chatMember.getStatus())){
            return messageBuilder
                    .line("Вы не подписались!")
                    .build();
        }

        for(ChannelCheck c : channelChecks){
            if (c.getChannelId().equalsIgnoreCase(channel.getId())){
                return messageBuilder
                        .line("Вы ранее получили награду")
                        .build();
            }
        }

        ChannelCheck channelCheck = new ChannelCheck();
        channelCheck.setChannelId(channelId);
        channelCheck.setUserId(userId);
        channelCheckService.update(channelCheck);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(userId));
        sendMessage.setText("Вы получили " + channel.getPrice() + " руб.");
        executeWithExceptionCheck(sendMessage);

        user.setMoney(user.getMoney() + channel.getPrice());
        userService.update(user);

        return joined(update);
    }

    public boolean join(List<ChannelCheck> channelChecks, Channel channel){
        for(ChannelCheck c : channelChecks){
            if (c.getChannelId().equalsIgnoreCase(channel.getId())){
                return true;
            }
        }
        return false;
    }

    public SendMessage bonus(Update update){
        int userId = update.getMessage().getFrom().getId();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));

        List<BonusChannel> bonusChannels = bonusChannelService.findAll();

        messageBuilder
                .line("\uD83D\uDD12 ВАЖНО, ЧТОБЫ СОБРАТЬ следующий бонус, ВАМ нужно ПОДПИСАТЬСЯ на все эти каналы! ⤵️\n" +
                        "\n");
        if (bonusChannels.isEmpty()){
            messageBuilder.line("Бонусных каналов нет\n\n");
        }
        else {
            for (int i = 0; i < bonusChannels.size(); i++) {
                messageBuilder.line(i+1 + ". <a href=\"" + bonusChannels.get(i).getUrl() + "\">Подписаться</a>\uD83D\uDC48\uD83C\uDFFB\n");
            }
        }

                 messageBuilder.line("\n\uD83C\uDF81 ЧТОБЫ ПОЛУЧИТЬ СЛЕДУЮЩИЙ БОНУС - ПОДПИШИСЬ НА ВСЕХ СПОНСОРОВ \uD83D\uDC46\uD83C\uDFFB\n" +
                        "\n" +
                        "\uD83E\uDD1D По вопросам рекламы - @" + adminUrl);

                messageBuilder
                        .row()
                        .button("\uD83C\uDF81 Собрать бонус", "/dailyBonus");

        SendMessage sendMessage = messageBuilder.build();
        sendMessage.enableHtml(true);
        sendMessage.disableWebPagePreview();
        return sendMessage;
    }

    public SendMessage dailyBonus(Update update){
        int userId = update.getCallbackQuery().getFrom().getId();
        Usr user = userService.findById(userId);
        MessageBuilder messageBuilder = MessageBuilder.create(user);

        if (user.isBonus()){
            return messageBuilder
                    .line("Вы уже получали бонус за последние 10 часов")
                    .build();
        }
        else {

            user.setMoney(user.getMoney() + 1);
            user.setBonus(true);
            userService.update(user);

            return messageBuilder
                    .line("Вы получили 1 бонусный рубль\n")
                    .line("Приходите через 10 часов")
                    .build();
        }
    }

    public SendMessage addBonusChannel(Update update){
      int userId = update.getMessage().getFrom().getId();
      Usr user = userService.findById(userId);
      user.setPosition("addBonus");
      userService.update(user);
      MessageBuilder messageBuilder = MessageBuilder.create(user);
      messageBuilder.line("Введите url бонусного канала для добавления")
              .row()
              .button("Отмена", "/cancel");
      return messageBuilder.build();
    }

    public SendMessage deleteBonusChannel(Update update){
        int userId = update.getMessage().getFrom().getId();
        Usr user = userService.findById(userId);
        user.setPosition("deleteBonus");
        userService.update(user);
        MessageBuilder messageBuilder = MessageBuilder.create(user);
        messageBuilder.line("Введите id бонусного канала для его удаления")
                .row()
                .button("Отмена", "/cancel");
        return messageBuilder.build();
    }

    public SendMessage addBonusChannelImpl(Update update){
        int userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        Usr user = userService.findById(userId);

        BonusChannel bonusChannel = new BonusChannel();
        bonusChannel.setUrl(text);
        bonusChannelService.update(bonusChannel);
        log.info("Бонусный канал добавлен");

        user.setPosition("back");
        userService.update(user);

        return messageBuilder.line("Бонусный канал добавлен").build();
    }

    public SendMessage deleteBonusChannelImpl(Update update){
        int userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));
        Usr user = userService.findById(userId);

        int bonusId = 0;
        try {
            bonusId = Integer.parseInt(text);
        }catch (Exception e){
            log.error("ошибка при удалении бонусного канала");
        }
        try {
            bonusChannelService.delete(bonusId);
        }catch (Exception e){
            log.error("Канал не удален, его не существует");
        }
        log.info("Бонусный канал удален");

        user.setPosition("back");
        userService.update(user);

        return messageBuilder.line("Бонусный канал удален").build();
    }

    public SendMessage findBonusChannel(Update update){
        int userId = update.getMessage().getFrom().getId();
        List<BonusChannel> bonusChannels = bonusChannelService.findAll();
        MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(userId));

        if (bonusChannels.isEmpty()){
            return messageBuilder.line("Бонусных каналов нет").build();
        }

        bonusChannels.forEach(e ->{
            messageBuilder.line("\nId: " + e.getId() + " | Url: " + e.getUrl());
        });
        SendMessage sendMessage = messageBuilder.build();
        sendMessage.disableWebPagePreview();
        return sendMessage;
    }

    @Scheduled(fixedDelay = 36000000)
    public void bon(){
        List<Usr> users = userService.getAllNotBonus();
        users.forEach(u -> u.setBonus(false));
        userService.updateAll(users);
        users.forEach(u ->{
            MessageBuilder messageBuilder = MessageBuilder.create(String.valueOf(u.getId()));
            executeWithExceptionCheck(messageBuilder
                    .line("Ваш ежедневный бонус обновлен")
                    .build());
        });
    }
}
