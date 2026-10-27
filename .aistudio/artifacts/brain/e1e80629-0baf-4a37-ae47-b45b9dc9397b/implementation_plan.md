# План реализации: Пользовательские обои из галереи и персонализация Lumina Browser

В соответствии с запросом и ТОП-100 рекомендациями добавляется возможность установки собственных фотографий и картинок из галереи устройства на фон стартового экрана, а также сопутствующие инструменты кастомизации.

---

## 1. Архитектура и интеграция с Android

### 1.1 Безопасный выбор фото через Android Photo Picker
- Использование стандартного Android контракта `ActivityResultContracts.PickVisualMedia(ActivityResultContracts.PickVisualMedia.ImageOnly)`.
- Полное соответствие Google Play Policy: **0 опасных разрешений** (без устаревших `READ_EXTERNAL_STORAGE`).
- При выборе изображения данные безопасно копируются во внутреннее изолированное хранилище приложения (`context.filesDir / "lumina_custom_wallpaper.png"`). Это гарантирует сохранение фонового изображения даже при очистке временных кэшей галереи или перезагрузке устройства.

### 1.2 Состояние и настройки в BrowserViewModel & BrowserUiState
- Добавление полей в `BrowserUiState`:
  - `customWallpaperPath: String?` — путь к локальному сохраненному файлу фона.
  - `wallpaperBlur: Float` (0f..25f) — уровень эффекта размытия (Glassmorphism / Frosted Glass).
  - `wallpaperDimming: Float` (0f..0.85f) — интенсивность затемнения для читаемости текста и закладок.
- Методы ViewModel:
  - `setCustomWallpaper(uri: Uri, context: Context)`
  - `removeCustomWallpaper()`
  - `setWallpaperBlur(blur: Float)`
  - `setWallpaperDimming(dimming: Float)`
  - Автоматическое сохранение настроек в локальный DataStore / Preferences.

---

## 2. Пользовательский интерфейс (NewTabScreen.kt)

### 2.1 Обновленная панель выбора обоев (Wallpaper Picker Sheet)
- Добавление первой интерактивной карточки: **«Свое фото»** с иконкой галереи (`Icons.Default.AddPhotoAlternate`).
- Индикатор выбранного пользовательского фото с кнопкой быстрой замены или сброса на стандартные темы.
- Ползунок **«Размытие фона (Blur)»** в дополнение к ползунку **«Затемнение (Dimming)»** для идеального контраста и читаемости плиток закладок.
- Быстрый переключатель между встроенными космическими/киберпанк градиентами и личным фото.

### 2.2 Рендеринг фона
- Использование `AsyncImage` (библиотека Coil) для эффективного декодирования и кэширования изображения в памяти без просадок FPS.
- Слой наложения `Box` с `Modifier.background(Color.Black.copy(alpha = wallpaperDimming))` и `Modifier.blur(...)` при включенном размытии.

---

## 3. План тестирования и верификации
1. **Robolectric Unit Tests**: Тестирование состояния `BrowserViewModel` при установке и очистке кастомного фона в `ExampleRobolectricTest.kt`.
2. **Сборка и проверка компиляции**: Прогон `compile_applet` и сборка обновленного APK-файла.
3. **Обновление релизного описания**: Актуализация `lumina_browser_release.md`.
