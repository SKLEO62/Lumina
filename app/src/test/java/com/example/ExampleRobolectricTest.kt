package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AiActionType
import com.example.data.model.DownloadItem
import com.example.data.model.LuminaThemeMode
import com.example.data.model.PasswordEntry
import com.example.data.model.WebTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Lumina", appName)
    }

    @Test
    fun `web tab model creation and incognito`() {
        val regularTab = WebTab(title = "Lumina Hub", url = "lumina://newtab")
        assertEquals("Lumina Hub", regularTab.title)
        assertFalse(regularTab.isIncognito)

        val incognitoTab = WebTab(title = "Инкогнито", url = "lumina://newtab", isIncognito = true)
        assertTrue(incognitoTab.isIncognito)
        assertNotNull(incognitoTab.id)
    }

    @Test
    fun `ai actions definition`() {
        assertEquals(9, AiActionType.entries.size)
        assertEquals("Выжимка страницы", AiActionType.SUMMARIZE.title)
        assertEquals("Собрать задачи и чеклист", AiActionType.EXTRACT_TASKS.title)
    }

    @Test
    fun `tab spaces and web notes models`() {
        assertEquals(5, com.example.data.model.TabSpace.entries.size)
        assertEquals(4, com.example.data.model.SearchEngine.entries.size)
        val note = com.example.data.model.WebNote(
            url = "https://example.com",
            domain = "example.com",
            text = "Test note"
        )
        assertEquals("example.com", note.domain)
    }

    @Test
    fun `theme modes and password vault`() {
        assertEquals(5, LuminaThemeMode.entries.size)
        val pass = PasswordEntry(
            siteName = "GitHub",
            domain = "github.com",
            username = "dev",
            password = "secretPassword123"
        )
        assertEquals("github.com", pass.domain)

        val download = DownloadItem(fileName = "test.pdf", url = "https://example.com/test.pdf")
        assertTrue(download.isCompleted)
    }

    @Test
    fun `wallpapers and bookmark category and custom photo wallpaper`() {
        assertEquals(7, com.example.data.model.LuminaWallpaper.entries.size)
        val wallpaper = com.example.data.model.LuminaWallpaper.COSMIC_NEBULA
        assertEquals("Космос", wallpaper.title)

        val customWp = com.example.data.model.LuminaWallpaper.CUSTOM
        assertEquals("Свое фото", customWp.title)

        val vm = com.example.ui.viewmodel.BrowserViewModel(ApplicationProvider.getApplicationContext())
        vm.setCustomWallpaperPath("/data/user/0/custom.jpg")
        assertEquals(com.example.data.model.LuminaWallpaper.CUSTOM, vm.uiState.value.selectedWallpaper)
        assertEquals("/data/user/0/custom.jpg", vm.uiState.value.customWallpaperPath)

        vm.setWallpaperBlur(12f)
        assertEquals(12f, vm.uiState.value.wallpaperBlur)

        vm.removeCustomWallpaper()
        assertEquals(com.example.data.model.LuminaWallpaper.COSMIC_NEBULA, vm.uiState.value.selectedWallpaper)
        assertEquals(null, vm.uiState.value.customWallpaperPath)

        val bookmark = com.example.data.model.Bookmark(
            title = "GitHub",
            url = "https://github.com",
            category = "Работа",
            isPinned = true
        )
        assertEquals("Работа", bookmark.category)
        assertTrue(bookmark.isPinned)
    }

    @Test
    fun `tab duplication and recently closed restore`() {
        val vm = com.example.ui.viewmodel.BrowserViewModel(ApplicationProvider.getApplicationContext())
        val initialTabId = vm.uiState.value.activeTabId
        assertNotNull(initialTabId)

        // Duplicate the tab
        vm.duplicateTab(initialTabId)
        assertEquals(2, vm.uiState.value.tabs.size)
        val duplicatedId = vm.uiState.value.activeTabId
        org.junit.Assert.assertNotEquals(initialTabId, duplicatedId)

        // Close the duplicated tab
        vm.closeTab(duplicatedId)
        assertEquals(1, vm.uiState.value.tabs.size)
        assertEquals(initialTabId, vm.uiState.value.activeTabId)
    }

    @Test
    fun `search engine and theme selection in viewmodel`() {
        val vm = com.example.ui.viewmodel.BrowserViewModel(ApplicationProvider.getApplicationContext())
        assertEquals(com.example.data.model.SearchEngine.GOOGLE, vm.uiState.value.searchEngine)

        vm.setSearchEngine(com.example.data.model.SearchEngine.DUCKDUCKGO)
        assertEquals(com.example.data.model.SearchEngine.DUCKDUCKGO, vm.uiState.value.searchEngine)

        vm.setTheme(LuminaThemeMode.NEON)
        assertEquals(LuminaThemeMode.NEON, vm.uiState.value.currentTheme)
    }
}
