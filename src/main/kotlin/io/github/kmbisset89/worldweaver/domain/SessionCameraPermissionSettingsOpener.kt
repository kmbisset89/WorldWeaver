package io.github.kmbisset89.worldweaver.domain

/**
 * Opens the operating system's camera privacy settings so the GM can grant access.
 *
 * macOS and Windows do not let an app re-show the first-use prompt after the user
 * denies it; those cases need System Settings / Windows Settings.
 */
internal class SessionCameraPermissionSettingsOpener(
    private val osName: () -> String = { System.getProperty("os.name").orEmpty() },
    private val startProcess: (List<String>) -> Boolean = Companion::launch,
) {
    fun open(): Boolean {
        val os = osName().lowercase()
        val command = when {
            os.contains("mac") -> listOf(
                "open",
                "x-apple.systempreferences:com.apple.preference.security?Privacy_Camera",
            )
            os.contains("windows") -> listOf("cmd", "/c", "start", "ms-settings:privacy-webcam")
            else -> return false
        }
        return startProcess(command)
    }

    private companion object {
        fun launch(command: List<String>): Boolean {
            return try {
                ProcessBuilder(command).start()
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}
