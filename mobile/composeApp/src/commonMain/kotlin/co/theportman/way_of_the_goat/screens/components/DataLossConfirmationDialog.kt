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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Colors for the confirmation dialog
private val DialogBackground = Color(0xFF0f172b)     // slate-900
private val TextPrimary = Color(0xFFf8fafc)          // slate-50
private val TextSecondary = Color(0xFF94a3b8)        // slate-400
private val WarningYellow = Color(0xFFfbbf24)        // amber-400
private val DestructiveRed = Color(0xFFef4444)       // red-500
private val DestructiveRedDark = Color(0xFF7f1d1d)  // red-900
private val DestructiveRedBorder = Color(0xFFdc2626) // red-600
private val SecondaryButton = Color(0xFF334155)      // slate-700

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
                    spotColor = DestructiveRed.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = DestructiveRedBorder.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DialogBackground)
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
                    tint = WarningYellow,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Data will be lost",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dynamic text
                Text(
                    text = "Switching to $profileName will reset today's data.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Warning box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DestructiveRedDark.copy(alpha = 0.4f))
                        .border(
                            width = 1.dp,
                            color = DestructiveRedBorder.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .semantics {
                            contentDescription = "Important: This action cannot be undone. Consider completing today's profile before switching tomorrow."
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "This action cannot be undone. Consider completing today's profile before switching tomorrow.",
                        color = TextPrimary,
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
                            containerColor = DestructiveRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
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
                            contentColor = TextSecondary
                        ),
                        border = null,
                        shape = RoundedCornerShape(8.dp)
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
