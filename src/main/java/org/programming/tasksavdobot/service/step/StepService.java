package org.programming.tasksavdobot.service.step;
import lombok.RequiredArgsConstructor;
import org.programming.tasksavdobot.domen.step.UserStep;
import org.programming.tasksavdobot.reposetory.step.StepRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class StepService {
    private final StepRepository stepRepository;

    public UserStep getType(Update update) {

        if (update.hasMessage()) {
            return stepRepository.getByUserId(update.getMessage().getChatId());
        } else if (update.hasCallbackQuery()) {
            return stepRepository.getByUserId(update.getCallbackQuery().getMessage().getChatId());
        }
        return null;
    }

    public void setType(Update update, String product,String typ) {

        Long chatId = getChatIdFromUpdate(update);
        if (chatId == null) return;

        UserStep userStep = stepRepository.getByUserId(chatId);

        if (userStep == null) {
            userStep = new UserStep();
            userStep.setUserId(chatId);
        }

        if (product != null) {
            userStep.setStepName(product);
        }
            userStep.setTyp(typ);

        stepRepository.save(userStep);
    }

    private Long getChatIdFromUpdate(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return null;

    }
}
