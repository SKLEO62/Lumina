package com.example.data.ai

import android.util.Log
import com.example.data.model.AiActionType
import com.example.data.model.ExtractedTask
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.Schema
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "LuminaAiService"

class LuminaAiService {

    // Lazy initialization of standard generative model
    private val standardModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    // JSON schema for structured task extraction
    private val taskListSchema = Schema.obj(
        properties = mapOf(
            "tasks" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "text" to Schema.string(description = "Название задачи, шага или действия"),
                        "category" to Schema.string(description = "Категория: Действие, Срок, Покупка, Напоминание")
                    )
                ),
                description = "Список извлеченных из контекста задач и действий"
            )
        )
    )

    private val structuredTaskModel by lazy {
        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = taskListSchema
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    /**
     * Streams an intelligent AI response based on the active page context and user action/query.
     */
    fun streamPageAction(
        actionType: AiActionType,
        userQuery: String = "",
        pageTitle: String,
        pageUrl: String,
        pageText: String
    ): Flow<String> = flow {
        val safeText = if (pageText.isNotBlank()) {
            pageText.take(15000)
        } else {
            "Текстовое содержимое страницы не обнаружено или страница находится на стартовом экране."
        }

        val prompt = buildContextPrompt(
            actionType = actionType,
            userQuery = userQuery,
            pageTitle = pageTitle,
            pageUrl = pageUrl,
            pageText = safeText
        )

        val fullTextBuilder = StringBuilder()

        try {
            withTimeout(60_000L) {
                standardModel.generateContentStream(prompt).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        fullTextBuilder.append(chunkText)
                        emit(fullTextBuilder.toString())
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed during streaming generation", e)
            throw e
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Extracts structured actionable tasks from the page content.
     */
    suspend fun extractTasks(pageTitle: String, pageText: String): List<ExtractedTask> {
        val safeText = pageText.take(15000)
        val prompt = """
            Ты — встроенный интеллектуальный ассистент браузера Lumina.
            Проанализируй содержимое веб-страницы и извлеки из неё все возможные задачи, инструкции, шаги, покупки, рецептурные ингредиенты или действия, которые пользователю имеет смысл выполнить или сохранить в чеклист.
            
            Заголовок: $pageTitle
            Контент:
            $safeText
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                structuredTaskModel.generateContent(prompt)
            }
            val jsonString = response.text ?: return emptyList()
            parseTasksFromJson(jsonString)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Task extraction failed, falling back to manual parsing", e)
            emptyList()
        }
    }

    private fun parseTasksFromJson(jsonString: String): List<ExtractedTask> {
        val result = mutableListOf<ExtractedTask>()
        try {
            val root = JSONObject(jsonString)
            val tasksArray = root.optJSONArray("tasks") ?: JSONArray()
            for (i in 0 until tasksArray.length()) {
                val item = tasksArray.getJSONObject(i)
                val text = item.optString("text", "").trim()
                val category = item.optString("category", "Действие").trim()
                if (text.isNotBlank()) {
                    result.add(ExtractedTask(text = text, category = category))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing task JSON", e)
        }
        return result
    }

    private fun buildContextPrompt(
        actionType: AiActionType,
        userQuery: String,
        pageTitle: String,
        pageUrl: String,
        pageText: String
    ): String {
        val baseContext = """
            Ты — Lumina AI, интеллектуальный контекстный ассистент веб-браузера нового поколения Lumina (по аналогии с передовыми браузерами вроде Comet).
            Твоя цель: предоставить пользователю мгновенный анализ, контекстный поиск и автоматизацию повседневных задач на основе текущей просматриваемой веб-страницы.
            
            КОНТЕКСТ ВЕБ-СТРАНИЦЫ:
            - Заголовок: $pageTitle
            - URL: $pageUrl
            - Текст страницы (выжимка):
            $pageText
            
            ФОРМАТИРОВАНИЕ И СТИЛЬ:
            - Пиши четко, структурированно, без вводной "воды".
            - Используй краткие заголовки, списки с маркерами (•) и выделение ключевых слов жирным шрифтом.
            - Отвечай на русском языке, если страница на русском или пользователь пишет на русском.
        """.trimIndent()

        val specificInstruction = when (actionType) {
            AiActionType.SUMMARIZE -> """
                ЗАДАЧА: Сделай лаконичную и ценную выжимку страницы.
                1. Одно предложение: в чём суть страницы.
                2. 3-4 ключевых тезиса с подробностями.
                3. Главный практический вывод или польза для читателя.
            """.trimIndent()

            AiActionType.EXTRACT_TASKS -> """
                ЗАДАЧА: Сформируй список конкретных действий, шагов, рекомендаций или задач, вытекающих из этого материала.
                Каждый пункт должен начинаться с глагола действия.
            """.trimIndent()

            AiActionType.FIND_KEY_FACTS -> """
                ЗАДАЧА: Найди все ключевые факты, важные цифры, даты, имена, аргументы и источники, упомянутые на странице.
            """.trimIndent()

            AiActionType.EXPLAIN -> """
                ЗАДАЧА: Объясни содержание страницы простыми словами так, чтобы понял любой человек без специальных знаний. Используй аналогии.
            """.trimIndent()

            AiActionType.TRANSLATE -> """
                ЗАДАЧА: Предоставь перевод основного содержания на русский язык в живом, качественном и связном стиле.
            """.trimIndent()

            AiActionType.DRAFT_MESSAGE -> """
                ЗАДАЧА: Составь черновик сообщения (или электронного письма), резюмирующий эту страницу или отвечающий на неё, готовый к отправке коллеге или другу.
            """.trimIndent()

            AiActionType.COMPARE_PAGE -> """
                ЗАДАЧА: Проведи сравнительный анализ.
                1. С какими популярными аналогами, сервисами или продуктами можно сравнить предмет страницы?
                2. Сильные и слабые стороны по сравнению с ними.
                3. Кому лучше всего подходит этот вариант.
            """.trimIndent()

            AiActionType.FACT_CHECK -> """
                ЗАДАЧА: Проверь факты и объективность страницы.
                1. Оцени авторитетность и прозрачность утверждений.
                2. Выдели проверяемые факты и потенциально субъективные мнения.
                3. Есть ли признаки манипуляции, кликбейта или устаревшей информации?
            """.trimIndent()

            AiActionType.CHAT -> """
                ЗАДАЧА: Пользователь задал конкретный вопрос по странице:
                "$userQuery"
                Ответь строго на этот вопрос, опираясь на контекст страницы. Если ответа на странице нет, ответь на основе своих общих знаний и честно отметь это.
            """.trimIndent()
        }

        return "$baseContext\n\n$specificInstruction"
    }
}
