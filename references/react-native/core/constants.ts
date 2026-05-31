import { PossibleSingleServingScores, Servings } from "@/core/types";
// Maximum possible scores for each food category, if a user eats the max number of servings (6).
export const maxScores = {
  veg: [2, 2, 2, 1, 0, 0] as PossibleSingleServingScores,
  fruit: [2, 2, 2, 1, 0, 0] as PossibleSingleServingScores,
  nuts: [2, 2, 1, 0, 0, -1] as PossibleSingleServingScores,
  wholegrains: [2, 2, 1, 0, 0, -1] as PossibleSingleServingScores,
  dairy: [2, 1, 1, 0, -1, -2] as PossibleSingleServingScores,
  leanproteins: [2, 1, 1, 0, -1, -2] as PossibleSingleServingScores,
  beverages: [1, 1, 0, 0, 0, 0] as PossibleSingleServingScores,
  refinedgrains: [-1, -1, -2, -2, -2, -2] as PossibleSingleServingScores,
  sweets: [-2, -2, -2, -2, -2, -2] as PossibleSingleServingScores,
  fattyproteins: [-2, -2, -2, -2, -2, -2] as PossibleSingleServingScores,
  friedfoods: [-2, -2, -2, -2, -2, -2] as PossibleSingleServingScores,
  alcohol: [0, -2, -2, -2, -2, -2] as PossibleSingleServingScores,
  other: [-1, -2, -2, -2, -2, -2] as PossibleSingleServingScores,
};

export const defaultServings: Servings = {
  date: "",
  veg: 0,
  fruit: 0,
  nuts: 0,
  wholegrains: 0,
  dairy: 0,
  leanproteins: 0,
  beverages: 0,
  refinedgrains: 0,
  sweets: 0,
  fattyproteins: 0,
  friedfoods: 0,
  alcohol: 0,
  other: 0,
};
