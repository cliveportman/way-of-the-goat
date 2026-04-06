package co.theportman.way_of_the_goat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import co.theportman.way_of_the_goat.data.scoring.model.CategoryId
import co.theportman.way_of_the_goat.data.scoring.model.FoodCategory
import co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite
import co.theportman.way_of_the_goat.isDebugBuild
import co.theportman.way_of_the_goat.screens.components.FoodCategoryRow
import co.theportman.way_of_the_goat.ui.theme.GoatSpacing
import co.theportman.way_of_the_goat.ui.theme.WayOfTheGoatTheme
import co.theportman.way_of_the_goat.ui.theme.goatColors

@Composable
fun HelpScreen(
    onNavigateToDesignTokens: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = GoatSpacing.s16),
        verticalArrangement = Arrangement.spacedBy(GoatSpacing.s8)
    ) {
        // ── Title ──────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(GoatSpacing.s16))
            Text(
                text = "User guide",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.goatColors.onSurface,
            )
            Text(
                text = "How to get the most out of this app",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.goatColors.onSurfaceVariant,
            )
        }

        // ── Intro ──────────────────────────────────────────────
        item {
            Text(
                text = buildAnnotatedString {
                    append("This user guide is not intended to replace the inspiration behind it: a book called ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("Racing Weight")
                    }
                    append(" by Matt Fitzgerald. If you haven't already, you really should buy the book.")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = "That said, you're likely to have some questions, so let's try and preempt them here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        // ── How to use ────────────────────────────────────────
        item { SectionDivider() }
        item {
            Text(
                text = "How to use",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            val balanced = SuiteDefinitions.BALANCED
            Column(verticalArrangement = Arrangement.spacedBy(GoatSpacing.s4)) {
                val fruitCategory = balanced.getCategoryById(CategoryId("fruit"))
                val refinedGrainsCategory = balanced.getCategoryById(CategoryId("refinedgrains"))
                if (fruitCategory != null && refinedGrainsCategory != null) {
                    FoodCategoryRow(
                        category = fruitCategory,
                        servingCount = 2,
                        onIncrement = {},
                        onDecrement = {},
                    )
                    FoodCategoryRow(
                        category = refinedGrainsCategory,
                        servingCount = 3,
                        onIncrement = {},
                        onDecrement = {},
                    )
                }
            }
        }
        item {
            Text(
                text = buildAnnotatedString {
                    append("To add a serving ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("tap the food category.")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = buildAnnotatedString {
                    append("To remove a serving ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("press and hold the food category.")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = buildAnnotatedString {
                    append("To view a different day ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("swipe.")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        // ── Food categories ───────────────────────────────────
        item { SectionDivider() }
        item {
            Text(
                text = "Food categories",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = "The categories you see depend on your active scoring profile. Here's a guide to all of them.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        // Food category entries
        items(GUIDE_ENTRIES.size, key = { GUIDE_ENTRIES[it].categoryId.value }) { index ->
            val entry = GUIDE_ENTRIES[index]
            val category = entry.suite.getCategoryById(entry.categoryId)
            if (category != null) {
                FoodCategoryGuideEntry(
                    category = category,
                    heading = entry.heading,
                    description = entry.description,
                )
            }
        }

        // ── Breaking the rules ────────────────────────────────
        item { SectionDivider() }
        item { SectionHeading("Breaking the rules") }
        item { BodyText(BREAKING_THE_RULES_INTRO) }
        item { BodyText(BREAKING_THE_RULES_EXAMPLE) }

        // ── Other stuff ───────────────────────────────────────
        item { SectionDivider() }
        item { SectionHeading("Other stuff") }
        item { SectionSubheading(OTHER_STUFF_VEGETARIAN_TITLE) }
        item { BodyText(OTHER_STUFF_VEGETARIAN) }
        item { SectionSubheading(OTHER_STUFF_EATING_ON_THE_RUN_TITLE) }
        item { BodyText(OTHER_STUFF_EATING_ON_THE_RUN) }
        item { SectionSubheading(OTHER_STUFF_PROCESSED_TITLE) }
        item { BodyText(buildAnnotatedString(OTHER_STUFF_PROCESSED_CONTENT)) }
        item { SectionSubheading(OTHER_STUFF_PROTEIN_SHAKES_TITLE) }
        item { BodyText(OTHER_STUFF_PROTEIN_SHAKES) }
        item { SectionSubheading(OTHER_STUFF_OFFICIAL_APP_TITLE) }
        item { BodyText(buildAnnotatedString(OTHER_STUFF_OFFICIAL_APP_CONTENT)) }

        // ── Technical questions ───────────────────────────────
        item { SectionDivider() }
        item { SectionHeading("Technical questions") }
        item { SectionSubheading(TECHNICAL_WHAT_HAPPENS_TO_DATA_TITLE) }
        item { BodyText(TECHNICAL_WHAT_HAPPENS_TO_DATA) }
        item { SectionSubheading(TECHNICAL_UNINSTALL_TITLE) }
        item { BodyText(TECHNICAL_UNINSTALL) }
        item { SectionSubheading(TECHNICAL_ACCOUNT_TITLE) }
        item { BodyText(TECHNICAL_ACCOUNT) }
        item { SectionSubheading(TECHNICAL_SUGGESTIONS_TITLE) }
        item { BodyText(buildAnnotatedString(TECHNICAL_SUGGESTIONS_CONTENT)) }

        // ── Developer section (debug only) ────────────────────
        if (isDebugBuild) {
            item { SectionDivider() }
            item {
                Text(
                    text = "Developer",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.goatColors.onSurface,
                    modifier = Modifier.padding(bottom = GoatSpacing.s8)
                )
            }
            item {
                Surface(
                    onClick = onNavigateToDesignTokens,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = GoatSpacing.s16)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = GoatSpacing.s12),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Design tokens",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.goatColors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "\u2192",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.goatColors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Bottom padding for nav bar clearance
        item { Spacer(modifier = Modifier.height(GoatSpacing.s48)) }
    }
}

@Preview(name = "Default")
@Composable
private fun HelpScreenPreview() {
    WayOfTheGoatTheme(darkTheme = true) {
        HelpScreen()
    }
}

@Preview(name = "Light")
@Composable
private fun HelpScreenPreviewLight() {
    WayOfTheGoatTheme(darkTheme = false) {
        HelpScreen()
    }
}

@Preview(name = "FoodCategoryGuideEntry")
@Composable
private fun FoodCategoryGuideEntryPreview() {
    // safe: BALANCED always contains the "fruit" category
    val fruitCategory = SuiteDefinitions.BALANCED.getCategoryById(CategoryId("fruit"))!!
    WayOfTheGoatTheme(darkTheme = true) {
        FoodCategoryGuideEntry(
            category = fruitCategory,
            heading = "Fruit",
            description = "Whole fruit, tinned fruit, canned fruit, smoothies and juices made with 100% fruit.",
        )
    }
}

/**
 * A divider used between major guide sections.
 */
@Composable
private fun SectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        color = MaterialTheme.goatColors.outline,
        modifier = modifier.padding(vertical = GoatSpacing.s8)
    )
}

/**
 * A section heading.
 */
@Composable
private fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.goatColors.onSurface,
        modifier = modifier
    )
}

/**
 * A section subheading.
 */
@Composable
private fun SectionSubheading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.goatColors.onSurface,
        modifier = modifier
    )
}

/**
 * Body text content.
 */
@Composable
private fun BodyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.goatColors.onSurface,
        modifier = modifier
    )
}

/**
 * Body text content with rich formatting.
 */
@Composable
private fun BodyText(text: androidx.compose.ui.text.AnnotatedString, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.goatColors.onSurface,
        modifier = modifier
    )
}

/**
 * A food category entry in the guide: heading, demo row at 6 servings, and description.
 */
@Composable
private fun FoodCategoryGuideEntry(
    category: FoodCategory,
    heading: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.goatColors.onSurface,
        )
        Spacer(modifier = Modifier.height(GoatSpacing.s4))
        FoodCategoryRow(
            category = category,
            servingCount = 6,
            onIncrement = {},
            onDecrement = {},
        )
        Spacer(modifier = Modifier.height(GoatSpacing.s4))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.goatColors.onSurface,
        )
    }
}

