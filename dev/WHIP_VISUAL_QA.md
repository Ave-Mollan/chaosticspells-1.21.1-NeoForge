# Проверка кнута двумя настоящими клиентами

`src/visualTest` подключается только с `-Pwhip_visual_test=true`. Обычный `build`
не включает этот код в JAR. Проверка использует отдельные папки под `build/`,
собственный плоский мир и подключение второго клиента через `127.0.0.1:25574`.
Окна тестовых клиентов скрыты; скриншоты сохраняет сам Minecraft.

Запустить хозяина из корня проекта:

```powershell
.\gradlew.bat runClient -Pwhip_visual_test=true -Pwhip_visual_role=host -Pwith_viltrumite=true -Pave_client_uuid=516e7a70-c667-499b-9870-2783e9e7b214 -Pave_client_name=AveMollan -Pave_client_dir=whip7-client-host --offline --no-daemon --console=plain
```

После появления `build/whip7-visual-session/ready.txt` запустить во втором терминале:

```powershell
.\gradlew.bat runClient -Pwhip_visual_test=true -Pwhip_visual_role=observer -Pwith_viltrumite=true -Pave_client_uuid=2a8ddf21-e3b7-4b3b-89ca-b4f7c147d1aa -Pave_client_name=WhipObserver -Pave_client_dir=whip7-client-observer '-Pave_client_server=127.0.0.1:25574' --offline --no-daemon --console=plain
```

Оба клиента автоматически завершаются после проверки. Перед повторным запуском
переместить предыдущие `ready.txt`, `observer-ready.txt`, `*-done.txt`, `*-failure.txt`
и `*-trace.json` из этой папки в отдельный каталог результатов.

Сценарий:

1. Хозяин берёт свёрнутый кнут и наносит удар через настоящий сетевой обработчик ввода.
2. Наблюдатель подключается после завершения удара и получает уже лежащий ремень.
3. Хозяин наносит второй одиночный удар. Оба клиента сохраняют замах назад,
   бросок вперёд, щелчок, осевший ремень и поворот владельца после удара.
4. `host-trace.json` и `observer-trace.json` содержат хеши всех 75 координат
   по серверному тику. На общих тиках значения должны совпадать.

Кадры: `build/whip7-client-host/screenshots` и
`build/whip7-client-observer/screenshots`. Эти изображения получены из игрового
буфера кадра, а не из расчётного превью модели.

После проверки собрать обычный `build` без флага `whip_visual_test` и проверить,
что JAR не содержит `WhipVisualProbe` и классов `gametest`.
