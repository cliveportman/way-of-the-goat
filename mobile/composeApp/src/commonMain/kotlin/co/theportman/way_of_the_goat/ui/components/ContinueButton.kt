package co.theportman.way_of_the_goat.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.theportman.way_of_the_goat.ui.theme.GoatColors

/**
 * Circular continue button used in splash and intro screens
 */
@Composable
fun ContinueButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .size(120.dp)
            .background(GoatColors.Navy900, CircleShape),
        shape = CircleShape,
        border = BorderStroke(1.dp, GoatColors.Navy800)
    ) {
        Text(
            text = "Continue",
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 14.sp
            ),
            color = GoatColors.Slate50
        )
    }
}
