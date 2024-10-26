<h1 align="center">Search Engine</h1>
<p align="center">Основная цель проекта - создание поискового сервиса</p>
<p align="center">
<img src="https://img.shields.io/badge/made%20by-SergeyDmitriev-blue.svg" >
</p>

<p align="center">
<img src="https://img.shields.io/badge/java-17-green.svg">
<img src="https://img.shields.io/badge/liquibase-4.29.2-green.svg">
</p>

## Installation
- Запуск докер контейнера с базой данных - `docker compose up -d`
- Оствновка и удаление кнотейнера с базой данных - `docker compose down`

## Settings
- Docker - [Установка и настройка](https://docs.docker.com/?_gl=1*xpz8ao*_gcl_au*MjExODYxNzMwOC4xNzI5MjE2ODI0*_ga*NDkzMjczMjAzLjE3MjczMTcyNTY.*_ga_XJWPQMJYHQ*MTcyOTIxNjgyNC4yLjEuMTcyOTIxNjgyNi41OC4wLjA.)
- Java - [Установка и настройка](https://www.java.com/ru/download/help/download_options_ru.html#mac)
- Liquibase - [How to](https://docs.liquibase.com/start/home.html)

## Package

### Indexing
- Запуск индексации
- Остановка

### Search
- Формирование ответа на поисковые запросы

### Statistic
- Формирование статистики

### Scrabbing
- генерация заданий для получения ссылок


### Lemma
- Получение лемм
- Работа с леммами

### Morphology
- Определение языка текста
- Получение нормальной формы


### Snippets
- Формирование снипетов

## Examples API requests
Postman collection - [SearchEngine.postman_collection.json](SearchEngine.postman_collection.json)

