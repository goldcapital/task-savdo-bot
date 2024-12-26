package org.programming.tasksavdobot.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.programming.tasksavdobot.bot.MyTelegramBot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Setter
@Service
@Slf4j
public class UserStepService {
    private MyTelegramBot telegramBot;


    public void sendMessage(SendPhoto sendPhoto) {
        try {
            telegramBot.execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendMessage(SendMessage sendMessage) {
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendMessage(DeleteMessage deleteMessage) {
        try {
            telegramBot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public File execute(GetFile getFileMethod) throws TelegramApiException {
        return telegramBot.execute(getFileMethod);
    }

}
