package co.theportman.way_of_the_goat.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.theportman.way_of_the_goat.ui.theme.GoatPalette
import co.theportman.way_of_the_goat.ui.theme.GoatRadius
import co.theportman.way_of_the_goat.ui.theme.goatColors

/**
 * Confirmation dialog shown when switching profiles would delete existing data.
 *
 * This dialog appears on top of both the Scores screen AND the profile switcher
 * bottom sheet (both should be dimmed/blurred behind this dialog).
 *
 * @param isOpen Whether the dialog is visible
 * @param onDismiss Called when the dialog should be dismissed (tapping outside or system back)
 * @param profileName The name of the profile being switched to
 * @param onSwitchAnyway Called when user confirms they want to switch despite data loss
 * @param onKeepCurrent Called when user decides to keep current profile
 */
@Composable
fun DataLossConfirmationDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    profileName: String,
    onSwitchAnyway: () -> Unit,
    onKeepCurrent: () -> Unit
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(
                    elevation = 24.dp,
                    spotColor = MaterialTheme.goatColors.error.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(GoatRadius.lg)
                )
                .border(
                    width = 1.dp,
                    color = GoatPalette.Red600.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(GoatRadius.lg)
                ),
            shape = RoundedCornerShape(GoatRadius.lg),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.goatColors.surfaceContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning icon
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null, // Dialog title provides context
                    tint = GoatPalette.Amber400,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Data will be lost",
                    color = MaterialTheme.goatColors.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dynamic text
                Text(
                    text = "Switching to $profileName will reset today's data.",
                    color = MaterialTheme.goatColors.onSurfaceVariant,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Warning box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(GoatRadius.sm))
                        .background(GoatPalette.Red900.copy(alpha = 0.4f))
                        .border(
                            width = 1.dp,
                            color = GoatPalette.Red600.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(GoatRadius.sm)
                        )
                        .semantics {
                            contentDescription = "Important: This action cannot be undone. Consider completing today's profile before switching tomorrow."
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "This action cannot be undone. Consider completing today's profile before switching tomorrow.",
                        color = MaterialTheme.goatColors.onSurface,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Destructive action - Switch anyway
                    Button(
                        onClick = onSwitchAnyway,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.goatColors.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(GoatRadius.sm)
                    ) {
                        Text(
                            text = "Switch anyway",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Safe action - Keep current profile
                    OutlinedButton(
                        onClick = onKeepCurrent,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.goatColors.onSurfaceVariant
                        ),
                        border = null,
                        shape = RoundedCornerShape(GoatRadius.sm)
                    ) {
                        Text(
                            text = "Keep current profile",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
