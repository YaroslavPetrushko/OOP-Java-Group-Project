# OOP-Java-Group-Project
Командний проєкт.
**GUI-додаток для роботи з таблицею в СУБД (JavaFX + JDBC).**  
Група: ІН-33-4.  
Дисципліна: Об'єктно-орієнтоване програмування на мові Java.

[Посилання на JavaDoc на Github Pages.](https://yaroslavpetrushko.github.io/OOP-Java-Group-Project/com.library/com/library/package-summary.html)
# 📚 Library Management System (JavaFX + JDBC)

## 📌 Опис проєкту

Цей проєкт є десктопним GUI-застосунком для роботи з базою даних бібліотеки.  
Метою є реалізація CRUD-операцій та пошуку для управління книгами, авторами та читачами.

Застосунок дозволяє:
- додавати, редагувати та видаляти записи;
- переглядати інформацію у вигляді таблиць;
- виконувати пошук за різними критеріями;
- працювати з базою даних PostgreSQL через JDBC.

---

## 🧱 Використані технології

| Технологія    | Версія  | Призначення                        |
|---------------|---------|------------------------------------|
| Java          | 25      | Мова програмування                 |
| JavaFX        | 25.0.2  | Графічний інтерфейс                |
| PostgreSQL    | 18      | СУБД                               |
| pgJDBC        | 42.7.10 | Драйвер, взаємодія з БД            |
| Maven         | 4.0.0   | Збірка та залежності               |
| dotenv-java   | 3.0.0   | Конфігурація підключення           |
| JUnit Jupiter | 5.11.3  | Модульне тестування DAO та моделей |

---

## 🏗 Архітектура застосунку

Проєкт побудований за шаблоном **MVC**:
```
View (FXML)  →  Controller  →  DAO  →  DB (PostgreSQL)
                    ↕
                  Model
```

- **model/** — POJO-класи (`Book`, `Author`, `Reader`, `Loan`)
- **dao/** — інтерфейси та JDBC-реалізації (PreparedStatement)
- **controller/** — JavaFX-контролери, обробка подій
- **view/** — FXML-макети + CSS
- **db/** — `DBConnection` (Singleton), JDBC-з'єднання
- **test/** — JUnit 5 тести для DAO (`FakeJdbc`-фреймворк) та моделей
- **docs/** — Документація JavaDocs

Всі SQL-запити реалізовані через **`PreparedStatement`** — без конкатенації рядків.
---

## 📂 Структура проєкту

```
OOP-Java-Group-Project/
├── sql/
│   ├── schema.sql              # DDL: CREATE TABLE
│   └── seed.sql                # Тестові дані
├── src/main/
│   ├── java/
│   │   ├── module-info.java
│   │   └── com/library/
│   │       ├── Main.java           # Entry point
│   │       ├── AppLauncher.java    # JavaFX launcher wrapper
│   │       ├── LibraryApp.java     # Application subclass
│   │       ├── model/              # Book, Author, Reader, Loan
│   │       ├── dao/                # DAO interfaces + JDBC implementations
│   │       ├── controller/         # JavaFX controllers
│   │       ├── view/               # (резерв)
│   │       └── db/
│   │           └── DBConnection.java # Підключення до бази даних
│   └── resources/
│       ├── fxml/               # FXML-макети вікон
│       └── css/                # Стилі
├── src/test/
│   └── java/com/library/
│       ├── dao/impl/           # DAO-тести (FakeJdbc + JUnit 5)
│       │   ├── FakeJdbc.java
│       │   ├── AuthorDaoImplTest.java
│       │   ├── BookDaoImplTest.java
│       │   ├── ReaderDaoImplTest.java
│       │   └── LoanDaoImplTest.java
│       └── model/              # POJO-тести
│           ├── AuthorTest.java
│           ├── BookTest.java
│           ├── LoanTest.java
│           └── ReaderTest.java
├── docs/                       # Javadoc (автогенерація через GitHub Actions)
├── .github/
│   └── workflows/
│       └── javadoc.yaml        # CI: публікація Javadoc у гілку main/docs
├── .env                        # Локальна конфігурація (відсутня в git)
├── .gitignore
└── pom.xml
```
 
---

## 🗄 Схема бази даних

```
authors          books                readers          loans
────────         ────────────         ────────         ──────────────
id (PK)          id (PK)              id (PK)          id (PK)
full_name        title                full_name        book_id (FK)
country          author_id (FK) ──→   email            reader_id (FK)
birth_year       genre                phone            loan_date
                 isbn                 reg_date         due_date
                 pub_year                              status
                 copies
```

**Зв'язки:**
- `authors` **1 → N** `books`
- `books` **1 → N** `loans`
- `readers` **1 → N** `loans`

> `loans` — асоціативна таблиця між `books` і `readers` (M:N через дві FK).  
> Поле `status`: `active` | `returned` | `overdue`.
 
---

## ⚙️ Функціонал

| Операція                                    | Статус  |
|---------------------------------------------|---------|
| Read — перегляд таблиць                     | ✅      |
| Create — додавання записів                  | ✅      |
| Update — редагування записів                | ✅      |
| Delete — видалення записів                  | ✅      |
| Каскадне видалення з підтвердженням         | ✅      |
| Автоматичне позначення overdue-позик        | ✅      |
| Пошук книги за назвою / автором / ISBN      | ✅      |
| Фільтрація книг за жанром та роком          | ✅      |
| Пошук автора за іменем / країною            | ✅      |
| Фільтрація авторів за роком                 | ✅      |
| Пошук читача за іменем / поштою / тел.      | ✅      |
| Фільтрація читачів за датою реєстрації      | ✅      |
| Пошук позик за ID / книгою / читачем        | ✅      |
| Фільтрація позик за статусом та датою       | ✅      |
| Валідація полів у діалогах                  | ✅      |
| Колірне кодування статусу позик             | ✅      |
| Javadoc для всіх публічних класів           | ✅      |
| Модульні тести (JUnit 5, FakeJdbc)          | ✅      |
| CI: автопублікація Javadoc (GitHub Actions) | ✅      |

---
## 🧪 Тестування

Проєкт покрито модульними тестами на базі **JUnit 5** без залежності від реальної БД.

### Підхід

`FakeJdbc` — власний легковагий фреймворк на `java.lang.reflect.Proxy`, що підміняє
`DBConnection` синглтон фейковими `Connection` / `PreparedStatement` / `ResultSet`.
Це дозволяє тестувати DAO-логіку ізольовано, без PostgreSQL та зовнішніх бібліотек
(Mockito, Testcontainers тощо).

### Покриття

| Клас                | Тип тестів          | К-сть тестів |
|---------------------|---------------------|:------------:|
| `AuthorDaoImpl`     | DAO (FakeJdbc)      | 22           |
| `BookDaoImpl`       | DAO (FakeJdbc)      | 22           |
| `ReaderDaoImpl`     | DAO (FakeJdbc)      | 22           |
| `LoanDaoImpl`       | DAO (FakeJdbc)      | 20           |
| `Author`            | POJO / unit         | 6            |
| `Book`              | POJO / unit         | 12           |
| `Reader`            | POJO / unit         | 8            |
| `Loan`              | POJO / unit         | 9            |

### Запуск

```bash
mvn test
```
---

## 📖 Документація

Javadoc згенеровано для всіх класів (включно з `private`-членами).

- **Локально:** `mvn javadoc:javadoc` → `target/reports/apidocs/index.html`
- **GitHub Actions:** при кожному push у `main` Javadoc автоматично оновлюється
  в папці `docs/` того ж репозиторію.
---

## 👥 Команда

| Роль            | Учасник               | Обов’язки                                                 |
|-----------------|-----------------------|-----------------------------------------------------------|
| **Team Lead**   | *Петрушко Ярослав*    | Координація, рев’ю PR, архітектура, внесення правок в код |
| **Developer 1** | *Куцомеля Денис*      | Робота з БД, JDBC, DAO, PreparedStatement                 |
| **Developer 2** | *Федоренко Роман*     | JavaFX UI/UX, FXML, CSS, тестування                       |
| **Developer 3** | *Гордієнко Владислав* | Бізнес-логіка, Controller, Model                          |

---

## 🌿 Робота з Git

### Структура гілок

| Гілка       | Призначення       |
|-------------|-------------------|
| `main`      | Стабільні релізи  |
| `develop`   | Основна розробка  |
| `feature/*` | Новий функціонал  |
| `fix/*`     | Виправлення помилок |

### Формат комітів

```
type: короткий опис (англійська)
```

| Тип        | Коли використовувати         |
|------------|------------------------------|
| `feat`     | Новий функціонал             |
| `fix`      | Виправлення помилки          |
| `refactor` | Рефакторинг без зміни логіки |
| `docs`     | Документація                 |
| `style`    | Форматування, CSS            |
| `test`     | Тести                        |

**Приклади:**
```
feat: add book search by title
fix: correct PreparedStatement index in BookDaoImpl
refactor: extract DB error handling to base DAO class
docs: update README with setup instructions
```

### Pull Request правила

- PR створюється **тільки в `develop`**
- Мінімум **1 рев'ю** (Team Lead)
- Заборонено пушити напряму в `main` (лише chores)
- Кожен PR повинен містити: опис змін + пояснення логіки

---

## 📋 План робіт

| Період      | Задача                                               | Статус       |
|-------------|------------------------------------------------------|--------------|
| 01.03–10.03 | Вибір теми, розподіл ролей                           | ✅ Виконано  |
| 11.03–23.03 | Підготовка тез для конференції                       | ✅ Виконано  |
| 20.03–29.03 | Проєктування БД, schema.sql, seed.sql                | ✅ Виконано  |
| 24.03–26.03 | Step 1–2: структура Maven-проєкту, підключення до БД | ✅ Виконано  |
| 27.03–28.03 | Step 3: перше JavaFX-вікно                           | ✅ Виконано  |
| 29.03–05.03 | Step 4: базовий UI — навігація, TabPane, TableView   | ✅ Виконано  |
| 01.04–15.04 | Step 5: Model + DAO + перший живий CRUD (Books)      | ✅ Виконано  |
| 15.04–29.04 | Step 6: CRUD для Authors, Readers, Loans             | ✅ Виконано  |
| 29.04–10.05 | **Step 7: пошук та фільтрація (PreparedStatement)**  | ✅ Виконано  |
| 10.04–11.05 | **Step 8: полірування UI, діалоги, валідація**       | ✅ Виконано  |
| 11.05–12.05 | Тестування, виправлення помилок                      | ✅ Виконано  |
| 12.05–13.05 | Документація, підготовка до захисту                  | 🔄 Поточний  |

---

## ⚠️ Примітки

- Всі SQL-запити — **тільки через `PreparedStatement`**
- Credentials — **тільки через `.env`**, не хардкодити
- SQL-запити розміщуються **тільки в DAO-класах**, не в контролерах
- Назви гілок, комітів і коментарі до коду — **англійська мова**
- Модульні тести — **без реальної БД**: `FakeJdbc` емулює JDBC-шар через `Proxy`
- Javadoc публікується автоматично через **GitHub Actions** (`docs/` у гілці `main`)