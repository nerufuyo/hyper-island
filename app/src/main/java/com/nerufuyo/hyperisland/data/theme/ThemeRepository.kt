package com.nerufuyo.hyperisland.data.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.nerufuyo.hyperisland.models.theme.ActionConfig
import com.nerufuyo.hyperisland.models.theme.AppThemeOverride
import com.nerufuyo.hyperisland.models.theme.ColorMode
import com.nerufuyo.hyperisland.models.theme.GlobalConfig
import com.nerufuyo.hyperisland.models.theme.HyperTheme
import com.nerufuyo.hyperisland.models.theme.ResourceType
import com.nerufuyo.hyperisland.models.theme.ThemeMetadata
import com.nerufuyo.hyperisland.models.theme.ThemeResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import java.util.zip.ZipInputStream

class ThemeRepository(private val context: Context) {

    companion object {
        // ponytail: Minimal/Monochrome batch only for now - add more BUILT_IN_PRESETS
        // entries (Vibrant, Pastel, ...) once there's a direction for them.
        internal val BUILT_IN_PRESETS = listOf(
            HyperTheme(
                id = "builtin_minimal_onyx",
                meta = ThemeMetadata(
                    name = "Onyx",
                    author = "Hyper Island",
                    description = "Pure black, minimal contrast."
                ),
                global = GlobalConfig(
                    highlightColor = "#000000",
                    backgroundColor = "#000000",
                    textColor = "#FFFFFF",
                    colorMode = ColorMode.CUSTOM,
                    iconShapeId = "circle"
                )
            ),
            HyperTheme(
                id = "builtin_minimal_slate",
                meta = ThemeMetadata(
                    name = "Slate",
                    author = "Hyper Island",
                    description = "Neutral dark gray, easy on the eyes."
                ),
                global = GlobalConfig(
                    highlightColor = "#3A3A3C",
                    backgroundColor = "#1C1C1E",
                    textColor = "#FFFFFF",
                    colorMode = ColorMode.CUSTOM,
                    iconShapeId = "squircle"
                )
            ),
            HyperTheme(
                id = "builtin_minimal_paper",
                meta = ThemeMetadata(
                    name = "Paper",
                    author = "Hyper Island",
                    description = "Clean off-white, built for light mode."
                ),
                global = GlobalConfig(
                    highlightColor = "#F2F2F7",
                    backgroundColor = "#FFFFFF",
                    textColor = "#1C1C1E",
                    colorMode = ColorMode.CUSTOM,
                    iconShapeId = "circle"
                )
            )
        )
    }

    private val tag = "HyperIslandTheme"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val _activeTheme = MutableStateFlow<HyperTheme?>(null)
    val activeTheme: StateFlow<HyperTheme?> = _activeTheme.asStateFlow()

    private val themesDir = File(context.filesDir, "themes")

    init {
        if (!themesDir.exists()) themesDir.mkdirs()
        seedBuiltInPresets()
    }

    /**
     * Installs the bundled Minimal/Monochrome presets on first run, using the same
     * on-disk format as an imported .hbr theme so they show up in getAvailableThemes()
     * with no other changes needed. Guarded by a marker file so a user who deletes a
     * preset doesn't get it silently reinstalled.
     *
     * ponytail: more preset styles (Vibrant, Pastel, ...) can be added the same way -
     * append to BUILT_IN_PRESETS - once there's a direction for them.
     */
    private fun seedBuiltInPresets() {
        val marker = File(themesDir, ".presets_seeded")
        if (marker.exists()) return
        BUILT_IN_PRESETS.forEach { theme ->
            val folder = File(themesDir, theme.id)
            if (!folder.exists()) {
                folder.mkdirs()
                runCatching {
                    File(folder, "theme_config.json").writeText(json.encodeToString(theme))
                }.onFailure { Log.e(tag, "Failed to seed preset ${theme.id}", it) }
            }
        }
        runCatching { marker.createNewFile() }
    }