/**
 * Data class representing a food category guide entry.
 */
private data class GuideEntry(
    val suite: ScoringSuite,
    val categoryId: CategoryId,
    val heading: String,
    val description: String,
)

// ── Breaking the rules section content ──
private const val BREAKING_THE_RULES_INTRO =
    "You 100% need to work out what works for you with this. Remember a food can cover more than one category and there are exceptions to every rule. Some examples:"
private const val BREAKING_THE_RULES_EXAMPLE =
    "- I might dilute 250ml of milkshake with another 250ml of milk and call it 2 portions of Dairy and 1 portion of Sweets."

// ── Other stuff section content ──
private const val OTHER_STUFF_VEGETARIAN_TITLE = "You're a vegetarian"
private const val OTHER_STUFF_VEGETARIAN =
    "While you could just wing it with the existing food groups, a vegetarian option that uses different categories is something I'd like to add if there is the interest. Let me know using the details at the bottom of the page."
private const val OTHER_STUFF_EATING_ON_THE_RUN_TITLE = "Eating on the run"
private const val OTHER_STUFF_EATING_ON_THE_RUN =
    "Anything you eat while exercising doesn't count. So go run an ultramarathon and stuff your face while doing so!"
private const val OTHER_STUFF_PROCESSED_TITLE = "Processed vs unprocessed"
private const val OTHER_STUFF_PROCESSED_CONTENT: String = "Matt (author of {Racing Weight}) is quite keen on unprocessed food, and the science is only getting stronger. It's not that all processed food is bad, but you'll find most of it is low quality. So if you can steer clear of it, you probably should."
private const val OTHER_STUFF_PROTEIN_SHAKES_TITLE = "Protein shakes"
private const val OTHER_STUFF_PROTEIN_SHAKES =
    "Unsweetened whey protein fits the nutrition profile of lean proteins, so count it as that. The sweetened powders and shakes can contain surprising amounts of sugar or artificial sweetener though, so you'll need to use your judgement there - we'd probably make a large shake and treat it as 1 portion of lean proteins and 1 portion of sweets."
