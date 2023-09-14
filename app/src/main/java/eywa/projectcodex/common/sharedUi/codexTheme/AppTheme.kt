package eywa.projectcodex.common.sharedUi.codexTheme

enum class AppTheme(
        val colors: CodexThemeColors,
        val dimens: CodexDimens = CodexDimens(),
) {
    LIGHT(
            colors = CodexThemeColors()
    ),
    DARK(
            // TODO Set up dark theme
            colors = CodexThemeColors(
                    appBackground = CodexColors.COLOR_PRIMARY_DARK
            )
    )
}
