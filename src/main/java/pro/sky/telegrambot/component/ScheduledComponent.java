package pro.sky.telegrambot.component;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.domain.NotificationTask;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class ScheduledComponent {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final NotificationTaskRepository notificationTaskRepository;
    private final TelegramBot telegramBot;

    public ScheduledComponent(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    private void checkSchedule() {
        List<NotificationTask> scheduleTasks = notificationTaskRepository.getNotificationTaskByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        scheduleTasks.forEach(notificationTask -> {
            telegramBot.execute(new SendMessage(notificationTask.getChatId(), notificationTask.getMessageText()));
            notificationTaskRepository.delete(notificationTask);
            logger.info("Message sent: {} ", notificationTask);
            logger.info("Invoke method for deleting task");
        });
    }
}
