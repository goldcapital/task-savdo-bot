package org.programming.tasksavdobot.service;

import lombok.RequiredArgsConstructor;
import org.programming.tasksavdobot.domen.AttachEntity;
import org.programming.tasksavdobot.exp.AppBadException;
import org.programming.tasksavdobot.reposetory.AttachRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AttachService {

    private final AttachRepository attachRepository;
    private final UserStepService userStepService;

    @Value("${telegram.bot.token}")
    private String botToken;


    public String savePhotoToSystem(String fileId) throws IOException, TelegramApiException {

        try (InputStream inputStream = downloadFileAsInputStream(fileId)) {
            BufferedImage image = ImageIO.read(inputStream);
            String filePath = "attaches/" + UUID.randomUUID().toString() + ".jpg";
            Path path = Paths.get(filePath);

            Files.createDirectories(path.getParent());
            try (FileOutputStream outputStream = new FileOutputStream(path.toFile())) {
                ImageIO.write(image, "jpg", outputStream);
                saveProductInfo(filePath);
            }

            return path.toString();
        }
    }

    private InputStream downloadFileAsInputStream(String fileId) throws IOException, TelegramApiException {
        GetFile getFileMethod = new GetFile(fileId);
        File fileObj = userStepService.execute(getFileMethod);
        String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + fileObj.getFilePath();
        return new URL(fileUrl).openStream();
    }

    public void saveProductInfo(String filePath) {
        try {
            AttachEntity entity = new AttachEntity();
            entity.setSize(0L);
            entity.setExtension("jpg");
            entity.setOriginalName("Mahsulot Rasm");
            entity.setCreatedData(LocalDateTime.now());
            entity.setId(UUID.randomUUID().toString());
            entity.setPath(filePath);

            attachRepository.save(entity);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<AttachEntity> get(String id) {
        return Optional.ofNullable(attachRepository.findById(id).orElseThrow(() -> {
            try {
                throw new AppBadException("File not found");
            } catch (AppBadException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    }

