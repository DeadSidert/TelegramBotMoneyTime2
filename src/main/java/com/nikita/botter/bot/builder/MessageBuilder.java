package com.nikita.botter.bot.builder;

import com.nikita.botter.model.User;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


import java.util.ArrayList;
import java.util.List;

public final class MessageBuilder {
    @Setter
    private String chatId;
    @Setter
    private String url;

    private final StringBuilder sb = new StringBuilder();
    private final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
    private List<InlineKeyboardButton> row = null;

    private MessageBuilder() {
    }

    public static MessageBuilder create(String chatId) {
        MessageBuilder builder = new MessageBuilder();
        builder.setChatId(chatId);
        return builder;
    }


    public static MessageBuilder create(User user) {
        return create(String.valueOf(user.getId()));
    }


    public MessageBuilder line() {
        sb.append(String.format("%n"));
        return this;
    }

    public MessageBuilder line(String text) {
        sb.append(text);
        return this;
    }

    public MessageBuilder row() {
        addRowToKeyboard();
        row = new ArrayList<>();
        return this;
    }

    // cannot resolve method setCallbackData(java.lang.String)
    public MessageBuilder button(String text, String callbackData) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setCallbackData(callbackData);
        row.add(new InlineKeyboardButton().setText(text).setCallbackData(callbackData));
        return this;
    }
    public MessageBuilder buttonWithUrl(String text, String url) {
        row.add(new InlineKeyboardButton().setText(text).setUrl(url));
        return this;
    }

    public SendMessage build() {
        SendMessage sendMessage = new SendMessage()
                .setChatId(chatId)
                .setText(sb.toString())
                .enableMarkdown(true);
        addRowToKeyboard();

        if (!keyboard.isEmpty()) {
            sendMessage.setReplyMarkup(new InlineKeyboardMarkup().setKeyboard(keyboard));
        }

        return sendMessage;
    }

    private void addRowToKeyboard() {
        if (row != null) {
            keyboard.add(row);
        }
    }
}