package org.programming.tasksavdobot.util;


import org.programming.tasksavdobot.service.UserStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Component
public class SendUniversalMessageUtil {
    private static ApplicationContext context;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }


    public static UserStepService getUserStepService() {
        return context.getBean(UserStepService.class);
    }


    public static void createTextMessage(Long chatId, String text) {
        UserStepService userStepService = getUserStepService();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        userStepService.sendMessage(sendMessage);

    }
    public static void deleteMessage(Long chatId, Integer messageId) {
        UserStepService userStepService=getUserStepService();
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setMessageId(messageId);
            deleteMessage.setChatId(chatId);
           userStepService .sendMessage(deleteMessage);
        }


    public static void createKeyboardMessage(Long userId, String text, InlineKeyboardMarkup viewCategory) {
        UserStepService userStepService = getUserStepService();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(userId);
        sendMessage.setReplyMarkup(viewCategory);
        userStepService.sendMessage(sendMessage);
    }

    public static void createReplyKeyboardMarkupMessage(Long id, String text, ReplyKeyboardMarkup markup) {
        if (text == null || text.trim().isEmpty()) {
            System.err.println("Text parameter can't be empty");
            return;
        }
        if (markup == null) {
            System.err.println("Markup is null");
            return;
        }
        UserStepService userStepService = getUserStepService();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);
        sendMessage.setText(text);

        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markup);
        userStepService.sendMessage(sendMessage);

    }
}