private const val OTHER_STUFF_OFFICIAL_APP_TITLE = "There is already an official app"
private const val OTHER_STUFF_OFFICIAL_APP_CONTENT: String = "There is, and I've paid for it and used it for several weeks. But there's a lot about it that I don't like and development seems to have stagnated, so I built my own. If you feel bad for Matt, buy his book {Racing Weight} (or any of his other books). Actually, please just do that anyway!"

// ── Technical questions section content ──
private const val TECHNICAL_WHAT_HAPPENS_TO_DATA_TITLE = "What happens to your data?"
private const val TECHNICAL_WHAT_HAPPENS_TO_DATA =
    "It currently stays on your device, which means that once it's gone, it really is gone. We are planning on adding cloud backup at some point."
private const val TECHNICAL_UNINSTALL_TITLE = "What happens to my data if I uninstall the app?"
private const val TECHNICAL_UNINSTALL =
    "It's gone for good. Reinstalling will not bring it back (the tiny database is destroyed)."
private const val TECHNICAL_ACCOUNT_TITLE = "Do I need an account?"
private const val TECHNICAL_ACCOUNT =
    "No. I want people to use this app without an account and use it without needing an internet connection. If I add a cloud backup feature, that will require an account but it'll be an opt-in feature only."
private const val TECHNICAL_SUGGESTIONS_TITLE = "Suggestions for improvement"
private const val TECHNICAL_SUGGESTIONS_CONTENT: String = "If you have any suggestions, get in touch using {wayofthegoat@theportman.co} - yes, that is just a .co at the end."

/**
 * Helper function to create AnnotatedString with italic formatting for braced content.
 */
private fun buildAnnotatedString(content: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var lastIndex = 0
        val regex = "\\{([^}]+)\\}".toRegex()
        for (match in regex.findAll(content)) {
            append(content.substring(lastIndex, match.range.first))
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                append(match.groupValues[1])
            }
            lastIndex = match.range.last + 1
        }
        append(content.substring(lastIndex))
    }
}

/**
 * All food category guide entries.
 */
