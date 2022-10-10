[![Build Status](https://travis-ci.org/VadimShein/job4j_chat.svg?branch=master)](https://travis-ci.org/VadimShein/job4j_chat)

# <p align="center">Приложение чат</p>


## Описание:
Приложение реализует функционал простого чата. 
Используется Rest архитектура. 
Пользователи регистрируются, затем авторизируются с использованием JWT token.
Пользователь может создать или редактировать комнату для общения, а также оставить свой комментарий.


## Используемые технологии:
* Java 13, Rest API, RestTemplate
* Spring Boot, Spring Security & JWT authorization
* Spring Data
* PosgreSQL, Hibernate
* Maven


## Запросы к "/users"
1. Зарегистрировать пользователя
```
curl -i --header "Content-Type: application/json" \
--request POST 'http://localhost:8080/users/sign-up' \
-d '{"username":"username2", "password":"password2", "role": {"roleName": "user"}}'
```

2. Авторизация пользователя и получение токена вида "Bearer your_token".
```
curl -i --header "Content-Type: application/json" \
--request POST 'http://localhost:8080/login' \
-d '{"username":"username2", "password":"password2"}'
```

3. Получить список всех пользователей
```
curl -i -H "Authorization: Bearer your_token" http://localhost:8080/users/all
```

4. Получить пользователя по id
```
curl -i -H "Authorization: Bearer your_token" http://localhost:8080/users/20
```

## Запросы к "/room"

1. Получить список всех комнат со всеми сообщениями, используется RestTemplate
```
curl -i -H "Authorization: Bearer your_token" http://localhost:8080/room/
```

2. Получить комнату по id со всеми сообщениями, используется RestTemplate
```
curl -i -H "Authorization: Bearer your_token" http://localhost:8080/room/1
```

3. Добавить комнату
```
curl -i --header "Content-Type: application/json" \
-H "Authorization: Bearer your_token" \
--request POST 'http://localhost:8080/room/' \
-d '{"name":"room_1", "description":"room_1 description"}'
```

4. Редактировать описание комнаты
```
curl -i --header "Content-Type: application/json" \
-H "Authorization: Bearer your_token" \
--request PUT 'http://localhost:8080/room/' \
-d '{"id":1, "name":"room_1", "description":"room_1 description"}'
```

5. Удалить комнату
```
curl -i --header "Content-Type: application/json" \
-H "Authorization: Bearer your_token" \
--request DELETE 'http://localhost:8080/room/1'
```

6. Добавить сообщение в комнату, используется RestTemplate
```
curl -i --header "Content-Type: application/json" \
-H "Authorization: Bearer your_token" \
--request POST 'http://localhost:8080/room/message' \
-d '{"text":"test_text_rest", "roomId":1, "authorId": 20}}'
```

7. Редактировать сообщение, используется RestTemplate
```
curl -i --header "Content-Type: application/json" \
-H "Authorization: Bearer your_token" \
--request PUT 'http://localhost:8080/room/message' \
-d '{"id": 19, "text":"updated_text_rest", "roomId":1, "authorId":20}}'
```

8. Удалить сообщение по id, используется RestTemplate
```
curl -i --header "Content-Type: application/json" \
-H "Authorization: Bearer your_token" \
--request DELETE 'http://localhost:8080/room/message/19'
```

## Запросы к "/message"
1. Получить все сообщения
```
curl -i -H "Authorization: Bearer your_token" http://localhost:8080/message/
```

2. Получить сообщение по id
```
curl -i -H "Authorization: Bearer your_token" http://localhost:8080/message/22
```

3. Создать сообщение
```
curl -i --header "Content-Type: application/json" \
-H "Authorization: Bearer your_token" \
--request POST 'http://localhost:8080/message/' \
-d '{"text":"test_text", "roomId":1, "authorId":20}}'
```

4. Обновить сообщение
```
curl -i --header "Content-Type: application/json" \
-H "Authorization: Bearer your_token" \
--request PUT 'http://localhost:8080/message/' \
-d '{"id": 14, "text":"updated_text", "roomId":1, "authorId":20}}'
```

5. Пропатчить сообщение
```
curl -i --header "Content-Type: application/json" \
-H "Authorization: Bearer your_token" \
--request PATCH 'http://localhost:8080/message/' \
-d '{"id": 14, "text":"updated_text", "roomId":1, "authorId":20}}'
```

6. Удалить сообщение
```
curl -i --header "Content-Type: application/json" \
-H "Authorization: Bearer your_token" \
--request DELETE 'http://localhost:8080/message/15'
```


## Запуск проекта:
1. Скопировать проект 
```
git clone https://github.com/VadimShein/job4j_chat
```

2. Создать базу данных chat и таблицы из файла src/main/resources/db/schema.sql

3. Собрать проект
```
mvn clean package -DskipTests
```

4. Запустить приложение, адрес по умолчанию  [http://localhost:8080/](http://localhost:8080/)
```
mvn spring-boot:run
```


## Контакты:
[![alt-text](https://img.shields.io/badge/-telegram-grey?style=flat&logo=telegram&logoColor=white)](https://t.me/SheinVadim)
[![alt-text](https://img.shields.io/badge/@%20email-005FED?style=flat&logo=mail&logoColor=white)](mailto:shein.v94@mail.ru)
