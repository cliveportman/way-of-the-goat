import { Pressable } from "react-native";

import { FoodCat } from "@/core/enums";
import type { PossibleSingleServingScores } from "@/core/types";
import React from "react";
import { ScoreServing } from "@/features/scores/components/ScoreServing";
import { ScoreLabel } from "@/features/scores/components/ScoreLabel";

type ScoreProps = {
  text: string;
  cat: FoodCat;
  servings: number;
  maxScores: PossibleSingleServingScores;
  onPress: (cat: FoodCat) => void;
  onLongPress: (cat: FoodCat) => void;
  twc?: string;
};

export function Score({
  text,
  cat,
  servings,
  maxScores,
  onPress,
  onLongPress,
  twc = "",
}: ScoreProps) {
  /**
   * Helper function for determining the serving size.
   * It'll be called 6 times, passing an index from 0 to 5.
   * e.g. If servings = 3, the first three loops through this helper should return 1 and the others 0.
   * We're also including support for half servings, e.g. serving = 3.5.
   * @param index
   */
  function getServingSize(index: number) {
    // future cases where we accept 0.5 servings
    if (servings - index === 0.5) return 0.5;
    else if (servings > index) return 1;
    else return 0;
  }

  return (
    <Pressable
      tw={`w-full flex-row mb-1 ${twc}`}
      onPress={() => onPress(cat)}
      onLongPress={() => onLongPress(cat)}
    >
      <ScoreLabel text={text} />
      {maxScores.map((max, i) => (
        <ScoreServing key={i} maxScore={max} serving={getServingSize(i)} />
      ))}
    </Pressable>
  );
}
