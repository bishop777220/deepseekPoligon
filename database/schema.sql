-- Contact Book schema and demo data for MySQL 8.0+
-- Usage: mysql -u root -p --default-character-set=utf8mb4 < database/schema.sql

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS contactbook
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Local Tomcat process
CREATE USER IF NOT EXISTS 'contactbook'@'localhost' IDENTIFIED BY 'contactbook';
GRANT ALL PRIVILEGES ON contactbook.* TO 'contactbook'@'localhost';

-- Docker / remote clients
CREATE USER IF NOT EXISTS 'contactbook'@'%' IDENTIFIED BY 'contactbook';
GRANT ALL PRIVILEGES ON contactbook.* TO 'contactbook'@'%';

FLUSH PRIVILEGES;

USE contactbook;

CREATE TABLE IF NOT EXISTS contacts (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    first_name    VARCHAR(80)  NOT NULL,
    last_name     VARCHAR(80)  NOT NULL,
    email         VARCHAR(160) NOT NULL,
    phone         VARCHAR(40)  NULL,
    organization  VARCHAR(120) NULL,
    category      VARCHAR(20)  NOT NULL,
    notes         VARCHAR(1000) NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_contacts_email (email),
    KEY idx_contacts_name (last_name, first_name),
    KEY idx_contacts_category (category)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT IGNORE INTO contacts
    (first_name, last_name, email, phone, organization, category, notes, created_at, updated_at)
VALUES
    ('Анна', 'Смирнова', 'anna.smirnova@example.com', '+7 900 123-45-67', 'ООО «Ромашка»', 'WORK',
     'Руководитель проектов. Предпочитает звонки после 11:00.', NOW(6), NOW(6)),
    ('Дмитрий', 'Козлов', 'dmitry.kozlov@example.com', '+7 900 234-56-78', 'ТехноСофт', 'WORK',
     'Ведущий разработчик. Помогает с интеграциями.', NOW(6), NOW(6)),
    ('Мария', 'Иванова', 'maria.ivanova@example.com', '+7 900 345-67-89', NULL, 'FAMILY',
     'Двоюродная сестра, живёт в Казани.', NOW(6), NOW(6)),
    ('Сергей', 'Петров', 'sergey.petrov@example.com', '+7 900 456-78-90', 'Фриланс', 'FRIEND',
     'Фотограф, снимает семейные праздники.', NOW(6), NOW(6)),
    ('Елена', 'Соколова', 'elena.sokolova@example.com', '+7 900 567-89-01', 'Школа № 15', 'WORK',
     'Классный руководитель.', NOW(6), NOW(6)),
    ('Алексей', 'Морозов', 'alexey.morozov@example.com', '+7 900 678-90-12', NULL, 'OTHER',
     'Знакомый по книжному клубу.', NOW(6), NOW(6)),
    ('Ольга', 'Волкова', 'olga.volkova@example.com', '+7 900 789-01-23', 'Клиника «Здоровье»', 'FAMILY',
     'Семейный врач.', NOW(6), NOW(6)),
    ('Иван', 'Лебедев', 'ivan.lebedev@example.com', '+7 900 890-12-34', 'Гараж', 'FRIEND',
     'Автомеханик, поможет с машиной.', NOW(6), NOW(6)),
    ('Наталья', 'Новикова', 'natalia.novikova@example.com', '+7 900 901-23-45', 'Дизайн-студия «Пиксель»', 'WORK',
     'Дизайнер интерфейсов.', NOW(6), NOW(6)),
    ('Павел', 'Егоров', 'pavel.egorov@example.com', '+7 900 012-34-56', NULL, 'OTHER',
     NULL, NOW(6), NOW(6)),
    ('Татьяна', 'Орлова', 'tatiana.orlova@example.com', '+7 900 111-22-33', 'Бухгалтерия «Плюс»', 'WORK',
     'Ведёт документы по ИП.', NOW(6), NOW(6)),
    ('Михаил', 'Зайцев', 'mikhail.zaytsev@example.com', '+7 900 222-33-44', NULL, 'FRIEND',
     'Сосед, увлекается велосипедами.', NOW(6), NOW(6));
