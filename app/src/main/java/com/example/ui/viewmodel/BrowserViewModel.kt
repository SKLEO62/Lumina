package com.example.ui.viewmodel

import android.net.Uri
import android.util.Log
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.LuminaAiService
import com.example.data.model.AiActionType
import com.example.data.model.AiUiState
import com.example.data.model.Bookmark
import com.example.data.model.DownloadItem
import com.example.data.model.ExtractedTask
import com.example.data.model.HistoryItem
import com.example.data.model.LuminaThemeMode
import com.example.data.model.LuminaWallpaper
import com.example.data.model.PasswordEntry
import com.example.data.model.SearchEngine
import com.example.data.model.TabSpace
import com.example.data.model.WebNote
import com.example.data.model.WebTab
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "BrowserViewModel"

data class BrowserUiState(
    val tabs: List<WebTab> = listOf(WebTab()),
    val activeTabId: String = "",
    val isTabSwitcherOpen: Boolean = false,
    val isAiChatOpen: Boolean = false,
    val aiUiState: AiUiState = AiUiState.Idle,
    val selectedAiAction: AiActionType = AiActionType.SUMMARIZE,
    val aiPromptInput: String = "",
    val extractedTasks: List<ExtractedTask> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList(),
    val history: List<HistoryItem> = emptyList(),
    val isReaderMode: Boolean = false,
    val isOmnibarFocused: Boolean = false,
    val omnibarQueryText: String = "",
    val webViewActionTrigger: WebViewAction? = null,
    // Vault, Downloads, Themes, Onboarding
    val passwords: List<PasswordEntry> = emptyList(),
    val downloads: List<DownloadItem> = emptyList(),
    val currentTheme: LuminaThemeMode = LuminaThemeMode.OBSIDIAN,
    val selectedWallpaper: LuminaWallpaper = LuminaWallpaper.COSMIC_NEBULA,
    val customWallpaperPath: String? = null,
    val wallpaperDimming: Float = 0.45f,
    val wallpaperBlur: Float = 0f,
    val activeBookmarkCategory: String = "Все",
    val isBookmarkGridView: Boolean = true,
    val isWelcomeOpen: Boolean = true,
    val isPasswordManagerOpen: Boolean = false,
    val isDownloadsOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isMenuOpen: Boolean = false,
    // Chrome Parity Features:
    val isFindInPageOpen: Boolean = false,
    val findQuery: String = "",
    val findCurrentIndex: Int = 0,
    val findTotalMatches: Int = 0,
    val isHistoryOpen: Boolean = false,
    val isBookmarksOpen: Boolean = false,
    val isClearDataDialogOpen: Boolean = false,
    val textZoom: Int = 100,
    val isAdBlockEnabled: Boolean = true,
    // Spaces, Notes, QR Code & Search Engine
    val isQrCodeOpen: Boolean = false,
    val isNotesOpen: Boolean = false,
    val notes: List<WebNote> = emptyList(),
    val selectedSpace: TabSpace = TabSpace.ALL,
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,
    val recentlyClosedTabs: List<WebTab> = emptyList()
)

sealed interface WebViewAction {
    data object GoBack : WebViewAction
    data object GoForward : WebViewAction
    data object Reload : WebViewAction
    data class LoadUrl(val url: String) : WebViewAction
    data class FindQuery(val query: String) : WebViewAction
    data class FindNext(val forward: Boolean) : WebViewAction
    data object ClearFindMatches : WebViewAction
    data object ClearAllWebCache : WebViewAction
}

class BrowserViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val aiService: LuminaAiService = LuminaAiService()

    private val prefs: SharedPreferences = application.getSharedPreferences("lumina_browser_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        BrowserUiState(
            isWelcomeOpen = !prefs.getBoolean("welcome_dismissed", false),
            currentTheme = try {
                LuminaThemeMode.valueOf(prefs.getString("current_theme", LuminaThemeMode.OBSIDIAN.name) ?: LuminaThemeMode.OBSIDIAN.name)
            } catch (_: Exception) {
                LuminaThemeMode.OBSIDIAN
            },
            searchEngine = try {
                SearchEngine.valueOf(prefs.getString("search_engine", SearchEngine.GOOGLE.name) ?: SearchEngine.GOOGLE.name)
            } catch (_: Exception) {
                SearchEngine.GOOGLE
            },
            isAdBlockEnabled = prefs.getBoolean("adblock_enabled", true)
        )
    )
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    constructor(application: Application, aiService: LuminaAiService) : this(application) {
        // aiService is initialized
    }

    private var currentAiJob: Job? = null

    init {
        val initialTab = WebTab(
            title = "Новая вкладка",
            url = "lumina://newtab"
        )
        _uiState.update {
            it.copy(
                tabs = listOf(initialTab),
                activeTabId = initialTab.id,
                bookmarks = listOf(
                    Bookmark(title = "Google Search", url = "https://www.google.com", category = "Поиск", isPinned = true),
                    Bookmark(title = "Wikipedia", url = "https://ru.wikipedia.org", category = "Учеба", isPinned = true),
                    Bookmark(title = "GitHub", url = "https://github.com", category = "Работа", isPinned = true),
                    Bookmark(title = "Perplexity AI", url = "https://www.perplexity.ai", category = "Инструменты", isPinned = true),
                    Bookmark(title = "Hacker News", url = "https://news.ycombinator.com", category = "Медиа", isPinned = false),
                    Bookmark(title = "Habr", url = "https://habr.com", category = "Медиа", isPinned = false),
                    Bookmark(title = "YouTube", url = "https://youtube.com", category = "Медиа", isPinned = false)
                ),
                passwords = listOf(
                    PasswordEntry(
                        siteName = "GitHub",
                        domain = "github.com",
                        username = "developer@example.com",
                        password = "ghp_secureKey892#!"
                    ),
                    PasswordEntry(
                        siteName = "Google",
                        domain = "google.com",
                        username = "user@gmail.com",
                        password = "Gg_pass9812*&"
                    )
                ),
                downloads = listOf(
                    DownloadItem(
                        fileName = "lumina_guide.pdf",
                        url = "https://lumina.browser/docs/guide.pdf",
                        mimeType = "application/pdf",
                        fileSizeBytes = 2450000,
                        isCompleted = true
                    )
                ),
                notes = listOf(
                    WebNote(
                        url = "https://github.com",
                        domain = "github.com",
                        text = "Проверить новые релизы Jetpack Compose и Kotlin 2.0"
                    ),
                    WebNote(
                        url = "https://news.ycombinator.com",
                        domain = "news.ycombinator.com",
                        text = "Интересные обсуждения локальных моделей ИИ"
                    )
                )
            )
        }
    }

    val activeTab: WebTab?
        get() = _uiState.value.tabs.find { it.id == _uiState.value.activeTabId }

    fun openUrl(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return

        val targetUrl = formatUrlOrSearch(trimmed)
        val activeId = _uiState.value.activeTabId
        val tab = activeTab

        _uiState.update { state ->
            val updatedTabs = state.tabs.map { t ->
                if (t.id == activeId) {
                    t.copy(url = targetUrl, isLoading = true, progress = 10)
                } else {
                    t
                }
            }
            state.copy(
                tabs = updatedTabs,
                omnibarQueryText = targetUrl,
                isOmnibarFocused = false,
                isTabSwitcherOpen = false,
                isFindInPageOpen = false,
                webViewActionTrigger = WebViewAction.LoadUrl(targetUrl)
            )
        }

        if (tab?.isIncognito != true) {
            addToHistory(title = targetUrl, url = targetUrl)
        }
    }

    fun onOmnibarQueryChanged(query: String) {
        _uiState.update { it.copy(omnibarQueryText = query) }
    }

    fun setOmnibarFocused(focused: Boolean) {
        _uiState.update {
            it.copy(
                isOmnibarFocused = focused,
                omnibarQueryText = if (focused && activeTab?.url != "lumina://newtab") {
                    activeTab?.url ?: ""
                } else {
                    it.omnibarQueryText
                }
            )
        }
    }

    fun createNewTab(initialUrl: String = "lumina://newtab", isIncognito: Boolean = false) {
        val newTab = WebTab(
            title = when {
                isIncognito && initialUrl == "lumina://newtab" -> "Инкогнито"
                initialUrl == "lumina://newtab" -> "Новая вкладка"
                else -> "Загрузка..."
            },
            url = initialUrl,
            isIncognito = isIncognito
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newTab.id,
                isTabSwitcherOpen = false,
                isFindInPageOpen = false,
                omnibarQueryText = "",
                webViewActionTrigger = if (initialUrl != "lumina://newtab") WebViewAction.LoadUrl(initialUrl) else null
            )
        }
    }

    fun closeTab(tabId: String) {
        _uiState.update { state ->
            val closingTab = state.tabs.find { it.id == tabId }
            val remaining = state.tabs.filterNot { it.id == tabId }
            val newRecentlyClosed = if (closingTab != null && !closingTab.isIncognito && closingTab.url != "lumina://newtab") {
                (listOf(closingTab) + state.recentlyClosedTabs).take(10)
            } else {
                state.recentlyClosedTabs
            }
            if (remaining.isEmpty()) {
                val fallbackTab = WebTab(title = "Новая вкладка", url = "lumina://newtab")
                state.copy(
                    tabs = listOf(fallbackTab),
                    activeTabId = fallbackTab.id,
                    recentlyClosedTabs = newRecentlyClosed,
                    omnibarQueryText = ""
                )
            } else {
                val nextActiveId = if (state.activeTabId == tabId) {
                    remaining.last().id
                } else {
                    state.activeTabId
                }
                state.copy(
                    tabs = remaining,
                    activeTabId = nextActiveId,
                    recentlyClosedTabs = newRecentlyClosed
                )
            }
        }
    }

    fun duplicateTab(tabId: String) {
        _uiState.update { state ->
            val target = state.tabs.find { it.id == tabId } ?: return@update state
            val cloned = target.copy(
                id = java.util.UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis()
            )
            val index = state.tabs.indexOfFirst { it.id == tabId }
            val mutable = state.tabs.toMutableList()
            if (index >= 0 && index < mutable.size) {
                mutable.add(index + 1, cloned)
            } else {
                mutable.add(cloned)
            }
            state.copy(
                tabs = mutable,
                activeTabId = cloned.id,
                isTabSwitcherOpen = false
            )
        }
    }

    fun closeOtherTabs(tabId: String) {
        _uiState.update { state ->
            val kept = state.tabs.find { it.id == tabId } ?: return@update state
            val closed = state.tabs.filterNot { it.id == tabId || it.isIncognito }
            state.copy(
                tabs = listOf(kept),
                activeTabId = kept.id,
                recentlyClosedTabs = (closed + state.recentlyClosedTabs).take(10)
            )
        }
    }

    fun restoreLastClosedTab() {
        val recent = _uiState.value.recentlyClosedTabs
        if (recent.isEmpty()) return
        val lastClosed = recent.first()
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + lastClosed,
                activeTabId = lastClosed.id,
                recentlyClosedTabs = recent.drop(1)
            )
        }
    }

    fun closeAllTabs(isIncognitoOnly: Boolean = false) {
        _uiState.update { state ->
            val remaining = if (isIncognitoOnly) {
                state.tabs.filterNot { it.isIncognito }
            } else {
                state.tabs.filter { it.isIncognito }
            }

            if (remaining.isEmpty()) {
                val freshTab = WebTab(title = "Новая вкладка", url = "lumina://newtab")
                state.copy(
                    tabs = listOf(freshTab),
                    activeTabId = freshTab.id,
                    isTabSwitcherOpen = false,
                    omnibarQueryText = ""
                )
            } else {
                state.copy(
                    tabs = remaining,
                    activeTabId = remaining.last().id,
                    isTabSwitcherOpen = false
                )
            }
        }
    }

    fun selectTab(tabId: String) {
        _uiState.update { state ->
            state.copy(
                activeTabId = tabId,
                isTabSwitcherOpen = false,
                isFindInPageOpen = false,
                omnibarQueryText = state.tabs.find { it.id == tabId }?.url.orEmpty()
            )
        }
    }

    fun toggleTabSwitcher() {
        _uiState.update { it.copy(isTabSwitcherOpen = !it.isTabSwitcherOpen, isFindInPageOpen = false) }
    }

    fun updateTabInfo(
        tabId: String,
        title: String? = null,
        url: String? = null,
        canGoBack: Boolean? = null,
        canGoForward: Boolean? = null,
        isLoading: Boolean? = null,
        progress: Int? = null
    ) {
        val tab = _uiState.value.tabs.find { it.id == tabId }

        _uiState.update { state ->
            val updatedTabs = state.tabs.map { t ->
                if (t.id == tabId) {
                    t.copy(
                        title = title ?: t.title,
                        url = url ?: t.url,
                        canGoBack = canGoBack ?: t.canGoBack,
                        canGoForward = canGoForward ?: t.canGoForward,
                        isLoading = isLoading ?: t.isLoading,
                        progress = progress ?: t.progress
                    )
                } else {
                    t
                }
            }
            state.copy(tabs = updatedTabs)
        }

        if (tab?.isIncognito != true && url != null && url != "lumina://newtab" && url.isNotBlank()) {
            addToHistory(title = title ?: url, url = url)
        }
    }

    fun updateTabPageText(tabId: String, text: String) {
        _uiState.update { state ->
            val updated = state.tabs.map { tab ->
                if (tab.id == tabId) tab.copy(pageText = text) else tab
            }
            state.copy(tabs = updated)
        }
    }

    fun triggerGoBack() {
        _uiState.update { it.copy(webViewActionTrigger = WebViewAction.GoBack) }
    }

    fun triggerGoForward() {
        _uiState.update { it.copy(webViewActionTrigger = WebViewAction.GoForward) }
    }

    fun triggerReload() {
        _uiState.update { it.copy(webViewActionTrigger = WebViewAction.Reload) }
    }

    fun consumeActionTrigger() {
        _uiState.update { it.copy(webViewActionTrigger = null) }
    }

    fun toggleDesktopMode() {
        val activeId = _uiState.value.activeTabId
        _uiState.update { state ->
            val updated = state.tabs.map {
                if (it.id == activeId) it.copy(isDesktopMode = !it.isDesktopMode) else it
            }
            state.copy(
                tabs = updated,
                webViewActionTrigger = WebViewAction.Reload
            )
        }
    }

    fun toggleReaderMode() {
        _uiState.update { it.copy(isReaderMode = !it.isReaderMode) }
    }

    fun toggleBookmark() {
        val tab = activeTab ?: return
        if (tab.url == "lumina://newtab") return

        _uiState.update { state ->
            val isBookmarked = state.bookmarks.any { it.url == tab.url }
            val newBookmarks = if (isBookmarked) {
                state.bookmarks.filterNot { it.url == tab.url }
            } else {
                state.bookmarks + Bookmark(title = tab.title, url = tab.url)
            }
            state.copy(bookmarks = newBookmarks)
        }
    }

    fun isCurrentTabBookmarked(): Boolean {
        val url = activeTab?.url ?: return false
        return _uiState.value.bookmarks.any { it.url == url }
    }

    private fun addToHistory(title: String, url: String) {
        if (url == "lumina://newtab") return
        _uiState.update { state ->
            val filtered = state.history.filterNot { it.url == url }
            val newItem = HistoryItem(title = title, url = url)
            state.copy(history = listOf(newItem) + filtered.take(100))
        }
    }

    // --- FIND IN PAGE (CHROME FEATURE) ---
    fun openFindInPage() {
        _uiState.update { it.copy(isFindInPageOpen = true, findQuery = "", findCurrentIndex = 0, findTotalMatches = 0) }
    }

    fun closeFindInPage() {
        _uiState.update {
            it.copy(
                isFindInPageOpen = false,
                findQuery = "",
                findCurrentIndex = 0,
                findTotalMatches = 0,
                webViewActionTrigger = WebViewAction.ClearFindMatches
            )
        }
    }

    fun onFindQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                findQuery = query,
                webViewActionTrigger = WebViewAction.FindQuery(query)
            )
        }
    }

    fun findNext(forward: Boolean) {
        _uiState.update {
            it.copy(webViewActionTrigger = WebViewAction.FindNext(forward))
        }
    }

    fun updateFindResult(activeMatchOrdinal: Int, numberOfMatches: Int) {
        _uiState.update {
            it.copy(
                findCurrentIndex = activeMatchOrdinal,
                findTotalMatches = numberOfMatches
            )
        }
    }

    // --- HISTORY MANAGER (CHROME FEATURE) ---
    fun openHistory() {
        _uiState.update { it.copy(isHistoryOpen = true) }
    }

    fun closeHistory() {
        _uiState.update { it.copy(isHistoryOpen = false) }
    }

    fun deleteHistoryItem(id: String) {
        _uiState.update { state ->
            state.copy(history = state.history.filterNot { it.id == id })
        }
    }

    fun clearAllHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    // --- BOOKMARKS MANAGER (CHROME FEATURE) ---
    fun openBookmarks() {
        _uiState.update { it.copy(isBookmarksOpen = true) }
    }

    fun closeBookmarks() {
        _uiState.update { it.copy(isBookmarksOpen = false) }
    }

    fun addBookmark(bookmark: Bookmark) {
        _uiState.update { it.copy(bookmarks = it.bookmarks + bookmark) }
    }

    fun deleteBookmark(id: String) {
        _uiState.update { state ->
            state.copy(bookmarks = state.bookmarks.filterNot { it.id == id })
        }
    }

    fun togglePinBookmark(id: String) {
        _uiState.update { state ->
            val updated = state.bookmarks.map { b ->
                if (b.id == id) b.copy(isPinned = !b.isPinned) else b
            }
            state.copy(bookmarks = updated)
        }
    }

    fun editBookmark(id: String, newTitle: String, newUrl: String, newCategory: String) {
        _uiState.update { state ->
            val updated = state.bookmarks.map { b ->
                if (b.id == id) b.copy(
                    title = newTitle.trim(),
                    url = newUrl.trim(),
                    category = newCategory.trim().ifBlank { "Общие" }
                ) else b
            }
            state.copy(bookmarks = updated)
        }
    }

    fun setBookmarkCategory(category: String) {
        _uiState.update { it.copy(activeBookmarkCategory = category) }
    }

    fun toggleBookmarkViewMode() {
        _uiState.update { it.copy(isBookmarkGridView = !it.isBookmarkGridView) }
    }

    fun setWallpaper(wallpaper: LuminaWallpaper) {
        _uiState.update { it.copy(selectedWallpaper = wallpaper) }
    }

    fun setCustomWallpaperPath(path: String) {
        _uiState.update {
            it.copy(
                customWallpaperPath = path,
                selectedWallpaper = LuminaWallpaper.CUSTOM
            )
        }
    }

    fun removeCustomWallpaper() {
        _uiState.update {
            it.copy(
                customWallpaperPath = null,
                selectedWallpaper = LuminaWallpaper.COSMIC_NEBULA
            )
        }
    }

    fun setWallpaperDimming(dimming: Float) {
        _uiState.update { it.copy(wallpaperDimming = dimming.coerceIn(0.1f, 0.9f)) }
    }

    fun setWallpaperBlur(blur: Float) {
        _uiState.update { it.copy(wallpaperBlur = blur.coerceIn(0f, 25f)) }
    }

    // --- CLEAR BROWSING DATA (CHROME FEATURE) ---
    fun openClearDataDialog() {
        _uiState.update { it.copy(isClearDataDialogOpen = true) }
    }

    fun closeClearDataDialog() {
        _uiState.update { it.copy(isClearDataDialogOpen = false) }
    }

    fun clearBrowsingData(clearHist: Boolean, clearCache: Boolean, clearPass: Boolean, clearDown: Boolean) {
        _uiState.update { state ->
            state.copy(
                history = if (clearHist) emptyList() else state.history,
                passwords = if (clearPass) emptyList() else state.passwords,
                downloads = if (clearDown) emptyList() else state.downloads,
                webViewActionTrigger = if (clearCache) WebViewAction.ClearAllWebCache else state.webViewActionTrigger
            )
        }
    }

    // --- TEXT ZOOM & AD BLOCK (CHROME FEATURES) ---
    fun cycleTextZoom() {
        val nextZoom = when (_uiState.value.textZoom) {
            100 -> 125
            125 -> 150
            150 -> 80
            else -> 100
        }
        _uiState.update { it.copy(textZoom = nextZoom) }
    }

    fun toggleAdBlock() {
        val newState = !_uiState.value.isAdBlockEnabled
        try { prefs?.edit()?.putBoolean("adblock_enabled", newState)?.apply() } catch (_: Exception) {}
        _uiState.update { it.copy(isAdBlockEnabled = newState) }
    }

    // --- THEMES & SETTINGS ---
    fun setTheme(theme: LuminaThemeMode) {
        try { prefs?.edit()?.putString("current_theme", theme.name)?.apply() } catch (_: Exception) {}
        _uiState.update { it.copy(currentTheme = theme) }
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }

    fun openMenu() {
        _uiState.update { it.copy(isMenuOpen = true) }
    }

    fun closeMenu() {
        _uiState.update { it.copy(isMenuOpen = false) }
    }

    // --- ONBOARDING / WELCOME SCREEN ---
    fun openWelcome() {
        _uiState.update { it.copy(isWelcomeOpen = true) }
    }

    fun dismissWelcome(dontShowAgain: Boolean) {
        if (dontShowAgain) {
            try { prefs?.edit()?.putBoolean("welcome_dismissed", true)?.apply() } catch (_: Exception) {}
        }
        _uiState.update { it.copy(isWelcomeOpen = false) }
    }

    // --- PASSWORD MANAGER ---
    fun openPasswordManager() {
        _uiState.update { it.copy(isPasswordManagerOpen = true) }
    }

    fun closePasswordManager() {
        _uiState.update { it.copy(isPasswordManagerOpen = false) }
    }

    fun savePassword(entry: PasswordEntry) {
        _uiState.update { state ->
            val existing = state.passwords.filterNot { it.id == entry.id }
            state.copy(passwords = existing + entry)
        }
    }

    fun deletePassword(id: String) {
        _uiState.update { state ->
            state.copy(passwords = state.passwords.filterNot { it.id == id })
        }
    }

    // --- DOWNLOADS ---
    fun openDownloads() {
        _uiState.update { it.copy(isDownloadsOpen = true) }
    }

    fun closeDownloads() {
        _uiState.update { it.copy(isDownloadsOpen = false) }
    }

    fun addDownload(url: String, fileName: String, mimeType: String, length: Long) {
        val item = DownloadItem(
            fileName = fileName,
            url = url,
            mimeType = mimeType,
            fileSizeBytes = if (length > 0) length else 1024 * 1024,
            isCompleted = true
        )
        _uiState.update { state ->
            state.copy(
                downloads = listOf(item) + state.downloads,
                isDownloadsOpen = true
            )
        }
    }

    fun deleteDownload(id: String) {
        _uiState.update { state ->
            state.copy(downloads = state.downloads.filterNot { it.id == id })
        }
    }

    fun clearAllDownloads() {
        _uiState.update { it.copy(downloads = emptyList()) }
    }

    // --- GEMINI AI INTEGRATION ---

    fun openAiChat(action: AiActionType = AiActionType.SUMMARIZE) {
        _uiState.update {
            it.copy(
                isAiChatOpen = true,
                selectedAiAction = action
            )
        }
        executeAiAction(action)
    }

    fun closeAiChat() {
        currentAiJob?.cancel()
        _uiState.update { it.copy(isAiChatOpen = false) }
    }

    fun onAiPromptInputChanged(input: String) {
        _uiState.update { it.copy(aiPromptInput = input) }
    }

    fun sendCustomAiQuery() {
        val query = _uiState.value.aiPromptInput.trim()
        if (query.isEmpty()) return
        _uiState.update { it.copy(aiPromptInput = "") }
        executeAiAction(AiActionType.CHAT, customQuery = query)
    }

    fun executeAiAction(actionType: AiActionType, customQuery: String = "") {
        currentAiJob?.cancel()

        val currentTab = activeTab ?: return
        _uiState.update {
            it.copy(
                selectedAiAction = actionType,
                aiUiState = AiUiState.Loading
            )
        }

        currentAiJob = viewModelScope.launch {
            try {
                if (actionType == AiActionType.EXTRACT_TASKS) {
                    launch {
                        val tasks = aiService.extractTasks(
                            pageTitle = currentTab.title,
                            pageText = currentTab.pageText
                        )
                        _uiState.update { it.copy(extractedTasks = tasks) }
                    }
                }

                aiService.streamPageAction(
                    actionType = actionType,
                    userQuery = customQuery,
                    pageTitle = currentTab.title,
                    pageUrl = currentTab.url,
                    pageText = currentTab.pageText
                ).collect { partialText ->
                    _uiState.update {
                        it.copy(aiUiState = AiUiState.Streaming(partialText))
                    }
                }

                val finalState = _uiState.value.aiUiState
                if (finalState is AiUiState.Streaming) {
                    _uiState.update {
                        it.copy(aiUiState = AiUiState.Success(finalState.partialText))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI action failed", e)
                _uiState.update {
                    it.copy(
                        aiUiState = AiUiState.Error(
                            e.localizedMessage ?: "Не удалось сгенерировать ответ. Попробуйте еще раз."
                        )
                    )
                }
            }
        }
    }

    fun toggleTaskDone(taskId: String) {
        _uiState.update { state ->
            val updated = state.extractedTasks.map { task ->
                if (task.id == taskId) task.copy(isDone = !task.isDone) else task
            }
            state.copy(extractedTasks = updated)
        }
    }

    fun addTask(text: String, category: String = "Действие") {
        if (text.isBlank()) return
        val newTask = ExtractedTask(text = text.trim(), category = category)
        _uiState.update { it.copy(extractedTasks = it.extractedTasks + newTask) }
    }

    // --- QR CODE SHARING ---
    fun openQrCode() {
        _uiState.update { it.copy(isQrCodeOpen = true) }
    }

    fun closeQrCode() {
        _uiState.update { it.copy(isQrCodeOpen = false) }
    }

    // --- WEB NOTES ---
    fun openNotes() {
        _uiState.update { it.copy(isNotesOpen = true) }
    }

    fun closeNotes() {
        _uiState.update { it.copy(isNotesOpen = false) }
    }

    fun addNote(url: String, domain: String, text: String) {
        val note = WebNote(
            url = url,
            domain = domain,
            text = text
        )
        _uiState.update { it.copy(notes = listOf(note) + it.notes) }
    }

    fun deleteNote(id: String) {
        _uiState.update { state ->
            state.copy(notes = state.notes.filterNot { it.id == id })
        }
    }

    // --- TAB SPACES & PINNING ---
    fun selectSpace(space: TabSpace) {
        _uiState.update { it.copy(selectedSpace = space) }
    }

    fun togglePinTab(tabId: String) {
        _uiState.update { state ->
            val updated = state.tabs.map { tab ->
                if (tab.id == tabId) tab.copy(isPinned = !tab.isPinned) else tab
            }
            state.copy(tabs = updated)
        }
    }

    // --- SWIPE TAB NAVIGATION ---
    fun switchToNextTab() {
        val tabs = _uiState.value.tabs
        if (tabs.size <= 1) return
        val currentIndex = tabs.indexOfFirst { it.id == _uiState.value.activeTabId }
        val nextIndex = if (currentIndex in 0 until tabs.size - 1) currentIndex + 1 else 0
        selectTab(tabs[nextIndex].id)
    }

    fun switchToPreviousTab() {
        val tabs = _uiState.value.tabs
        if (tabs.size <= 1) return
        val currentIndex = tabs.indexOfFirst { it.id == _uiState.value.activeTabId }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else tabs.size - 1
        selectTab(tabs[prevIndex].id)
    }

    // --- SEARCH ENGINE ---
    fun setSearchEngine(engine: SearchEngine) {
        try { prefs?.edit()?.putString("search_engine", engine.name)?.apply() } catch (_: Exception) {}
        _uiState.update { it.copy(searchEngine = engine) }
    }

    private fun formatUrlOrSearch(input: String): String {
        return when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") && !input.contains(" ") -> "https://$input"
            else -> "${_uiState.value.searchEngine.searchUrlPrefix}${Uri.encode(input)}"
        }
    }
}
