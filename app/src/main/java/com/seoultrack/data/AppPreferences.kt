package com.seoultrack.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Simple SharedPreferences wrapper for app configuration.
 * Stores the TMDB API key and other user preferences.
 */
object AppPreferences {

    private const val PREFS_NAME = "seoultrack_prefs"
    private const val KEY_TMDB_API_KEY = "tmdb_api_key"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_OLED_MODE = "oled_mode"
    private const val KEY_AUTO_PLAY = "auto_play"
    private const val KEY_SUBTITLES = "subtitles"
    private const val KEY_QUALITY = "streaming_quality"
    private const val KEY_SUBTITLE_LANG = "subtitle_language"
    private const val KEY_NOTIFICATIONS = "notifications"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ── TMDB API Key ───────────────────────────────────────────────────────

    fun getTmdbApiKey(context: Context): String {
        return getPrefs(context).getString(KEY_TMDB_API_KEY, "") ?: ""
    }

    fun setTmdbApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_TMDB_API_KEY, key.trim()).apply()
    }

    fun hasTmdbApiKey(context: Context): Boolean {
        return getTmdbApiKey(context).isNotBlank()
    }

    // ── Theme Preferences ──────────────────────────────────────────────────

    fun isDarkMode(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DARK_MODE, true)
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun isOledMode(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_OLED_MODE, false)
    }

    fun setOledMode(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_OLED_MODE, enabled).apply()
    }

    // ── Playback Preferences ───────────────────────────────────────────────

    fun isAutoPlay(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_PLAY, true)
    }

    fun setAutoPlay(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_PLAY, enabled).apply()
    }

    fun isSubtitlesEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SUBTITLES, true)
    }

    fun setSubtitlesEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SUBTITLES, enabled).apply()
    }

    fun getStreamingQuality(context: Context): String {
        return getPrefs(context).getString(KEY_QUALITY, "Auto") ?: "Auto"
    }

    fun setStreamingQuality(context: Context, quality: String) {
        getPrefs(context).edit().putString(KEY_QUALITY, quality).apply()
    }

    fun getSubtitleLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_SUBTITLE_LANG, "Korean") ?: "Korean"
    }

    fun setSubtitleLanguage(context: Context, lang: String) {
        getPrefs(context).edit().putString(KEY_SUBTITLE_LANG, lang).apply()
    }

    // ── Notification Preferences ───────────────────────────────────────────

    fun isNotificationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_NOTIFICATIONS, true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }
}
