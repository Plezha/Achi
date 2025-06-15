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
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font

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
        displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
        displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
        displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
        titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
        titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
        titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
        bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
        labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
        labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
        labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
    )

//@Preview()
//@Composable
//private fun TypographyPreview() {
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(1)
//    ) {
//        listOf(
//            "displayLarge" to achiTypography.displayLarge,
//            "displayMedium" to achiTypography.displayMedium,
//            "displaySmall" to achiTypography.displaySmall,
//            "headlineLarge" to achiTypography.headlineLarge,
//            "headlineMedium" to achiTypography.headlineMedium,
//            "headlineSmall" to achiTypography.headlineSmall,
//            "titleLarge" to achiTypography.titleLarge,
//            "titleMedium" to achiTypography.titleMedium,
//            "titleSmall" to achiTypography.titleSmall,
//            "bodyLarge" to achiTypography.bodyLarge,
//            "bodyMedium" to achiTypography.bodyMedium,
//            "bodySmall" to achiTypography.bodySmall,
//            "labelLarge" to achiTypography.labelLarge,
//            "labelMedium" to achiTypography.labelMedium,
//            "labelSmall" to achiTypography.labelSmall,
//        ).forEach { (text, style) ->
//            item {
//                Text(text, style = style)
//            }
//        }
//    }
//}
//
