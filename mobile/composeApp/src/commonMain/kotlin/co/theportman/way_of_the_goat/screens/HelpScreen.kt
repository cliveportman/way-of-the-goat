package co.theportman.way_of_the_goat.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
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
import co.theportman.way_of_the_goat.ui.theme.goatColors

@Composable
fun HelpScreen(
    onNavigateToDesignTokens: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
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
                FoodCategoryRow(
                    category = balanced.categoryById("fruit"),
                    servingCount = 2,
                    onIncrement = {},
                    onDecrement = {},
                )
                FoodCategoryRow(
                    category = balanced.categoryById("refinedgrains"),
                    servingCount = 3,
                    onIncrement = {},
                    onDecrement = {},
                )
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

        // Healthy categories
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.BALANCED.categoryById("veg"),
                heading = "Vegetables",
                description = "Raw or cooked vegetables, pulses, tomatoes, chillies, eaten whole, chopped, pureed, whatever. One serving might be a fist-sized portion of veg, a decent side salad or a bowl of soup.",
            )
        }
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.BALANCED.categoryById("fruit"),
                heading = "Fruit",
                description = "Whole fruit, tinned fruit, canned fruit, smoothies and juices made with 100% fruit. One serving might be an apple or a banana, a handful of berries or a glass of juice. Something like apple crumble, you'd count as a portion of fruit and a portion of sweets.",
            )
        }
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.BALANCED.categoryById("nuts"),
                heading = "Nuts + seeds + healthy oils",
                description = "Any nuts, seeds and healthy oils (e.g. an olive oil-based salad-dressing). One portion would be a handful. Nut butters without added sugar also count.",
            )
        }
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.BALANCED.categoryById("wholegrains"),
                heading = "Whole grains",
                description = "Whole oats, wheat and other grains, including baked goods and pastas made with whole grain flours. One portion would be two slices of bread or a bowl of porridge.",
            )
        }
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.BALANCED.categoryById("dairy"),
                heading = "Dairy",
                description = "Unsweetened milk from cows, sheep and goats, unsweetened yoghurt, cheese, cream. And processed milks like soya milk. Small amounts of butter spread on bread do not count. A portion would be a glass of milk, two slices of cheese, a decent portion of yoghurt.",
            )
        }
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.BODY_COMPOSITION.categoryById("leandairy"),
                heading = "Lean dairy",
                description = "Lower-fat dairy options such as skimmed or semi-skimmed milk, low-fat yoghurt and cottage cheese. A portion would be a glass of milk or a decent serving of yoghurt. Used in place of Dairy in some scoring profiles.",
            )
        }
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.BALANCED.categoryById("leanproteins"),
                heading = "Lean proteins",
                description = "Unprocessed meats from land animals and fish. And eggs. One portion would be a chicken breast, regular-sized steak or fish fillet or 2 eggs. Listed as \"Lean meats\" in some scoring profiles.",
            )
        }

        // Unhealthy categories
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.BALANCED.categoryById("refinedgrains"),
                heading = "Refined grains",
                description = "White rice, white flour, most pastas, cereals, breads and other baked goods. A portion would be two slices of bread, a bowl of rice or pasta, etc.",
            )
        }
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.BALANCED.categoryById("sweets"),
                heading = "Sweets",
                description = "Anything with a substantial amount of sugar and anything artificially sweetened: sweets, pastries and other desserts, sugary drinks, energy bars, many breakfast cereals, yoghurts with sugar listed as their second ingredient.",
            )
        }
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.BALANCED.categoryById("fattymeats"),
                heading = "Fatty meats",
                description = "Meats that have been processed beyond cutting, grinding and seasoning: sausages, ham, bacon, corned beef, jerky, most fast foods.",
            )
        }
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.RACING_WEIGHT.categoryById("fattyproteins"),
                heading = "Fatty proteins",
                description = "Processed and fatty meats and proteins: sausages, ham, bacon, corned beef, jerky, most fast foods. Used in place of Fatty meats in some scoring profiles.",
            )
        }
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.RACING_WEIGHT.categoryById("friedfoods"),
                heading = "Fried foods",
                description = "Chips (fries), crisps, fried chicken or fish, donuts. Use your common sense with serving sizes.",
            )
        }
        item {
            FoodCategoryGuideEntry(
                category = SuiteDefinitions.BALANCED.categoryById("junkfoods"),
                heading = "Junk foods",
                description = "Fried foods, crisps, fast food and other low-quality snack foods. Chips (fries), fried chicken or fish, donuts and similar items. Use your common sense with serving sizes.",
            )
        }

        // ── Breaking the rules ────────────────────────────────
        item { SectionDivider() }
        item {
            Text(
                text = "Breaking the rules",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = "You 100% need to work out what works for you with this. Remember a food can cover more than one category and there are exceptions to every rule. Some examples:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = "- I might dilute 250ml of milkshake with another 250ml of milk and call it 2 portions of Dairy and 1 portion of Sweets.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        // ── Other stuff ───────────────────────────────────────
        item { SectionDivider() }
        item {
            Text(
                text = "Other stuff",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        item {
            Text(
                text = "You're a vegetarian",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = "While you could just wing it with the existing food groups, a vegetarian option that uses different categories is something I'd like to add if there is the interest. Let me know using the details at the bottom of the page.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        item {
            Text(
                text = "Eating on the run",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = "Anything you eat while exercising doesn't count. So go run an ultramarathon and stuff your face while doing so!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        item {
            Text(
                text = "Processed vs unprocessed",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = buildAnnotatedString {
                    append("Matt (author of ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("Racing Weight")
                    }
                    append(") is quite keen on unprocessed food, and the science is only getting stronger. It's not that all processed food is bad, but you'll find most of it is low quality. So if you can steer clear of it, you probably should.")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        item {
            Text(
                text = "Protein shakes",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = "Unsweetened whey protein fits the nutrition profile of lean proteins, so count it as that. The sweetened powders and shakes can contain surprising amounts of sugar or artificial sweetener though, so you'll need to use your judgement there - we'd probably make a large shake and treat it as 1 portion of lean proteins and 1 portion of sweets.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        item {
            Text(
                text = "There is already an official app",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = buildAnnotatedString {
                    append("There is, and I've paid for it and used it for several weeks. But there's a lot about it that I don't like and development seems to have stagnated, so I built my own. If you feel bad for Matt, buy his book ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("Racing Weight")
                    }
                    append(" (or any of his other books). Actually, please just do that anyway!")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        // ── Technical questions ───────────────────────────────
        item { SectionDivider() }
        item {
            Text(
                text = "Technical questions",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        item {
            Text(
                text = "What happens to your data?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = "It currently stays on your device, which means that once it's gone, it really is gone. We are planning on adding cloud backup at some point.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        item {
            Text(
                text = "What happens to my data if I uninstall the app?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = "It's gone for good. Reinstalling will not bring it back (the tiny database is destroyed).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        item {
            Text(
                text = "Do I need an account?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = "No. I want people to use this app without an account and use it without needing an internet connection. If I add a cloud backup feature, that will require an account but it'll be an opt-in feature only.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

        item {
            Text(
                text = "Suggestions for improvement",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.goatColors.onSurface,
            )
        }
        item {
            Text(
                text = buildAnnotatedString {
                    append("If you have any suggestions, get in touch using ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("wayofthegoat@theportman.co")
                    }
                    append(" - yes, that is just a .co at the end.")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.goatColors.onSurface,
            )
        }

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDesignTokens() }
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

        // Bottom padding for nav bar clearance
        item { Spacer(modifier = Modifier.height(GoatSpacing.s48)) }
    }
}

/**
 * A divider used between major guide sections.
 */
@Composable
private fun SectionDivider() {
    HorizontalDivider(
        color = MaterialTheme.goatColors.outline,
        modifier = Modifier.padding(vertical = GoatSpacing.s8)
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
) {
    Column {
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
 * Extension to look up a category by its string ID within a suite.
 */
private fun ScoringSuite.categoryById(id: String): FoodCategory =
    categories.first { it.id == CategoryId(id) }
