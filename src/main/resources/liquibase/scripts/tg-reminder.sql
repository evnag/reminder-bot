-- liquibase formatted sql

-- changeset evnag:1
CREATE TABLE notification_task
(
    id SERIAL PRIMARY KEY,
    chatId SERIAL,
    messageText TEXT,
    dateTime TIMESTAMP
);