package co.theportman.way_of_the_goat.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import way_of_the_goat.composeapp.generated.resources.*

@Composable
fun InterFontFamily() = FontFamily(
    Font(Res.font.Inter_Light, FontWeight.Light),
    Font(Res.font.Inter_Regular, FontWeight.Normal),
    Font(Res.font.Inter_Medium, FontWeight.Medium),
    Font(Res.font.Inter_SemiBold, FontWeight.SemiBold),
    Font(Res.font.Inter_Bold, FontWeight.Bold)
)

val GoatTypography: Typography
    @Composable
    get() {
        val interFamily = InterFontFamily()

        return Typography(
            displayLarge = TextStyle(
                fontFamily = interFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 40.sp
            ),
            headlineLarge = TextStyle(
                fontFamily = interFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 40.sp
            ),
            titleLarge = TextStyle(
                fontFamily = interFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 28.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = interFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = interFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            labelLarge = TextStyle(
                fontFamily = interFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )
        )
    }
