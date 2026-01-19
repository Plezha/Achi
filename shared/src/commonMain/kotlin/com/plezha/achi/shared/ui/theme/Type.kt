package com.plezha.achi.shared.ui.theme

import achi.shared.generated.resources.Montserrat_Bold
import achi.shared.generated.resources.Montserrat_Light
import achi.shared.generated.resources.Montserrat_Medium
import achi.shared.generated.resources.Montserrat_Regular
import achi.shared.generated.resources.Montserrat_SemiBold
import achi.shared.generated.resources.OpenSans_Bold
import achi.shared.generated.resources.OpenSans_Light
import achi.shared.generated.resources.OpenSans_Medium
import achi.shared.generated.resources.OpenSans_Regular
import achi.shared.generated.resources.OpenSans_SemiBold
import achi.shared.generated.resources.Res
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.plezha.achi.shared.ui.common.PreviewWrapper
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.ui.tooling.preview.Preview

val bodyFontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.OpenSans_Light, weight = FontWeight.Light),
        Font(Res.font.OpenSans_Regular, weight = FontWeight.Normal),
        Font(Res.font.OpenSans_Medium, weight = FontWeight.Medium),
        Font(Res.font.OpenSans_SemiBold, weight = FontWeight.SemiBold),
        Font(Res.font.OpenSans_Bold, weight = FontWeight.Bold),
    )

val displayFontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.Montserrat_Light, weight = FontWeight.Light),
        Font(Res.font.Montserrat_Regular, weight = FontWeight.Normal),
        Font(Res.font.Montserrat_Medium, weight = FontWeight.Medium),
        Font(Res.font.Montserrat_SemiBold, weight = FontWeight.SemiBold),
        Font(Res.font.Montserrat_Bold, weight = FontWeight.Bold),
    )

val baseline = Typography()

@Composable
fun achiTypography() = Typography(
    displayLarge = baseline.displayLarge.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold
    ),
    displayMedium = baseline.displayMedium.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold
    ),
    displaySmall = baseline.displaySmall.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold
    ),
    headlineLarge = baseline.headlineLarge.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.SemiBold
    ),
    headlineMedium = baseline.headlineMedium.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = baseline.headlineSmall.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = baseline.titleLarge.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Medium
    ),
    titleMedium = baseline.titleMedium.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = baseline.titleSmall.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = baseline.bodyLarge.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = baseline.bodyMedium.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = baseline.bodySmall.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = baseline.labelLarge.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal
    ),
    labelMedium = baseline.labelMedium.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal
    ),
    labelSmall = baseline.labelSmall.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal
    ),
)

@Preview
@Composable
private fun TypographyPreview() {
    PreviewWrapper {
        val achiTypography = achiTypography()
        LazyVerticalGrid(
            columns = GridCells.Fixed(1)
        ) {
            listOf(
                "displayLarge" to achiTypography.displayLarge,
                "displayMedium" to achiTypography.displayMedium,
                "displaySmall" to achiTypography.displaySmall,
                "headlineLarge" to achiTypography.headlineLarge,
                "headlineMedium" to achiTypography.headlineMedium,
                "headlineSmall" to achiTypography.headlineSmall,
                "titleLarge" to achiTypography.titleLarge,
                "titleMedium" to achiTypography.titleMedium,
                "titleSmall" to achiTypography.titleSmall,
                "bodyLarge" to achiTypography.bodyLarge,
                "bodyMedium" to achiTypography.bodyMedium,
                "bodySmall" to achiTypography.bodySmall,
                "labelLarge" to achiTypography.labelLarge,
                "labelMedium" to achiTypography.labelMedium,
                "labelSmall" to achiTypography.labelSmall,
            ).forEach { (text, style) ->
                item {
                    Text(text, style = style)
                }
            }
        }
    }
}
