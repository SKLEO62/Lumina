# Lumina — Product Roadmap & Implementation Notes

This document records a proposed implementation roadmap based on a review of the current repository. It is a planning document, not a claim that every feature has been built, tested, or verified on a device.

## Current project structure reviewed

- `app/src/main/java/com/example/ui/viewmodel/BrowserViewModel.kt`
  - Central `BrowserUiState` includes tabs, bookmarks, history, passwords, downloads, themes, wallpaper settings, reader mode, find-in-page, ad-block toggle, QR code, notes, tab spaces, search engine, and recently closed tabs.
  - Uses `SharedPreferences` for some preferences such as onboarding dismissal, theme, search engine, and ad-block toggle.
- `app/src/main/java/com/example/ui/components/NewTabScreen.kt`
  - Existing start/new-tab UI, search/bookmark-related controls, wallpaper selection and customization.
- `app/src/main/java/com/example/ui/components/Omnibar.kt`
  - Existing browser address/navigation UI and access to many browser actions.
- `app/src/main/java/com/example/ui/components/LuminaBottomBar.kt`
  - A five-item Neon Glass bottom bar already exists: Back, Forward, Home, Tabs (with count badge), and Menu.
- `app/src/main/java/com/example/ui/screens/LuminaBrowserScreen.kt`
  - Wires the browser screen to ViewModel actions and opens sheets for history, bookmarks, password manager, downloads, settings, and the menu.
- `app/src/main/java/com/example/ui/components/SettingsSheet.kt`
  - Existing bottom-sheet settings for themes, search engine, ad-block toggle, and clear-data/onboarding entry points.
- `app/src/main/java/com/example/ui/components/LuminaWebView.kt`
  - Configures Android WebView, downloads, page text extraction, find-in-page, navigation actions, text zoom, desktop mode, and incognito cleanup.
- `AndroidManifest.xml`
  - Must be reviewed as part of the security pass; the earlier source review found cleartext traffic enabled and WebView mixed-content compatibility mode.

## Important implementation distinction

UI state or a menu item does not by itself prove a feature is fully implemented. The ad-block toggle and persistence of browser data should be traced end-to-end before being described as production-ready. The existing `BrowserViewModel` initializes tabs, bookmarks, history, password entries, downloads, and notes as in-memory lists in its UI state; verify where and how each collection is persisted before relying on data surviving process death.

## Recommended priorities

### P0 — Reliability and security
1. Audit persistence for bookmarks, history, notes, downloads, saved passwords, tab list, and active tab. Use appropriate storage for each data type and verify restoration after process death.
2. Review WebView security: HTTPS defaults, mixed-content policy, SSL error handling, file chooser and permission requests, external intents, and URL handling. Never bypass certificate errors.
3. Verify whether ad/tracker blocking actually intercepts and blocks requests; if not, implement a tested rule-list/request-interception strategy or label the control honestly as unavailable.
4. Verify incognito behavior: do not persist incognito history, tabs, cookies, or page text; clear appropriate WebView data when sessions end.
5. Add build and regression checks for navigation, tab switching, downloads, bookmarks, and clear-data behavior.

### P1 — Browser workflow
1. Persist and restore the last session with a clear user-facing setting.
2. Improve tab switcher with search, pinning, groups/spaces, and recently closed tab restore.
3. Improve downloads with status/progress, failure state, retry where supported, open/share, and safe deletion.
4. Make settings a clear full-screen or well-organized sectioned screen if the existing bottom sheet becomes crowded.
5. Ensure search, bookmarks, and history use real saved data rather than placeholder/demo entries.

### P2 — AI and reading tools
1. Keep the existing AI chat and page-text extraction flow, but add explicit consent before sending page contents to an external AI provider.
2. Add clear provider/model configuration, loading/error/cancel states, and user-visible disclosure about API usage and potential costs.
3. Improve reader mode and text-to-speech; provide graceful handling for pages where text extraction is incomplete.
4. Add translation and structured summaries only after the current AI pipeline is verified.

### P3 — Visual polish
1. Refine the existing Neon Glass start page and bottom bar without replacing the Kotlin/Compose architecture.
2. Keep the bottom bar focused on Back, Forward, Home, Tabs, and Menu; put less-used tools in the menu.
3. Ensure contrast, touch target sizes, system-bar insets, and responsive layout on smaller screens.
4. Keep wallpaper blur/dimming and themes optional, and respect reduced-motion/low-performance considerations where feasible.

## Suggested acceptance checklist

- [ ] Gradle debug build succeeds.
- [ ] Main page, URL entry, navigation back/forward, and reload work.
- [ ] Tab count matches open tabs; switching tabs restores the correct page.
- [ ] Bookmarks/history/settings survive app restart where expected.
- [ ] Incognito data is not retained after the private session ends.
- [ ] Downloads expose accurate status and recover from errors.
- [ ] Ad-block UI accurately reflects real blocking behavior.
- [ ] SSL certificate errors are never silently ignored.
- [ ] AI page analysis requires consent before external transmission.
- [ ] UI is checked on a real Android device or emulator before claiming device verification.

## Scope of this update

This commit adds planning documentation only. It does not change application source code, build configuration, or produce an APK. No build or device test was run as part of this documentation update.
