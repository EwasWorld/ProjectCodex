package eywa.projectcodex.common.sharedUi

import androidx.annotation.DrawableRes
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource

sealed class CodexIconInfo {
    abstract val contentDescription: String?
    abstract val tint: Color?
    abstract val modifier: Modifier

    data class VectorIcon(
            val imageVector: ImageVector,
            override val contentDescription: String? = null,
            override val tint: Color? = null,
            override val modifier: Modifier = Modifier,
    ) : CodexIconInfo() {
        @Composable
        override fun asPainter() = rememberVectorPainter(imageVector)
    }

    data class PainterIcon(
            @DrawableRes val drawable: Int,
            override val contentDescription: String? = null,
            override val tint: Color? = null,
            override val modifier: Modifier = Modifier,
    ) : CodexIconInfo() {
        @Composable
        override fun asPainter() = painterResource(drawable)
    }

    @Composable
    abstract fun asPainter(): Painter

    @Composable
    fun CodexIcon(
            modifier: Modifier = Modifier
    ) {
        Icon(
                painter = asPainter(),
                contentDescription = contentDescription,
                tint = tint ?: LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
                modifier = modifier.then(this.modifier)
        )
    }

    fun copyIcon(
            contentDescription: String? = this.contentDescription,
            tint: Color? = this.tint,
            modifier: Modifier = this.modifier,
    ) = when (this) {
        is PainterIcon -> copy(contentDescription = contentDescription, tint = tint, modifier = modifier)
        is VectorIcon -> copy(contentDescription = contentDescription, tint = tint, modifier = modifier)
    }
}
