import { FoodCat } from "@/core/enums";
import TailwindColors from "tailwindcss/colors";
import Toast from "react-native-root-toast";
import {
  DateString,
  DayTotalsForDisplay,
  DayTotalsForMaths,
  Servings,
} from "@/core/types";
import { maxScores } from "@/core/constants";
import dayjs from "dayjs";
import customParseFormat from "dayjs/plugin/customParseFormat";
import advancedFormat from "dayjs/plugin/advancedFormat";
import isToday from "dayjs/plugin/isToday";
import weekday from "dayjs/plugin/weekday";
import updateLocale from "dayjs/plugin/updateLocale";

dayjs.extend(customParseFormat);
dayjs.extend(advancedFormat);
dayjs.extend(isToday);
dayjs.extend(weekday);
dayjs.extend(updateLocale);

export function foodCatToText(cat: FoodCat) {
  switch (cat) {
    case FoodCat.veg:
      return "Vegetables";
    case FoodCat.fruit:
      return "Fruit";
    case FoodCat.nuts:
      return "Nuts + seeds";
    case FoodCat.wholegrains:
      return "Whole grains";
    case FoodCat.dairy:
      return "Dairy";
    case FoodCat.leanproteins:
      return "Lean meats";
    case FoodCat.beverages:
      return "Beverages";
    case FoodCat.refinedgrains:
      return "Refined grains";
    case FoodCat.sweets:
      return "Sweets";
    case FoodCat.fattyproteins:
      return "Fatty meats";
    case FoodCat.friedfoods:
      return "Fried foods";
    case FoodCat.alcohol:
      return "Alcohol";
    case FoodCat.other:
      return "Other";
  }
}

export function shortToast(
  message: string,
  status: "success" | "info" | "error" = "info",
) {
  Toast.show(message, {
    duration: Toast.durations.SHORT,
    position: 90,
    shadow: true,
    animation: true,
    hideOnPress: true,
    delay: 0,
    backgroundColor:
      status === "success"
        ? TailwindColors.lime[600]
        : status === "error"
          ? TailwindColors.red[800]
          : TailwindColors.slate[600],
  });
}

/**
 * Get the total scores for a day, for display in the UI.
 * @param servings
 */
export function getTotalScoresForDisplay(
  servings: Servings,
): DayTotalsForDisplay {
  const healthyScore = getHealthyServingsScore(servings);
  const unhealthyScore = getUnhealthyServingsScore(servings);
  const portions = getNumberOfServings(servings);
  if (healthyScore === 0 && unhealthyScore === 0)
    return {
      healthy: "---",
      unhealthy: "---",
      total: "---",
      portions: 0,
    };
  else
    return {
      healthy: "+" + healthyScore,
      unhealthy: unhealthyScore,
      total:
        healthyScore + unhealthyScore > 0
          ? "+" + (healthyScore + unhealthyScore)
          : healthyScore + unhealthyScore,
      portions: portions,
    };
}

/**
 * Get the total scores for a day, for use in maths, charts, etc. Returns numbers only.
 * @param servings
 */
export function getTotalScoresForMaths(servings: Servings): DayTotalsForMaths {
  return {
    healthy: getHealthyServingsScore(servings),
    unhealthy: getUnhealthyServingsScore(servings),
    total:
      getHealthyServingsScore(servings) + getUnhealthyServingsScore(servings),
    portions: getNumberOfServings(servings),
  };
}

export function getHealthyServingsScore(servings: Servings) {
  let total = 0;
  for (const cat in servings) {
    // Filter out the unhealthy stuff
    if (
      cat === "id" ||
      cat === "date" ||
      cat === FoodCat.refinedgrains ||
      cat === FoodCat.sweets ||
      cat === FoodCat.fattyproteins ||
      cat === FoodCat.friedfoods ||
      cat === FoodCat.alcohol ||
      cat === FoodCat.other
    )
      continue;
    // Map the scores from SingleServingScores to numbers, then reduce them to a single number.
    total += maxScores[cat as FoodCat]
      .map((score) => Number(score))
      .reduce(
        (acc, curr, i) => {
          return servings[cat as FoodCat] > i ? acc + curr : acc;
        },
        0, // Don't forget the initial value!
      );
  }
  return total;
}

export function getUnhealthyServingsScore(servings: Servings) {
  let total = 0;
  for (const cat in servings) {
    // Filter out the healthy stuff
    if (
      cat === "id" ||
      cat === "date" ||
      cat === FoodCat.veg ||
      cat === FoodCat.fruit ||
      cat === FoodCat.nuts ||
      cat === FoodCat.wholegrains ||
      cat === FoodCat.dairy ||
      cat === FoodCat.leanproteins ||
      cat === FoodCat.beverages
    )
      continue;
    // Map the scores from SingleServingScores to numbers, then reduce them to a single number.
    total += maxScores[cat as FoodCat]
      .map((score) => Number(score))
      .reduce(
        (acc, curr, i) => {
          return servings[cat as FoodCat] > i ? acc + curr : acc;
        },
        0, // Don't forget the initial value!
      );
  }
  return total;
}

function getNumberOfServings(servings: Servings) {
  let total = 0;
  for (const cat in servings) {
    if (cat === "id" || cat === "date") continue;
    total += servings[cat as FoodCat];
  }
  return total;
}

export function getWeek(index: number) {
  const date = dayjs().subtract(index, "week");
  const start = date.startOf("week").add(1, "day");
  const end = date.endOf("week").add(1, "day");
  const dates: DateString[] = [];
  for (let d = start; d <= end; d = d.add(1, "day")) {
    dates.push(d.format("YYYY-MM-DD") as DateString);
  }
  return dates;
}
