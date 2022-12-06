package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.telegrambot.domain.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;


public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> getNotificationTaskByDateTime(LocalDateTime dateTime);

}
