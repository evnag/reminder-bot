package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

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
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendMessage(Long chatId, String message) {
        SendMessage messageToSent = new SendMessage(chatId, message);
        SendResponse response = telegramBot.execute(messageToSent);
        response.isOk();
    }


}
