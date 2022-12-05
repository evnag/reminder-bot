package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.domain.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String welcomeMessage = "Welcome to the reminder bot! Enter a description of the task in the format \"01.01.2022 20:00 Some text\"";
            String messageText = update.message().text();
            Long chatId = update.message().chat().id();

            if (messageText.startsWith("/start")) {
                logger.info("Message received: {}", messageText);
                sendMessage(chatId, welcomeMessage);
                logger.info("Message sent: {}", welcomeMessage);
            } else {
                Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\P{M}\\p{M}*+]+)");
                Matcher matcher = pattern.matcher(messageText);
                if (matcher.matches()) {
                    String date = matcher.group(1);
                    messageText = messageText.replace(date, "").trim();

                    LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    createTask(chatId, messageText, localDateTime);
                    logger.info("Invoke method for creating task");
                } else {
                    messageText = "Wrong message format";
                    sendMessage(chatId, messageText);
                    logger.info("Message sent: {}", welcomeMessage);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    private void checkSchedule() {
        List<NotificationTask> scheduleTasks = notificationTaskRepository.getNotificationTaskByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        if (!scheduleTasks.isEmpty()) {
            scheduleTasks.forEach(notificationTask -> {
                sendMessage(notificationTask.getChatId(), notificationTask.getMessageText());
                notificationTaskRepository.delete(notificationTask);
                logger.info("Message sent: {} ", notificationTask);
                logger.info("Invoke method for deleting task");
            });
        }
    }

    private void sendMessage(Long chatId, String message) {
        SendMessage messageToSent = new SendMessage(chatId, message);
        SendResponse response = telegramBot.execute(messageToSent);
        response.isOk();
    }

    private void createTask(Long chatId, String message, LocalDateTime dateTime) {
        notificationTaskRepository.save(new NotificationTask(chatId, message, dateTime));
    }
}
