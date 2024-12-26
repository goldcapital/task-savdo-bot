package org.programming.tasksavdobot.service;


import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programming.tasksavdobot.domen.ProfileEntity;
import org.programming.tasksavdobot.enums.*;
import org.programming.tasksavdobot.reposetory.ProfileRepository;
import org.programming.tasksavdobot.service.step.StepService;
import org.programming.tasksavdobot.util.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileService {
    private Map<Long, ProfileEntity> userData = new HashMap<>();
    private Map<Long, String> userState = new HashMap<>();

    private Map<Long, Integer> longStringMap = new HashMap<>();

    private final StepService stepService;

    private final ProfileRepository profileRepository;


    public void save(Update update) {

        if (update == null) {
            System.out.println("Update is null");
            return;
        }

        Message message = update.getMessage();
        CallbackQuery callbackQuery = update.getCallbackQuery();


        Long chatId = null;
        String text = null;

        if (message != null && message.hasText()) {
            chatId = message.getChatId();
            text = message.getText();

        } else if (callbackQuery != null) {
            chatId = callbackQuery.getMessage().getChatId();
            text = callbackQuery.getData();
        }

        if (message != null && message.hasContact()) {
            chatId = message.getChatId();
            text = message.getContact().getPhoneNumber();
            System.out.println("Kontakt yuborildi: " + message.getContact().getPhoneNumber());
        } else {
            System.out.println("Kontakt yuborilmadi.");

        }

        if (chatId == null || text == null) {
            return;
        }


        if (!userData.containsKey(chatId)) {
            ProfileEntity entity = new ProfileEntity();
            entity.setChatId(chatId);
            userData.put(chatId, entity);
        }
        UserAuthorization state = UserAuthorization.valueOf(userState.getOrDefault(chatId, UserAuthorization.START.getName()));
        switch (state) {

            case START:
                sendLanguageSelectionMenu(chatId);
                userState.put(chatId, UserAuthorization.WAITING_FOR_LANGUAGE.getName());
                log.info("STAR USER AUTHORIZATION {}", chatId);
                break;

            case WAITING_FOR_LANGUAGE:
               userLanguageUpdate(chatId,text);
               userState.put(chatId,UserAuthorization.WAITING_FOR_NAME.getName());
               break;

            case WAITING_FOR_NAME:
                ProfileEntity entity = userData.get(chatId);
                entity.setFullName(text);
                log.info("WAITING_FOR_NAME USER AUTHORIZATION SUCCESS, User ID: {}", chatId);
                SendUniversalMessageUtil.createReplyKeyboardMarkupMessage(chatId,"CONTAKNI YUBORING",Buttons.buttons(Buttons.row(Buttons.button())));
                userState.put(chatId, UserAuthorization.WAITING_FOR_PHONE.getName());

                break;

            case WAITING_FOR_PHONE:

                if (message != null && message.hasContact()) {
                    entity = userData.get(chatId);
                    entity.setPhone(text);
                    entity.setStatus(Status.ACTIVE);
                    entity.setRole(ProfileRole.ROLE_USER);
                    entity.setUsername(message.getFrom().getUserName());
                    entity.setDateTime(LocalDateTime.now());

                    profileRepository.save(entity);

                    log.info("WAITING_FOR_PHONE USER AUTHORIZATION SUCCESS, User ID: {}", chatId);
                    SendUniversalMessageUtil.createTextMessage(chatId, LocalizedMessagesUtil.PHONE_SAVE_MESSAGES[longStringMap.get(chatId)]);
                    stepService.setType(update, String.valueOf(Step.MAIN_MENU), null);
                    handleProfileMainMenu(update);
                    userState.put(chatId, UserAuthorization.START.getName());
                    break;

                } else {
                    SendUniversalMessageUtil.createTextMessage(chatId, LocalizedMessagesUtil.PLEASE_PHONE[longStringMap.get(chatId)]);
                }
            default:
                SendUniversalMessageUtil.createTextMessage(chatId, "Noto'g'ri buyruq.");
                break;
        }
    }

    private void userLanguageUpdate(Long chatId, String text) {
        if (text.startsWith("set_language:")) {
            try {
                int languageId = Integer.parseInt(text.split(":")[1]);

                ProfileEntity entity = userData.get(chatId);
                entity.setLanguage(Language.fromInt(languageId));

                if (!existsByChatId(chatId)) {
                    longStringMap.put(chatId, languageId);
                    SendUniversalMessageUtil.createTextMessage(chatId, LocalizedMessagesUtil.WAITING_FOR_NAME[languageId]);
                    userState.put(chatId, UserAuthorization.WAITING_FOR_NAME.getName());
                    log.info("{} USER AUTHORIZATION SUCCESS, User ID: {}", UserAuthorization.WAITING_FOR_LANGUAGE.getName(), chatId);
                    return;
                }
                profileRepository.updateLanguageByChatId(chatId, String.valueOf(Language.fromInt(Integer.parseInt(text.split(":")[1]))));

            } catch (NumberFormatException e) {
                log.error("WAITING_FOR_NAME USER AUTHORIZATION ERROR, Invalid language ID for user ID: {}, Error: {}", chatId, e.getMessage());
                SendUniversalMessageUtil.createTextMessage(chatId, "Tilni noto‘g‘ri tanladingiz. Qayta tanlang.");
            }
        } else {
            SendUniversalMessageUtil.createTextMessage(chatId, "Tilni tanlash uchun tugmani bosing.");
        }
    }

    public boolean isAdmin(Long userId) {
        return profileRepository.existsByChatIdAndRole(userId, ProfileRole.ROLE_ADMIN);
    }

    public void sendLanguageSelectionMenu(Long chatId) {

        SendUniversalMessageUtil.createKeyboardMessage(chatId, "Tilni tanlang / Выберите язык / Choose language:", InlineButtonUtil.keyboardMarkup(InlineButtonUtil.rowList(
                InlineButtonUtil.row(
                        InlineButtonUtil.button("🇺🇿 O'zbekcha", "set_language:0"),
                        InlineButtonUtil.button("🇬🇧 English", "set_language:1"),
                        InlineButtonUtil.button("🇷🇺 Русский", "set_language:2")
                ))
        ));


    }


    public void setUserLanguage(Long chatId, String language) {
        Optional<ProfileEntity> optional = profileRepository.findByChatId(chatId);
        if (optional.isPresent()) {
            ProfileEntity profileEntity = optional.get();
            profileEntity.setLanguage(Language.valueOf(language));
            profileRepository.save(profileEntity);
        }

    }

    public void handleProfileMainMenu(Update update) {
        SendUniversalMessageUtil.createKeyboardMessage(getChatId(update),"USER_MENU",InlineButtonUtil.keyboardMarkup(

                InlineButtonUtil.rowList(
                        InlineButtonUtil.row(InlineButtonUtil.button(
                                        ButtonName.USER_MANU[getUserLanguage(getChatId(update))],
                                        "USER_MENU"
                                ),
                                InlineButtonUtil.button(
                                        ButtonName.PRODUCT_GET[getUserLanguage(getChatId(update))],
                                        "PRODUCT_GET")),
                        InlineButtonUtil.row(InlineButtonUtil.button(
                                ButtonName.CHANGE_LANGUAGE[getUserLanguage(getChatId(update))],
                                "CHANGE_LANGUAGE"))
                )));


    }

    private Long getChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        } else if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }
        return null;


    }

    public int getUserLanguage(Long chatId) {

        Optional<ProfileEntity> optional = profileRepository.getLanguageByChatId(chatId);
        return optional.map(profileEntity -> switch (profileEntity.getLanguage()) {
            case UZ -> 0;
            case EN -> 1;
            default -> 2;
        }).orElse(0);
    }

    public boolean existsByChatId(@NonNull Long id) {
        Optional<ProfileEntity> optional = profileRepository.findByChatId(id);
        if (optional.isPresent()) {
            return true;
        }
        return false;
    }
    public String getuserNameById(Long profileId) {

        return profileRepository.findByProfileID(profileId);
    }

    public ProfileEntity getUserChatId(Long chatId) {
        Optional<ProfileEntity> optional = profileRepository.findByChatId(chatId);

        return optional.get();
    }
}
