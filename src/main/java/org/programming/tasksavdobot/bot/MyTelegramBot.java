package org.programming.tasksavdobot.bot;


import org.programming.tasksavdobot.confg.BotConfig;
import org.programming.tasksavdobot.service.ProfileService;
import org.programming.tasksavdobot.controller.UpdateResource;
import org.programming.tasksavdobot.controller.AdminResource;
import org.programming.tasksavdobot.service.UserStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final ProfileService profileService;
    private final AdminResource adminResource;
    private final UpdateResource updateController;


    @Autowired
    public MyTelegramBot(BotConfig botConfig, ProfileService profileService, AdminResource adminController, UpdateResource updateController) {

        this.botConfig = botConfig;
        this.profileService = profileService;
        this.adminResource = adminController;
        this.updateController = updateController;
    }

    @Bean(name = "sending")
    CommandLineRunner commandLineRunner(UserStepService sendingService) {
        return args -> {
            sendingService.setTelegramBot(this);

        };
    }

    @Override
    public void onUpdateReceived(Update update) {


        if (update == null) {
            System.out.println("Update is null");
            return;
        }

        Message message = update.getMessage();
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = null;
        String text = null;

        if (message != null) {
            chatId = message.getChatId();
            text = message.getText();


        } else if (callbackQuery != null) {
            chatId = callbackQuery.getMessage().getChatId();
            text = callbackQuery.getData();
        }

        if (chatId == null) {
            System.out.println("Chat ID topilmadi!");
            return;
        }

        if (profileService.isAdmin(chatId)) {
            adminResource.handle(update);
        } else if (profileService.existsByChatId(chatId)) {
            updateController.handle(update);
        } else {
            profileService.save(update);
        }
    }


    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}
