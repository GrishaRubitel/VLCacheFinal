# VideoCache

# Запуск

Для запуска проекта клонируем/качаем себе этот репозиторий и ставим Docker Desktop и WSL, в случае с Windows.

1. [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)
2. [https://learn.microsoft.com/ru-ru/windows/wsl/install](https://learn.microsoft.com/ru-ru/windows/wsl/install)

Установка на Linux.

[https://docs.docker.com/engine/install/ubuntu/](https://docs.docker.com/engine/install/ubuntu/).

Далее открываем терминал в корне (там где лежит **docker-compose.yml**) и прописываем следующие команды

```powershell
docker-compose build
docker-compose up
#Or
docker-compose up --build
```

Изменение кода сервисов требует повторной сборки **docker-compose**