private val GUIDE_ENTRIES = listOf(
    // Healthy categories
    GuideEntry(
        SuiteDefinitions.BALANCED,
        CategoryId("veg"),
        "Vegetables",
        "Raw or cooked vegetables, pulses, tomatoes, chillies, eaten whole, chopped, pureed, whatever. One serving might be a fist-sized portion of veg, a decent side salad or a bowl of soup.",
    ),
    GuideEntry(
        SuiteDefinitions.BALANCED,
        CategoryId("fruit"),
        "Fruit",
        "Whole fruit, tinned fruit, canned fruit, smoothies and juices made with 100% fruit. One serving might be an apple or a banana, a handful of berries or a glass of juice. Something like apple crumble, you'd count as a portion of fruit and a portion of sweets.",
    ),
    GuideEntry(
        SuiteDefinitions.BALANCED,
        CategoryId("nuts"),
        "Nuts + seeds + healthy oils",
        "Any nuts, seeds and healthy oils (e.g. an olive oil-based salad-dressing). One portion would be a handful. Nut butters without added sugar also count.",
    ),
    GuideEntry(
        SuiteDefinitions.BALANCED,
        CategoryId("wholegrains"),
        "Whole grains",
        "Whole oats, wheat and other grains, including baked goods and pastas made with whole grain flours. One portion would be two slices of bread or a bowl of porridge.",
    ),
    GuideEntry(
        SuiteDefinitions.BALANCED,
        CategoryId("dairy"),
        "Dairy",
        "Unsweetened milk from cows, sheep and goats, unsweetened yoghurt, cheese, cream. And processed milks like soya milk. Small amounts of butter spread on bread do not count. A portion would be a glass of milk, two slices of cheese, a decent portion of yoghurt.",
    ),
    GuideEntry(
        SuiteDefinitions.BODY_COMPOSITION,
        CategoryId("leandairy"),
        "Lean dairy",
        "Lower-fat dairy options such as skimmed or semi-skimmed milk, low-fat yoghurt and cottage cheese. A portion would be a glass of milk or a decent serving of yoghurt. Used in place of Dairy in some scoring profiles.",
    ),
    GuideEntry(
        SuiteDefinitions.BALANCED,
        CategoryId("leanproteins"),
        "Lean proteins",
        "Unprocessed meats from land animals and fish. And eggs. One portion would be a chicken breast, regular-sized steak or fish fillet or 2 eggs. Listed as \"Lean meats\" in some scoring profiles.",
    ),
    // Unhealthy categories
    GuideEntry(
        SuiteDefinitions.BALANCED,
        CategoryId("refinedgrains"),
        "Refined grains",
        "White rice, white flour, most pastas, cereals, breads and other baked goods. A portion would be two slices of bread, a bowl of rice or pasta, etc.",
    ),
    GuideEntry(
        SuiteDefinitions.BALANCED,
        CategoryId("sweets"),
        "Sweets",
        "Anything with a substantial amount of sugar and anything artificially sweetened: sweets, pastries and other desserts, sugary drinks, energy bars, many breakfast cereals, yoghurts with sugar listed as their second ingredient.",
    ),
    GuideEntry(
        SuiteDefinitions.BALANCED,
        CategoryId("fattymeats"),
        "Fatty meats",
        "Meats that have been processed beyond cutting, grinding and seasoning: sausages, ham, bacon, corned beef, jerky, most fast foods.",
    ),
    GuideEntry(
        SuiteDefinitions.RACING_WEIGHT,
        CategoryId("fattyproteins"),
        "Fatty proteins",
        "Processed and fatty meats and proteins: sausages, ham, bacon, corned beef, jerky, most fast foods. Used in place of Fatty meats in some scoring profiles.",
    ),
    GuideEntry(
        SuiteDefinitions.RACING_WEIGHT,
        CategoryId("friedfoods"),
        "Fried foods",
        "Chips (fries), crisps, fried chicken or fish, donuts. Use your common sense with serving sizes.",
    ),
    GuideEntry(
        SuiteDefinitions.BALANCED,
        CategoryId("junkfoods"),
        "Junk foods",
        "Fried foods, crisps, fast food and other low-quality snack foods. Chips (fries), fried chicken or fish, donuts and similar items. Use your common sense with serving sizes.",
    ),
)

