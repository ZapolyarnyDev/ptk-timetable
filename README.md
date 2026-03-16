# PtkTimetable

## О приложении
PtkTimetable - Приложение для просмотра расписания занятий:
- выбор курса и группы;
- просмотр расписания по дням и по дате;
- заметки к парам и напоминания.

Данные расписания загружаются с портала НовГУ/СПО

## Если вы пользователь: как установить
1. Скачайте APK-файл приложения (см. [releases](https://github.com/ZapolyarnyDev/PtkTimetable/releases))
2. На телефоне разрешите установку из неизвестных источников (если требуется)
3. Откройте APK и установите приложение

## Если вы разработчик: как собрать и разрабатывать
### Требования
- Android Studio (актуальная версия).
- Android SDK (указан в `local.properties`)
- JDK 17+.

### Запуск в разработке
1. Откройте проект в Android Studio
2. Дождитесь Gradle Sync.
3. Запустите приложение на эмуляторе или устройстве

### Сборка из консоли
```powershell
# debug APK
.\gradlew.bat :app:assembleDebug

# release APK (без production-подписи по умолчанию)
.\gradlew.bat :app:assembleRelease
```

Готовые APK лежат в `app/build/outputs/apk/...`

## Лицензия
Проект распространяется по лицензии **Mozilla Public License 2.0 (MPL-2.0)**.  
См. файл [LICENSE](LICENSE).
