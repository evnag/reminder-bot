package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.domain.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;

import static pro.sky.telegrambot.message.MessageConstants.*;
import static pro.sky.telegrambot.service.ServiceConstants.*;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskRepository notificationTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            String messageText = update.message().text();
            Long chatId = update.message().chat().id();

            if (messageText.startsWith(START_MESSAGE)) {
                logger.info("Message received: {}", messageText);
                sendMessage(chatId, WELCOME_MESSAGE);
                logger.info("Message sent: {}", WELCOME_MESSAGE);
            } else {
                Matcher matcher = PATTERN.matcher(messageText);
                if (matcher.matches()) {
                    String date = matcher.group(1);
                    messageText = messageText.replace(date, "").trim();

                    LocalDateTime localDateTime = LocalDateTime.parse(date, DATE_TIME_FORMATTER);
                    createTask(chatId, messageText, localDateTime);
                    sendMessage(chatId, SUCCESSFULLY_SAVED_MESSAGE);
                    logger.info("Message sent: {}", SUCCESSFULLY_SAVED_MESSAGE);
                    logger.info("Invoke method for creating task");
                } else {
                    sendMessage(chatId, WRONG_MESSAGE_FORMAT);
                    logger.info("Message sent: {}", WRONG_MESSAGE_FORMAT);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    private void checkSchedule() {
        List<NotificationTask> scheduleTasks = notificationTaskRepository.getNotificationTaskByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        scheduleTasks.forEach(notificationTask -> {
            sendMessage(notificationTask.getChatId(), notificationTask.getMessageText());
            notificationTaskRepository.delete(notificationTask);
            logger.info("Message sent: {} ", notificationTask);
            logger.info("Invoke method for deleting task");
        });
    }

    private void sendMessage(Long chatId, String message) {
        telegramBot.execute(new SendMessage(chatId, message));
    }

    private void createTask(Long chatId, String message, LocalDateTime dateTime) {
        notificationTaskRepository.save(new NotificationTask(chatId, message, dateTime));
    }
}
