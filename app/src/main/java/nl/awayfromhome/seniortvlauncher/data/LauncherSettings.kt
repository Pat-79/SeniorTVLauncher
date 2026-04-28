package nl.awayfromhome.seniortvlauncher.data

data class LauncherSettings(
    val rows: Int = 3,
    val columns: Int = 4,
    val buttonSizeDp: Int = 120,
    val showAppName: Boolean = true,
    val buttonShape: ButtonShape = ButtonShape.ROUNDED_SQUARE,
    val backgroundImageUri: String? = null,
    val backgroundBlurEnabled: Boolean = false,
    val backgroundBlurLevel: Int = 10,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val buttonAssignments: Map<Int, String> = emptyMap()
)

enum class ButtonShape { CIRCLE, ROUNDED_SQUARE, SQUARE }
