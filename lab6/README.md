# Лабораторная работа №6 — Клиент-серверное приложение

## Структура проекта

```
src/
├── common/
│   ├── Request.java          # Объект запроса клиент → сервер (Serializable)
│   └── Response.java         # Объект ответа сервер → клиент (Serializable)
│
├── model/
│   ├── MusicBand.java         # Serializable (добавлен serialVersionUID)
│   ├── Coordinates.java       # Serializable
│   ├── Studio.java            # Serializable
│   └── MusicGenre.java        # Serializable enum
│
├── managers/
│   ├── CollectionManager.java # Управление коллекцией (Stream API + лямбды)
│   └── FileManager.java       # Работа с CSV-файлом (без изменений)
│
├── server/
│   ├── Server.java            # Серверное приложение (NIO, неблокирующий режим)
│   └── CommandHandler.java    # Обработчик команд на сервере
│
└── client/
    ├── Client.java            # Клиентское приложение
    ├── InputManager.java      # Ввод и валидация данных
    └── ServerConnection.java  # TCP-соединение через потоки ввода-вывода
```

## Компиляция

```bash
mkdir -p out
javac -d out -sourcepath src src/model/*.java src/common/*.java src/managers/*.java src/server/*.java src/client/*.java
```

## Запуск

### Сервер
```bash
export MUSIC_BAND_FILE=/path/to/collection.csv
java -cp out server.Server
```

### Клиент (в другом терминале)
```bash
java -cp out client.Client
```

## Серверные команды (только через консоль сервера)
- `save` — сохранить коллекцию в файл
- `exit` — завершить сервер (коллекция сохраняется автоматически)

## Клиентские команды
| Команда | Описание |
|---------|----------|
| `help` | Список команд |
| `info` | Информация о коллекции |
| `show` | Показать все элементы (отсортированы по имени) |
| `add` | Добавить элемент |
| `update <id>` | Обновить элемент по ID |
| `remove_by_id <id>` | Удалить элемент по ID |
| `clear` | Очистить коллекцию |
| `exit` | Завершить клиент |
| `add_if_min` | Добавить, если меньше минимального |
| `remove_greater` | Удалить элементы больше введённого |
| `remove_lower` | Удалить элементы меньше введённого |
| `count_by_studio [название]` | Подсчитать по студии |
| `filter_less_than_number_of_participants <n>` | Фильтр по числу участников |
| `print_descending` | Вывод в порядке убывания |

> Команда `save` **недоступна клиенту** — только через консоль сервера.

## Соответствие требованиям

| Требование | Реализация |
|------------|------------|
| Stream API + лямбды | `CollectionManager.java` — все операции через stream() |
| Объекты в сериализованном виде | `Request`, `Response`, `MusicBand` реализуют `Serializable` |
| Коллекция отсортирована по имени | `getSortedByName()` при команде `show` |
| Обработка недоступности сервера | `connectWithRetry()` в `Client.java` (5 попыток, 3 сек) |
| Протокол TCP | `ServerSocketChannel` / `Socket` |
| Сетевой канал на сервере | `NIO SocketChannel + Selector` (неблокирующий режим) |
| Потоки ввода-вывода на клиенте | `DataInputStream / DataOutputStream` в `ServerConnection` |
| Неблокирующий режим | `channel.configureBlocking(false)` + `Selector` |
| Команда save только на сервере | Консольный поток сервера; клиент получает ошибку |
| Команды — объекты классов | `Request(commandName, argument, MusicBand)` |
