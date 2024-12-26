package org.programming.tasksavdobot.controller;

import lombok.RequiredArgsConstructor;
import org.programming.tasksavdobot.enums.Language;
import org.programming.tasksavdobot.enums.Step;
import org.programming.tasksavdobot.service.ProductServices;
import org.programming.tasksavdobot.service.ProfileService;
import org.programming.tasksavdobot.service.step.StepService;
import org.programming.tasksavdobot.util.ButtonName;
import org.programming.tasksavdobot.util.SendUniversalMessageUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UpdateResource {
    public final StepService stepService;
    public final ProfileService profileService;
    public final ProductServices productServices;

    public void handle(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (message.hasText()) {
                if (message.getText().equals("/start") || stepService.getType(update).getStepName() == null
                        || stepService.getType(update).getStepName().equals(String.valueOf(Step.MAIN_MENU))
                        || message.getText().equals(String.valueOf(Step.MAIN_MENU))) {
                    profileService.handleProfileMainMenu(update);
                    stepService.setType(update, String.valueOf(Step.MAIN_MENU), null);


                } else if (message.getText().equals(ButtonName.menu[profileService.getUserLanguage(message.getChatId())])) {
                    profileService.handleProfileMainMenu(update);
                    stepService.setType(update, String.valueOf(Step.MAIN_MENU), null);
                    SendUniversalMessageUtil.deleteMessage(message.getChatId(), message.getMessageId());
                }
            }
            switch (stepService.getType(update).getStepName()) {
                case "PRODUCT":
                    productServices.handle(update);
                    break;
                case "USER_MENU":
                    try {
                        productServices.handleUserMenu(update);
                    } catch (TelegramApiException | IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;

            }

        } else if (update.hasCallbackQuery()) {

            Message message = (Message) update.getCallbackQuery().getMessage();
            String text = update.getCallbackQuery().getData();

            if (stepService.getType(update).getStepName().equals(String.valueOf(Step.MAIN_MENU))) {
                System.out.println(text);
                stepService.setType(update, text, null);
            }

            switch (stepService.getType(update).getStepName()) {

                case "PRODUCT_GET":
                    productServices.handle(update);
                    SendUniversalMessageUtil.deleteMessage(update.getCallbackQuery().getMessage().getChatId(), message.getMessageId());
                    stepService.setType(update, String.valueOf(Step.PRODUCT), null);
                    break;

                case "USER_MENU":
                    try {
                        productServices.handleUserMenu(update);
                        SendUniversalMessageUtil.deleteMessage(message.getChatId(), message.getMessageId());
                    } catch (TelegramApiException | IOException e) {
                        throw new RuntimeException(e);
                    }
                    stepService.setType(update, String.valueOf(Step.USER_MENU), null);
                    break;

                case "CHANGE_LANGUAGE":
                    if (text.startsWith("set_language:")) {
                        int language = Integer.parseInt(text.split(":")[1]);
                        profileService.setUserLanguage(update.getCallbackQuery().getMessage().getChatId(), String.valueOf(Language.fromInt(language)));
                        stepService.setType(update, String.valueOf(Step.MAIN_MENU), null);
                        profileService.handleProfileMainMenu(update);
                        SendUniversalMessageUtil.deleteMessage(message.getChatId(), message.getMessageId());

                    } else {
                        profileService.sendLanguageSelectionMenu(update.getCallbackQuery().getMessage().getChatId());
                        SendUniversalMessageUtil.deleteMessage(message.getChatId(), message.getMessageId());
                    }
                    break;
                default:
                    SendUniversalMessageUtil.createTextMessage(update.getCallbackQuery().getMessage().getChatId(), "Noto'g'ri buyruq kiritildi.");
                    break;
            }
        }
    }


}