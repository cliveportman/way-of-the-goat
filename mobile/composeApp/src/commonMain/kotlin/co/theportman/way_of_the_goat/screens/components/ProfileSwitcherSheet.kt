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
import androidx.compose.material3.MaterialTheme
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
import co.theportman.way_of_the_goat.ui.theme.GoatPalette
import co.theportman.way_of_the_goat.ui.theme.goatColors

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
        containerColor = MaterialTheme.goatColors.surfaceContainer,
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
                color = MaterialTheme.goatColors.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose a scoring profile that matches your goals",
                color = MaterialTheme.goatColors.onSurfaceVariant,
                fontSize = 14.sp
            )

            // "Last used" hint for empty past days
            if (lastUsedSuiteId != null) {
                val lastUsedName = profiles.find { it.id == lastUsedSuiteId }?.name
                if (lastUsedName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Last used: $lastUsedName",
                        color = MaterialTheme.goatColors.primary,
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
                            checkedColor = MaterialTheme.goatColors.primary,
                            uncheckedColor = MaterialTheme.goatColors.onSurfaceVariant,
                            checkmarkColor = MaterialTheme.goatColors.surfaceContainer
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue using this profile in future",
                        color = MaterialTheme.goatColors.onSurface,
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
                    containerColor = MaterialTheme.goatColors.primary,
                    contentColor = MaterialTheme.goatColors.surfaceContainer,
                    disabledContainerColor = MaterialTheme.goatColors.surfaceContainerHigh,
                    disabledContentColor = MaterialTheme.goatColors.onSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isEmptyPastDay) "Select profile" else "Switch profile",
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
                    color = MaterialTheme.goatColors.onSurfaceVariant,
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
    val borderColor = if (isSelected) MaterialTheme.goatColors.primary else MaterialTheme.goatColors.outline
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val selectionState = if (isSelected) "Selected" else "Not selected"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.goatColors.surfaceContainerHigh)
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
                .background(if (isSelected) MaterialTheme.goatColors.primary else Color.Transparent)
                .border(
                    width = if (isSelected) 0.dp else 2.dp,
                    color = if (isSelected) Color.Transparent else MaterialTheme.goatColors.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null, // Handled by parent semantics
                    tint = MaterialTheme.goatColors.surfaceContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Profile info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.name,
                color = if (isSelected) MaterialTheme.goatColors.primary else MaterialTheme.goatColors.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = profile.description,
                color = MaterialTheme.goatColors.onSurfaceVariant,
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
            .background(GoatPalette.Amber950)
            .semantics(mergeDescendants = true) {
                contentDescription = "Warning: Data will be lost. Switching to $profileName will reset this day's data."
            }
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null, // Handled by parent semantics
            tint = GoatPalette.Amber400,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Data will be lost",
                color = GoatPalette.Amber400,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Switching to $profileName will reset this day's data.",
                color = MaterialTheme.goatColors.onSurface,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
