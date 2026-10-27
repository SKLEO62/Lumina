package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AiChatScreen
import com.example.ui.components.BookmarksSheet
import com.example.ui.components.ClearBrowsingDataDialog
import com.example.ui.components.DownloadsSheet
import com.example.ui.components.FindInPageBar
import com.example.ui.components.HistorySheet
import com.example.ui.components.LuminaBottomBar
import com.example.ui.components.LuminaWebView
import com.example.ui.components.NewTabScreen
import com.example.ui.components.NotesSheet
import com.example.ui.components.Omnibar
import com.example.ui.components.PasswordManagerSheet
import com.example.ui.components.QrCodeSheet
import com.example.ui.components.ReaderModeView
import com.example.ui.components.LuminaMenuBottomSheet
import com.example.ui.components.SettingsSheet
import com.example.ui.components.TabSwitcherView
import com.example.ui.components.WelcomeScreen
import com.example.ui.theme.LuminaTheme
import com.example.ui.viewmodel.BrowserViewModel
import com.example.util.LuminaTtsHelper

@Composable
fun LuminaBrowserScreen(
    viewModel: BrowserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeTab = viewModel.activeTab
    val context = LocalContext.current

    // Text to Speech Engine instance for Read Aloud
    val ttsHelper = remember(context) { LuminaTtsHelper(context) }
    val isSpeaking by ttsHelper.isSpeaking.collectAsState()

    DisposableEffect(ttsHelper) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    // Dynamically apply selected Lumina theme
    LuminaTheme(themeMode = uiState.currentTheme) {
        if (uiState.isWelcomeOpen) {
            WelcomeScreen(
                currentTheme = uiState.currentTheme,
                onSelectTheme = viewModel::setTheme,
                searchEngine = uiState.searchEngine,
                onSelectSearchEngine = viewModel::setSearchEngine,
                isAdBlockEnabled = uiState.isAdBlockEnabled,
                onToggleAdBlock = viewModel::toggleAdBlock,
                selectedWallpaper = uiState.selectedWallpaper,
                onSelectWallpaper = viewModel::setWallpaper,
                onDismiss = { dontShowAgain ->
                    viewModel.dismissWelcome(dontShowAgain)
                }
            )
        } else if (uiState.isAiChatOpen) {
            // Standalone Dedicated AI Chat Full-Screen View
            AiChatScreen(
                activeTab = activeTab,
                aiUiState = uiState.aiUiState,
                selectedAction = uiState.selectedAiAction,
                promptInput = uiState.aiPromptInput,
                extractedTasks = uiState.extractedTasks,
                onSelectAction = { action -> viewModel.executeAiAction(action) },
                onPromptInputChange = viewModel::onAiPromptInputChanged,
                onSendCustomQuery = viewModel::sendCustomAiQuery,
                onToggleTask = viewModel::toggleTaskDone,
                onAddTask = viewModel::addTask,
                onDismiss = viewModel::closeAiChat
            )
        } else {
            // Back handling
            BackHandler(
                enabled = uiState.isReaderMode ||
                        uiState.isQrCodeOpen ||
                        uiState.isNotesOpen ||
                        uiState.isTabSwitcherOpen ||
                        uiState.isFindInPageOpen ||
                        uiState.isHistoryOpen ||
                        uiState.isBookmarksOpen ||
                        uiState.isPasswordManagerOpen ||
                        uiState.isDownloadsOpen ||
                        uiState.isSettingsOpen ||
                        uiState.isMenuOpen
            ) {
                when {
                    uiState.isReaderMode -> {
                        ttsHelper.stop()
                        viewModel.toggleReaderMode()
                    }
                    uiState.isQrCodeOpen -> viewModel.closeQrCode()
                    uiState.isNotesOpen -> viewModel.closeNotes()
                    uiState.isFindInPageOpen -> viewModel.closeFindInPage()
                    uiState.isHistoryOpen -> viewModel.closeHistory()
                    uiState.isBookmarksOpen -> viewModel.closeBookmarks()
                    uiState.isPasswordManagerOpen -> viewModel.closePasswordManager()
                    uiState.isDownloadsOpen -> viewModel.closeDownloads()
                    uiState.isSettingsOpen -> viewModel.closeSettings()
                    uiState.isMenuOpen -> viewModel.closeMenu()
                    uiState.isTabSwitcherOpen -> viewModel.toggleTabSwitcher()
                }
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    Column {
                        FindInPageBar(
                            isVisible = uiState.isFindInPageOpen,
                            query = uiState.findQuery,
                            currentIndex = uiState.findCurrentIndex,
                            totalMatches = uiState.findTotalMatches,
                            onQueryChange = viewModel::onFindQueryChanged,
                            onFindNext = { viewModel.findNext(forward = true) },
                            onFindPrevious = { viewModel.findNext(forward = false) },
                            onClose = viewModel::closeFindInPage
                        )
                        if (!uiState.isTabSwitcherOpen && !uiState.isReaderMode && activeTab != null && activeTab.url != "lumina://newtab") {
                            Omnibar(
                                isTopBar = true,
                                activeTab = activeTab,
                                tabCount = uiState.tabs.size,
                                isFocused = uiState.isOmnibarFocused,
                                queryText = uiState.omnibarQueryText,
                                isBookmarked = viewModel.isCurrentTabBookmarked(),
                                onQueryChange = viewModel::onOmnibarQueryChanged,
                                onFocusChange = viewModel::setOmnibarFocused,
                                onSubmitUrl = viewModel::openUrl,
                                onOpenAiSheet = { viewModel.openAiChat() },
                                onToggleTabs = viewModel::toggleTabSwitcher,
                                onGoBack = viewModel::triggerGoBack,
                                onGoForward = viewModel::triggerGoForward,
                                onReload = viewModel::triggerReload,
                                onToggleBookmark = viewModel::toggleBookmark,
                                onToggleDesktop = viewModel::toggleDesktopMode,
                                onToggleReaderMode = viewModel::toggleReaderMode,
                                onOpenQrCode = viewModel::openQrCode,
                                onOpenNotes = viewModel::openNotes,
                                onNextTab = viewModel::switchToNextTab,
                                onPrevTab = viewModel::switchToPreviousTab,
                                onShare = {
                                    val url = activeTab.url
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "${activeTab.title}\n$url")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Поделиться ссылкой")
                                    context.startActivity(shareIntent)
                                },
                                onNewTab = { viewModel.createNewTab(isIncognito = false) },
                                onNewIncognitoTab = { viewModel.createNewTab(isIncognito = true) },
                                onOpenPasswordManager = viewModel::openPasswordManager,
                                onOpenDownloads = viewModel::openDownloads,
                                onOpenSettings = viewModel::openSettings,
                                onOpenWelcome = viewModel::openWelcome,
                                onOpenFindInPage = viewModel::openFindInPage,
                                onOpenHistory = viewModel::openHistory,
                                onOpenBookmarks = viewModel::openBookmarks,
                                onOpenClearData = viewModel::openClearDataDialog,
                                textZoom = uiState.textZoom,
                                onCycleZoom = viewModel::cycleTextZoom,
                                isAdBlockEnabled = uiState.isAdBlockEnabled,
                                onToggleAdBlock = viewModel::toggleAdBlock
                            )
                        }
                    }
                },
                bottomBar = {
                    if (!uiState.isTabSwitcherOpen && !uiState.isReaderMode) {
                        LuminaBottomBar(
                            activeTab = activeTab,
                            tabCount = uiState.tabs.size,
                            isHomeSelected = activeTab?.url == "lumina://newtab" || activeTab == null,
                            onGoBack = viewModel::triggerGoBack,
                            onGoForward = viewModel::triggerGoForward,
                            onGoHome = { viewModel.openUrl("lumina://newtab") },
                            onToggleTabs = viewModel::toggleTabSwitcher,
                            onOpenMenu = viewModel::openMenu
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        )
                ) {
                    Crossfade(targetState = uiState.isTabSwitcherOpen, label = "tab_switcher_crossfade") { isOpen ->
                        if (isOpen) {
                            TabSwitcherView(
                                tabs = uiState.tabs,
                                activeTabId = uiState.activeTabId,
                                selectedSpace = uiState.selectedSpace,
                                onSelectSpace = viewModel::selectSpace,
                                onTogglePinTab = viewModel::togglePinTab,
                                onSelectTab = viewModel::selectTab,
                                onCloseTab = viewModel::closeTab,
                                onCloseAll = { isIncognito -> viewModel.closeAllTabs(isIncognitoOnly = isIncognito) },
                                onNewTab = { isIncognito -> viewModel.createNewTab(isIncognito = isIncognito) },
                                onDismiss = viewModel::toggleTabSwitcher,
                                recentlyClosedCount = uiState.recentlyClosedTabs.size,
                                onRestoreLastClosed = viewModel::restoreLastClosedTab,
                                onDuplicateTab = viewModel::duplicateTab
                            )
                        } else {
                            val currentTab = activeTab
                            if (currentTab?.url == "lumina://newtab" || currentTab == null) {
                                NewTabScreen(
                                    bookmarks = uiState.bookmarks,
                                    tasks = uiState.extractedTasks,
                                    selectedWallpaper = uiState.selectedWallpaper,
                                    customWallpaperPath = uiState.customWallpaperPath,
                                    wallpaperDimming = uiState.wallpaperDimming,
                                    wallpaperBlur = uiState.wallpaperBlur,
                                    activeBookmarkCategory = uiState.activeBookmarkCategory,
                                    isBookmarkGridView = uiState.isBookmarkGridView,
                                    onOpenUrl = viewModel::openUrl,
                                    onOpenAiWithPrompt = { prompt ->
                                        viewModel.openAiChat()
                                        viewModel.onAiPromptInputChanged(prompt)
                                        viewModel.sendCustomAiQuery()
                                    },
                                    onOpenTasksSheet = {
                                        viewModel.openAiChat(com.example.data.model.AiActionType.EXTRACT_TASKS)
                                    },
                                    onAddBookmark = viewModel::addBookmark,
                                    onDeleteBookmark = viewModel::deleteBookmark,
                                    onTogglePinBookmark = viewModel::togglePinBookmark,
                                    onEditBookmark = viewModel::editBookmark,
                                    onSelectBookmarkCategory = viewModel::setBookmarkCategory,
                                    onToggleBookmarkViewMode = viewModel::toggleBookmarkViewMode,
                                    onSelectWallpaper = viewModel::setWallpaper,
                                    onSetCustomWallpaper = viewModel::setCustomWallpaperPath,
                                    onRemoveCustomWallpaper = viewModel::removeCustomWallpaper,
                                    onSetWallpaperDimming = viewModel::setWallpaperDimming,
                                    onSetWallpaperBlur = viewModel::setWallpaperBlur
                                )
                            } else {
                                LuminaWebView(
                                    tab = currentTab,
                                    textZoom = uiState.textZoom,
                                    actionTrigger = uiState.webViewActionTrigger,
                                    onConsumeAction = viewModel::consumeActionTrigger,
                                    onUpdateTab = { title, url, canGoBack, canGoForward, isLoading, progress ->
                                        viewModel.updateTabInfo(
                                            tabId = currentTab.id,
                                            title = title,
                                            url = url,
                                            canGoBack = canGoBack,
                                            canGoForward = canGoForward,
                                            isLoading = isLoading,
                                            progress = progress
                                        )
                                    },
                                    onExtractPageText = { text ->
                                        viewModel.updateTabPageText(currentTab.id, text)
                                    },
                                    onDownloadRequested = { url, fileName, mimeType, length ->
                                        viewModel.addDownload(url, fileName, mimeType, length)
                                    },
                                    onFindResult = { activeMatchOrdinal, numberOfMatches ->
                                        viewModel.updateFindResult(activeMatchOrdinal, numberOfMatches)
                                    }
                                )
                            }
                        }
                    }

                    // Reader Mode distraction-free overlay
                    AnimatedVisibility(
                        visible = uiState.isReaderMode && activeTab != null,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        if (activeTab != null) {
                            ReaderModeView(
                                tab = activeTab,
                                isSpeaking = isSpeaking,
                                onToggleSpeak = { textToRead ->
                                    ttsHelper.toggleSpeak(textToRead)
                                },
                                onClose = {
                                    ttsHelper.stop()
                                    viewModel.toggleReaderMode()
                                }
                            )
                        }
                    }
                }

                // QR Code Sharing Sheet
                if (uiState.isQrCodeOpen && activeTab != null) {
                    QrCodeSheet(
                        url = activeTab.url,
                        title = activeTab.title,
                        onDismiss = viewModel::closeQrCode
                    )
                }

                // Sticky Web Notes Sheet
                if (uiState.isNotesOpen) {
                    val currentUrl = activeTab?.url ?: ""
                    val domain = try {
                        android.net.Uri.parse(currentUrl).host ?: currentUrl
                    } catch (e: Exception) {
                        currentUrl
                    }
                    NotesSheet(
                        notes = uiState.notes,
                        currentUrl = currentUrl,
                        currentDomain = domain,
                        onAddNote = { url, noteDomain, text ->
                            viewModel.addNote(url, noteDomain, text)
                        },
                        onDeleteNote = viewModel::deleteNote,
                        onDismiss = viewModel::closeNotes
                    )
                }

                // History Sheet
                if (uiState.isHistoryOpen) {
                    HistorySheet(
                        history = uiState.history,
                        onOpenUrl = viewModel::openUrl,
                        onDeleteItem = viewModel::deleteHistoryItem,
                        onClearAll = viewModel::clearAllHistory,
                        onDismiss = viewModel::closeHistory
                    )
                }

                // Bookmarks Sheet
                if (uiState.isBookmarksOpen) {
                    BookmarksSheet(
                        bookmarks = uiState.bookmarks,
                        currentTitle = activeTab?.title ?: "",
                        currentUrl = activeTab?.url ?: "",
                        onOpenUrl = viewModel::openUrl,
                        onAddBookmark = viewModel::addBookmark,
                        onDeleteBookmark = viewModel::deleteBookmark,
                        onTogglePinBookmark = viewModel::togglePinBookmark,
                        onDismiss = viewModel::closeBookmarks
                    )
                }

                // Password Manager Sheet
                if (uiState.isPasswordManagerOpen) {
                    val currentDomain = activeTab?.url ?: ""
                    PasswordManagerSheet(
                        passwords = uiState.passwords,
                        currentSiteDomain = currentDomain,
                        onSavePassword = viewModel::savePassword,
                        onDeletePassword = viewModel::deletePassword,
                        onDismiss = viewModel::closePasswordManager
                    )
                }

                // Downloads Sheet
                if (uiState.isDownloadsOpen) {
                    DownloadsSheet(
                        downloads = uiState.downloads,
                        onDeleteDownload = viewModel::deleteDownload,
                        onClearAll = viewModel::clearAllDownloads,
                        onDismiss = viewModel::closeDownloads
                    )
                }

                // Settings Sheet (including Themes & Privacy)
                if (uiState.isSettingsOpen) {
                    SettingsSheet(
                        currentTheme = uiState.currentTheme,
                        onSelectTheme = viewModel::setTheme,
                        searchEngine = uiState.searchEngine,
                        onSelectSearchEngine = viewModel::setSearchEngine,
                        isAdBlockEnabled = uiState.isAdBlockEnabled,
                        onToggleAdBlock = viewModel::toggleAdBlock,
                        onOpenClearData = viewModel::openClearDataDialog,
                        onOpenWelcome = viewModel::openWelcome,
                        onDismiss = viewModel::closeSettings
                    )
                }

                // Comprehensive Browser Menu Bottom Sheet
                if (uiState.isMenuOpen) {
                    LuminaMenuBottomSheet(
                        activeTab = activeTab,
                        isBookmarked = viewModel.isCurrentTabBookmarked(),
                        textZoom = uiState.textZoom,
                        isAdBlockEnabled = uiState.isAdBlockEnabled,
                        onGoBack = viewModel::triggerGoBack,
                        onGoForward = viewModel::triggerGoForward,
                        onReload = viewModel::triggerReload,
                        onToggleBookmark = viewModel::toggleBookmark,
                        onNewTab = { viewModel.createNewTab(isIncognito = false) },
                        onNewIncognitoTab = { viewModel.createNewTab(isIncognito = true) },
                        onToggleReaderMode = viewModel::toggleReaderMode,
                        onOpenQrCode = viewModel::openQrCode,
                        onOpenNotes = viewModel::openNotes,
                        onOpenFindInPage = viewModel::openFindInPage,
                        onOpenHistory = viewModel::openHistory,
                        onOpenBookmarks = viewModel::openBookmarks,
                        onOpenPasswordManager = viewModel::openPasswordManager,
                        onOpenDownloads = viewModel::openDownloads,
                        onCycleZoom = viewModel::cycleTextZoom,
                        onToggleAdBlock = viewModel::toggleAdBlock,
                        onOpenSettings = viewModel::openSettings,
                        onOpenWelcome = viewModel::openWelcome,
                        onOpenClearData = viewModel::openClearDataDialog,
                        onToggleDesktop = viewModel::toggleDesktopMode,
                        onShare = {
                            val url = activeTab?.url ?: ""
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${activeTab?.title}\n$url")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Поделиться ссылкой")
                            context.startActivity(shareIntent)
                        },
                        onOpenAiSheet = { viewModel.openAiChat() },
                        onDismiss = viewModel::closeMenu
                    )
                }

                // Clear Data Dialog
                if (uiState.isClearDataDialogOpen) {
                    ClearBrowsingDataDialog(
                        onConfirmClear = { hist, cache, pass, down ->
                            viewModel.clearBrowsingData(hist, cache, pass, down)
                        },
                        onDismiss = viewModel::closeClearDataDialog
                    )
                }
            }
        }
    }
}