    /**
     * Loads a theme from disk into memory by ID.
     */
    suspend fun activateTheme(themeId: String) {
        withContext(Dispatchers.IO) {
            try {
                val themeFile = File(themesDir, "$themeId/theme_config.json") // Standard name
                if (themeFile.exists()) {
                    val content = themeFile.readText()
                    val theme = json.decodeFromString<HyperTheme>(content)
                    _activeTheme.value = theme
                    Log.i(tag, "Theme Activated: ${theme.meta.name}")
                } else {
                    Log.w(tag, "Theme file not found: $themeId")
                    _activeTheme.value = null
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to load theme", e)
                _activeTheme.value = null
            }
        }
    }

    // ==========================================
    //           INSTALLATION LOGIC
    // ==========================================

    /**
     * Unzips a .hbr file, validates it, and installs it to internal storage.
     * Returns the ID of the installed theme, or throws Exception.
     */
    suspend fun installThemeFromUri(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val tempId = UUID.randomUUID().toString()
            val tempDir = File(context.cacheDir, "theme_import_$tempId")
            if (!tempDir.exists()) tempDir.mkdirs()

            try {
                // 1. UNZIP
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zip ->
                        var entry = zip.nextEntry
                        while (entry != null) {
                            val file = File(tempDir, entry.name)

                            // Security: Zip Slip Check
                            if (!file.canonicalPath.startsWith(tempDir.canonicalPath)) {
                                throw SecurityException("Invalid Zip Path: ${entry.name}")
                            }

                            if (entry.isDirectory) {
                                file.mkdirs()
                            } else {
                                file.parentFile?.mkdirs()
                                file.outputStream().use { output ->
                                    zip.copyTo(output)
                                }
                            }
                            entry = zip.nextEntry
                        }
                    }
                }

                // 2. VALIDATION
                val configFile = File(tempDir, "theme_config.json")
                if (!configFile.exists()) {
                    throw IllegalArgumentException("Invalid Theme: Missing theme_config.json")
                }

                // Parse to check validity and get real ID if present
                val content = configFile.readText()
                val theme = try {
                    json.decodeFromString<HyperTheme>(content)
                } catch (_: Exception) {
                    throw IllegalArgumentException("Invalid JSON structure")
                }

                // 3. INSTALLATION
                // We use the ID defined in JSON, or generate one if missing (shouldn't happen with strict spec)
                val finalId = theme.id.ifEmpty { tempId }
                val targetDir = File(themesDir, finalId)

                // If updating, clear old version
                if (targetDir.exists()) {
                    targetDir.deleteRecursively()
                }

                // Move temp to final
                if (!tempDir.renameTo(targetDir)) {
                    // Fallback if rename fails (filesystems differ)
                    tempDir.copyRecursively(targetDir, overwrite = true)
                    tempDir.deleteRecursively()
                }

                Log.i(tag, "Theme Installed Successfully: $finalId")
                return@withContext finalId

            } catch (e: Exception) {
                Log.e(tag, "Installation failed", e)
                tempDir.deleteRecursively() // Cleanup
                throw e
            }
        }
    }

    // ==========================================
    //           HELPER LOGIC
    // ==========================================

    fun getHighlightColor(packageName: String, default: String = "#000000"): String {
        val theme = _activeTheme.value ?: return default
        theme.apps[packageName]?.highlightColor?.let { return it }
        return theme.global.highlightColor ?: default
    }

    fun getActionConfig(packageName: String, actionKey: String): ActionConfig? {
        val theme = _activeTheme.value ?: return null

        // 1. Check App Overrides
        val appActions = theme.apps[packageName]?.actions
        if (appActions != null && appActions.containsKey(actionKey)) {
            return appActions[actionKey]
        }

        // 2. Check Global Defaults
        if (theme.defaultActions.containsKey(actionKey)) {
            return theme.defaultActions[actionKey]
        }

        return null
    }

    fun getAppOverride(packageName: String): AppThemeOverride? {
        return _activeTheme.value?.apps?.get(packageName)
    }

    // ==========================================
    //           ASSET LOADING
    // ==========================================

    fun getResourceBitmap(resource: ThemeResource?): Bitmap? {
        if (resource == null) return null
        val themeId = _activeTheme.value?.id ?: return null

        return try {
            when (resource.type) {
                ResourceType.LOCAL_FILE -> {
                    val themeFolder = File(themesDir, themeId)
                    val file = File(themeFolder, resource.value)
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                }
                ResourceType.URI_CONTENT -> {
                    val uri = resource.value.toUri()
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                }
                ResourceType.PRESET_DRAWABLE -> null
            }
        } catch (e: Exception) {
            Log.w(tag, "Error loading bitmap resource: ${resource.value}", e)
            null
        }
    }

    fun getThemesDir(): File = themesDir


    /**
     * Scans the internal themes folder and returns metadata for all installed themes.
     */
    suspend fun getAvailableThemes(): List<HyperTheme> {
        return withContext(Dispatchers.IO) {
            val list = mutableListOf<HyperTheme>()

            if (themesDir.exists()) {
                themesDir.listFiles()?.forEach { themeFolder ->
                    try {
                        val configFile = File(themeFolder, "theme_config.json")
                        if (configFile.exists()) {
                            val content = configFile.readText()
                            val theme = json.decodeFromString<HyperTheme>(content)
                            list.add(theme)
                        }
                    } catch (_: Exception) {
                        Log.e(tag, "Corrupt theme found in: ${themeFolder.name}")
                    }
                }
            }
            return@withContext list
        }
    }

    /**
     * Deletes a theme from storage.
     */
    suspend fun deleteTheme(themeId: String) {
        withContext(Dispatchers.IO) {
            val themeFolder = File(themesDir, themeId)
            if (themeFolder.exists()) {
                themeFolder.deleteRecursively()
            }
            // If the deleted theme was active, reset to default
            if (_activeTheme.value?.id == themeId) {
                _activeTheme.value = null
            }
        }
    }

    /**
     * Saves a new or updated theme object to disk.
     */
    suspend fun saveTheme(theme: HyperTheme) {
        withContext(Dispatchers.IO) {
            // Ensure folder exists
            val themeFolder = File(themesDir, theme.id)
            if (!themeFolder.exists()) themeFolder.mkdirs()

            // Write JSON
            val configFile = File(themeFolder, "theme_config.json")
            val content = json.encodeToString(theme)
            configFile.writeText(content)
        }
    }

    /**
     * Zips the theme folder into a .hbr file for sharing.
     */
    suspend fun exportTheme(themeId: String): File? {
        return withContext(Dispatchers.IO) {
            val sourceFolder = File(themesDir, themeId)
            if (!sourceFolder.exists()) return@withContext null

            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val zipFile = File(exportDir, "${themeId}_v1.hbr")
            try {
                java.util.zip.ZipOutputStream(java.io.FileOutputStream(zipFile)).use { zos ->
                    sourceFolder.walkTopDown().forEach { file ->
                        if (file.isFile) {
                            val entryName = file.relativeTo(sourceFolder).path
                            zos.putNextEntry(java.util.zip.ZipEntry(entryName))
                            file.inputStream().use { it.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                }
                return@withContext zipFile
            } catch (e: Exception) {
                Log.e(tag, "Export failed", e)
                return@withContext null
            }
        }
    }
}