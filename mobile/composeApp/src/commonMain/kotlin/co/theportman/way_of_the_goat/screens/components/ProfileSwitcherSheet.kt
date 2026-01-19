package co.theportman.way_of_the_goat.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite
import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import co.theportman.way_of_the_goat.ui.theme.GoatColors

// Colors for the profile switcher
private val SheetBackground = Color(0xFF0f172b)      // slate-900
private val CardBackground = Color(0xFF1e293b)       // slate-800
private val TextPrimary = Color(0xFFf8fafc)          // slate-50
private val TextSecondary = Color(0xFF94a3b8)        // slate-400
private val SelectedGreen = Color(0xFF84cc16)        // lime-500
private val SelectedBorder = Color(0xFF84cc16)       // lime-500
private val UnselectedBorder = Color(0xFF334155)     // slate-700
private val WarningYellow = Color(0xFFfbbf24)        // amber-400
private val WarningBackground = Color(0xFF451a03)   // amber-950

/**
 * Bottom sheet modal for selecting a scoring profile.
 *
 * @param isOpen Whether the sheet is visible
 * @param onDismiss Called when the sheet should be dismissed
 * @param profiles List of available scoring profiles
 * @param currentProfileId The currently active profile ID
 * @param selectedProfileId The profile currently selected in the sheet (may differ from current)
 * @param onProfileSelected Called when a profile card is tapped
 * @param isToday Whether the current date is today (controls checkbox visibility)
 * @param useFutureChecked Whether "Continue using this profile in future" is checked
 * @param onUseFutureChanged Called when the checkbox is toggled
 * @param hasExistingData Whether the current day has existing servings data
 * @param onSwitchProfile Called when the "Switch profile" button is tapped
 * @param onCancel Called when "Cancel" is tapped
 * @param lastUsedSuiteId For empty past days, the last used profile (for hint display)
 * @param isEmptyPastDay True if the target day is in the past and has no profile selected yet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSwitcherSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    profiles: List<ScoringSuite>,
    currentProfileId: SuiteId,
    selectedProfileId: SuiteId,
    onProfileSelected: (SuiteId) -> Unit,
    isToday: Boolean,
    useFutureChecked: Boolean,
    onUseFutureChanged: (Boolean) -> Unit,
    hasExistingData: Boolean,
    onSwitchProfile: () -> Unit,
    onCancel: () -> Unit,
    lastUsedSuiteId: SuiteId? = null,
    isEmptyPastDay: Boolean = false,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isOpen) return

    // For empty past days, any selection is valid (there's no current profile)
    val isNewProfileSelected = if (isEmptyPastDay) true else selectedProfileId != currentProfileId
    val showWarning = hasExistingData && isNewProfileSelected

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            Text(
                text = "Select Profile",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose a scoring profile that matches your goals",
                color = TextSecondary,
                fontSize = 14.sp
            )

            // "Last used" hint for empty past days
            if (lastUsedSuiteId != null) {
                val lastUsedName = profiles.find { it.id == lastUsedSuiteId }?.name
                if (lastUsedName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Last used: $lastUsedName",
                        color = SelectedGreen,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile cards
            profiles.forEach { profile ->
                val isSelected = profile.id == selectedProfileId
                val isCurrent = profile.id == currentProfileId

                ProfileCard(
                    profile = profile,
                    isSelected = isSelected,
                    onClick = { onProfileSelected(profile.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Checkbox for future use (only shown for today)
            if (isToday) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUseFutureChanged(!useFutureChecked) }
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = useFutureChecked,
                        onCheckedChange = onUseFutureChanged,
                        colors = CheckboxDefaults.colors(
                            checkedColor = SelectedGreen,
                            uncheckedColor = TextSecondary,
                            checkmarkColor = SheetBackground
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue using this profile in future",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
            }

            // Warning banner (shown when switching with existing data)
            if (showWarning) {
                Spacer(modifier = Modifier.height(16.dp))
                WarningBanner(
                    profileName = profiles.find { it.id == selectedProfileId }?.name ?: "selected profile"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Button(
                onClick = onSwitchProfile,
                enabled = isNewProfileSelected,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SelectedGreen,
                    contentColor = SheetBackground,
                    disabledContainerColor = CardBackground,
                    disabledContentColor = TextSecondary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Switch profile",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cancel link
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable(
                            onClick = onCancel,
                            role = Role.Button
                        )
                        .semantics { contentDescription = "Cancel profile selection" }
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * A single profile selection card with radio button semantics for accessibility.
 */
@Composable
private fun ProfileCard(
    profile: ScoringSuite,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) SelectedBorder else UnselectedBorder
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val selectionState = if (isSelected) "Selected" else "Not selected"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .semantics {
                contentDescription = "${profile.name}. ${profile.description}. $selectionState"
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radio indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) SelectedGreen else Color.Transparent)
                .border(
                    width = if (isSelected) 0.dp else 2.dp,
                    color = if (isSelected) Color.Transparent else UnselectedBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null, // Handled by parent semantics
                    tint = SheetBackground,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Profile info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.name,
                color = if (isSelected) SelectedGreen else TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = profile.description,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Warning banner shown when switching profiles with existing data.
 */
@Composable
private fun WarningBanner(
    profileName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(WarningBackground)
            .semantics(mergeDescendants = true) {
                contentDescription = "Warning: Data will be lost. Switching to $profileName will reset this day's data."
            }
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null, // Handled by parent semantics
            tint = WarningYellow,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Data will be lost",
                color = WarningYellow,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Switching to $profileName will reset this day's data.",
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
