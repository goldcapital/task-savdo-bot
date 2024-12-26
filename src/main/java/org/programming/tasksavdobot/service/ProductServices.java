package org.programming.tasksavdobot.service;

import lombok.RequiredArgsConstructor;
import org.programming.tasksavdobot.domen.ProductEntity;
import org.programming.tasksavdobot.domen.ProfileEntity;
import org.programming.tasksavdobot.dto.ProfileDTO;
import org.programming.tasksavdobot.reposetory.ProductRepository;
import org.programming.tasksavdobot.service.step.StepService;
import org.programming.tasksavdobot.util.ButtonName;
import org.programming.tasksavdobot.util.Buttons;
import org.programming.tasksavdobot.util.InlineButtonUtil;
import org.programming.tasksavdobot.util.SendUniversalMessageUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServices {
    private final ProductRepository productRepository;
    private final AttachService attachService;
    private final UserStepService userStepService;

    private final StepService stepService;


    private final List<ProfileDTO> usersList = new ArrayList<>();
    private final ProfileService profileService;


    public void handle(Update update) {

        if (update.hasMessage()) {

            Message message = update.getMessage();
            if (message.hasText()) {

                String text = message.getText();
                Long chatId = message.getChatId();


                Optional<ProductEntity> product = productRepository.findById(UUID.fromString(text));
                if (product.isPresent()) {
                    sendProducts(chatId, product.get());
                } else {
                    SendUniversalMessageUtil.createTextMessage(chatId, "TOPILMADI");
                }
                SendUniversalMessageUtil.createTextMessage(chatId, "ILTIMOS PRODUC ID KIRITING");

            }

        } else if (update.hasCallbackQuery()) {
            Message message = (Message) update.getCallbackQuery().getMessage();
            SendUniversalMessageUtil.createReplyKeyboardMarkupMessage(message.getChatId(), "PRODUCT IDSINI KIRITING", Buttons.buttons(Buttons.row(Buttons.button(ButtonName.menu[profileService.getUserLanguage(message.getChatId())]))));
        }

    }

    public void handleUserMenu(Update update) throws TelegramApiException, IOException {

        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String text = message.getText();
                Long chatId = message.getChatId();
                var step = saveUser(chatId);

                if (text.equals("ADD") || step.getWalking() != null) {
                    if (step.getCurrentStep() == null) {
                        SendUniversalMessageUtil.createTextMessage(chatId, "Iltimos, mahsulot rasmini yuklang:");
                        step.setCurrentStep("awaiting_image");
                        step.setWalking("ADD");
                    }

                    switch (step.getCurrentStep()) {

                        case "awaiting_product_name" -> {
                            step.setProductName(text);
                            SendUniversalMessageUtil.createTextMessage(chatId, "Mahsulot turi qanday? Iltimos, kiriting:");
                            step.setCurrentStep("awaiting_product_type");
                        }
                        case "awaiting_product_type" -> {
                            step.setProductType(text);
                            SendUniversalMessageUtil.createTextMessage(chatId, "Narxini kiriting");
                            step.setCurrentStep("awaiting_additional_info");
                        }
                        case "awaiting_additional_info" -> {

                            try {
                                step.setPrice(Double.parseDouble(text));
                                SendUniversalMessageUtil.createTextMessage(chatId, "Qo‘shimcha ma’lumotlarni kiriting:");
                                step.setCurrentStep("awaiting_price");

                            } catch (NumberFormatException e) {
                                SendUniversalMessageUtil.createTextMessage(chatId, "Iltimos, narxni raqam ko‘rinishida kiriting.");
                            }
                        }
                        case "awaiting_price" -> {
                            step.setAdditionalInfo(text);
                            UUID productId = saveProductToDatabase(step, update);
                            sendMessageToChannel(step, message.getFrom().getUserName(), productId);
                            SendUniversalMessageUtil.createTextMessage(chatId, "Mahsulot muvaffaqiyatli saqlandi!");
                            step.reset();

                            stepService.setType(update, "ASOSIY_MENUE", null);
                        }

                    }

                }

            } else if (message.hasPhoto()) {
                Long chatId = message.getChatId();
                var step = saveUser(chatId);
                System.out.println(message.getPhoto());

                if (step.getCurrentStep().equals("awaiting_image")) {
                    String fileId = message.getPhoto().get(message.getPhoto().size() - 1).getFileId();
                    String filePath = attachService.savePhotoToSystem(fileId);
                    step.setImagePath(filePath);

                    SendUniversalMessageUtil.createTextMessage(chatId, "Rasm qabul qilindi. Endi mahsulot nomini kiriting:");
                    step.setCurrentStep("awaiting_product_name");
                }
            }

        } else if (update.hasCallbackQuery()) {
            Message message = (Message) update.getCallbackQuery().getMessage();
            SendUniversalMessageUtil.createReplyKeyboardMarkupMessage(message.getChatId(), "ADD BOSING", Buttons.buttons(Buttons.row(
                    Buttons.button("ADD"),
                    Buttons.button(ButtonName.menu[profileService.getUserLanguage(message.getChatId())]))));


        }


    }


    private UUID saveProductToDatabase(ProfileDTO step, Update update) {

        ProfileEntity profile = profileService.getUserChatId(update.getMessage().getChatId());

        if (update.getMessage() != null && update.getMessage().getFrom() != null) {
            String userName = update.getMessage().getFrom().getUserName();

            profile.setFullName(userName);

        }

        ProductEntity product = new ProductEntity();

        product.setProfileId(step.getChatId());
        product.setProfileEntity(profile);
        product.setImageUrl(step.getImagePath());
        product.setNameUz(step.getProductName());
        product.setNameEn(step.getProductName());
        product.setNameRu(step.getProductName());
        product.setType(step.getProductType());
        product.setPrice(step.getPrice());
        product.setAdditionalInfo(step.getAdditionalInfo());

        ProductEntity savedProduct = productRepository.save(product);

        return savedProduct.getId();
    }


    private void sendMessageToChannel(ProfileDTO step, String userName, UUID productId) throws TelegramApiException {
        Optional<ProductEntity> product = productRepository.findById(productId);
        String channelId = "@top_savdo_bot_uz";
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(channelId);
        sendPhoto.setPhoto(new InputFile(new File(step.getImagePath())));
        sendPhoto.setCaption(String.format(
                "📦 *Mahsulot ma’lumotlari:*\n\n" +
                        "🔹 *Nomi:* %s\n" +
                        "🔹 *Turi:* %s\n" +
                        "🔹 *PRODUCT ID:* %s\n" +
                        "🔹 *Qo‘shimcha ma’lumot:* %s\n" +
                        "🔹 *Narxi:* %.2f so‘m\n" +
                        "👤 *Qo‘shgan foydalanuvchi:* @%s",
                getProductName(step.getChatId(), product.get()),
                step.getProductType(),
                productId,
                step.getAdditionalInfo(),
                step.getPrice() != null ? step.getPrice() : 0.0,
                userName
        ));
        sendPhoto.setParseMode("Markdown");
        userStepService.sendMessage(sendPhoto);
    }

    private void sendProducts(Long chatId, ProductEntity product) {

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);


        if (product.getImageUrl().startsWith("http")) {
            sendPhoto.setPhoto(new InputFile(product.getImageUrl()));
        } else {
            File file = new File(product.getImageUrl());
            if (!file.exists() || !file.isFile()) {
                throw new IllegalArgumentException("Rasm topilmadi: " + product.getImageUrl());
            }
            sendPhoto.setPhoto(new InputFile(file));
        }


        String productDetails = String.format(
                "📦 *Mahsulot ma’lumotlari:*\n\n" +
                        "🔹 *Nomi:* %s\n" +
                        "🔹 *Turi:* %s\n" +
                        "🔹 *PRODUCT ID:* %s\n" +
                        "🔹 *Qo‘shimcha ma’lumot:* %s\n" +
                        "🔹 *Narxi:* %.2f so‘m\n" +
                        "👤 *Qo‘shgan foydalanuvchi:* @%s",
                getProductName(chatId, product),
                product.getType(),
                product.getId(),
                product.getAdditionalInfo(),
                product.getPrice() != null ? product.getPrice() : 0.0,
                profileService.getuserNameById(product.getProfileId())
        );
        sendPhoto.setCaption(productDetails);
        sendPhoto.setParseMode("Markdown");
        sendPhoto.setReplyMarkup(
                InlineButtonUtil.keyboardMarkup(InlineButtonUtil.rowList(
                        InlineButtonUtil.row(InlineButtonUtil.button("🛒 Savatchaga qo'shish", "add_to_cart:" + product.getId())))));

        userStepService.sendMessage(sendPhoto);
    }

    public ProfileDTO saveUser(Long chatId) {
        return usersList.stream()
                .filter(users -> users.getChatId().equals(chatId))
                .findFirst()
                .orElseGet(() -> {
                    var users = new ProfileDTO();
                    users.setChatId(chatId);
                    usersList.add(users);
                    return users;
                });
    }

    private String getProductName(Long chatId, ProductEntity product) {
        if (chatId == null || product == null) {
            System.out.println("Chat ID yoki ProductEntity null!");
            return "Ma'lumot mavjud emas";
        }
        return switch (profileService.getUserLanguage(chatId)) {
            case 1 -> product.getNameEn();
            case 2 -> product.getNameRu();
            default -> product.getNameUz();
        };
    }

    private Long chatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getMessage().getChatId();
        } else
            return update.getMessage().getChatId();

    }


}


