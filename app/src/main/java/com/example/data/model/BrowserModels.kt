package com.example.data.model

import java.util.UUID

enum class TabSpace(val title: String) {
    ALL("Все"),
    WORK("Работа"),
    STUDY("Учеба"),
    PERSONAL("Личное"),
    SHOPPING("Шопинг")
}

enum class SearchEngine(val title: String, val searchUrlPrefix: String) {
    GOOGLE("Google", "https://www.google.com/search?q="),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q="),
    YANDEX("Яндекс", "https://yandex.ru/search/?text="),
    PERPLEXITY("Perplexity AI", "https://www.perplexity.ai/search?q=")
}

data class WebTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Новая вкладка",
    val url: String = "lumina://newtab",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val pageText: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isDesktopMode: Boolean = false,
    val isIncognito: Boolean = false,
    val space: TabSpace = TabSpace.ALL,
    val isPinned: Boolean = false
)

data class Bookmark(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val category: String = "Общие",
    val isPinned: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

enum class LuminaWallpaper(
    val id: String,
    val title: String,
    val description: String,
    val drawableResId: Int? = null
) {
    DEFAULT("default", "Минимализм", "Чистый цвет текущей темы", null),
    CUSTOM("custom", "Свое фото", "Изображение из галереи устройства", null),
    COSMIC_NEBULA("cosmic", "Космос", "Туманность со звездами", com.example.R.drawable.wp_cosmic_nebula),
    CYBER_NEON("cyber", "Киберпанк", "Неоновый город и сетка", com.example.R.drawable.wp_cyber_neon),
    AURORA("aurora", "Аврора", "Северное сияние и горы", com.example.R.drawable.wp_aurora_borealis),
    SUNSET_DUNES("sunset", "Закат", "Теплые закатные дюны", com.example.R.drawable.wp_sunset_dunes),
    GEOMETRIC_GLASS("geometric", "Стекло", "Абстрактные грани и блики", com.example.R.drawable.wp_geometric_abstract)
}

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis()
)

data class ExtractedTask(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val category: String = "Действие",
    val isDone: Boolean = false
)

data class PasswordEntry(
    val id: String = UUID.randomUUID().toString(),
    val siteName: String,
    val domain: String,
    val username: String,
    val password: String,
    val updatedAt: Long = System.currentTimeMillis()
)

data class DownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val url: String,
    val mimeType: String = "",
    val fileSizeBytes: Long = 0,
    val progress: Int = 100,
    val isCompleted: Boolean = true,
    val downloadedAt: Long = System.currentTimeMillis()
)

data class WebNote(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val domain: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

enum class LuminaThemeMode(val title: String, val description: String) {
    OBSIDIAN("Обсидиан", "Глубокий темный минимализм с фиолетовым свечением"),
    NEON("Киберпанк", "Ультраконтрастный темный с неоновым цианом и пурпуром"),
    DEEP_SPACE("Глубокий космос", "Темно-синяя космическая бездна с сапфировыми искрами"),
    EMERALD("Северный лес", "Сдержанный темно-изумрудный скандинавский стиль"),
    LIGHT("Эфирный свет", "Кристально чистый светлый дизайн с индиго акцентами")
}

enum class AiActionType(val title: String, val description: String, val iconName: String) {
    SUMMARIZE(
        title = "Выжимка страницы",
        description = "Главные тезисы и выводы за 10 секунд",
        iconName = "summary"
    ),
    EXTRACT_TASKS(
        title = "Собрать задачи и чеклист",
        description = "Автоматически извлечь все дела, шаги и покупки",
        iconName = "tasks"
    ),
    FIND_KEY_FACTS(
        title = "Ключевые факты",
        description = "Важные цифры, выводы и аргументы",
        iconName = "facts"
    ),
    EXPLAIN(
        title = "Объяснить простыми словами",
        description = "Разбор сложных терминов простым языком",
        iconName = "explain"
    ),
    COMPARE_PAGE(
        title = "Сравнить с аналогами",
        description = "Поиск альтернатив и сравнительный анализ",
        iconName = "compare"
    ),
    FACT_CHECK(
        title = "Проверить факты",
        description = "Оценка надежности источников и проверка данных",
        iconName = "check"
    ),
    TRANSLATE(
        title = "Умный перевод",
        description = "Адаптивный перевод с сохранением смысла",
        iconName = "translate"
    ),
    DRAFT_MESSAGE(
        title = "Составить сообщение / письмо",
        description = "Создать готовый ответ на основе содержания",
        iconName = "draft"
    ),
    CHAT(
        title = "Задать вопрос по странице",
        description = "Интеллектуальный поиск в контексте документа",
        iconName = "chat"
    )
}

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Streaming(val partialText: String) : AiUiState
    data class Success(val responseText: String) : AiUiState
    data class Error(val errorMessage: String) : AiUiState
}
