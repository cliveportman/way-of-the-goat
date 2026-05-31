import React from "react";
import { TouchableOpacity, Text } from "react-native";
import { router } from "expo-router";
import * as Haptics from "expo-haptics";
import { TwContainer } from "@/core/components/TwContainer";
import { TwText } from "@/core/components/TwText";
import { Score } from "@/features/scores/components/Score";
import { FoodCat } from "@/core/enums";
import { maxScores } from "@/core/constants";
import { useState } from "react";
import database from "@/core/database";
import { useDatabase } from "@/core/hooks";

export default function OnboardingPage3() {
  const db = useDatabase();

  const [fruit, setFruit] = useState(3);
  const [grains, setGrains] = useState(2);
  const addFruit = () => {
    setFruit(fruit + 1);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
  };
  const addGrains = () => {
    setGrains(grains + 1);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
  };
  const removeFruit = () => {
    setFruit(fruit - 1);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
  };
  const removeGrains = () => {
    setGrains(grains - 1);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
  };

  function handleContinue() {
    if (!db) return;
    const date = new Date();
    database
      .updateMetaField(db, "onboardedDate", date.toISOString().split("T")[0])
      .then(() => {
        // No nice way of preventing the back button from going back to the onboarding pages.
        // We'll just have to wait for Expo Router to support router.reset() or similar..
        router.push("/(app)/scores");
      });
  }

  return (
    <TwContainer twc="flex-1 bg-slate-950">
      <TwContainer twc="flex-1 flex-col justify-between px-3 pt-12">
        <TwContainer twc={"flex-1 flex-col justify-start"}>
          <TwText variant="title" twc={""}>
            How?
          </TwText>
          <TwText variant="large" twc={"text-slate-300 mb-6"}>
            Each day, record your dietary intake using the food categories
            provided. Nutritious foods will raise your score, unhealthy foods
            will lower it.
          </TwText>
          <TwContainer twc={"flex-col justify-center mb-6"}>
            <Score
              text={"Fruit"}
              cat={FoodCat.fruit}
              servings={fruit}
              maxScores={maxScores.fruit}
              onPress={addFruit}
              onLongPress={removeFruit}
            />
            <Score
              text={"Refined grains"}
              cat={FoodCat.refinedgrains}
              servings={grains}
              maxScores={maxScores.refinedgrains}
              onPress={addGrains}
              onLongPress={removeGrains}
            />
          </TwContainer>
          <TwText variant="large" twc={"text-slate-300 mb-6"}>
            To <Text style={{ fontWeight: 600 }}>add a serving</Text> tap the
            food category.
          </TwText>
          <TwText variant="large" twc={"text-slate-300 mb-6"}>
            To <Text style={{ fontWeight: 600 }}>remove a serving</Text> press
            and hold the food category.
          </TwText>
          <TwText variant="large" twc={"text-slate-300 mb-6"}>
            You can practice on the two foods above.
          </TwText>
        </TwContainer>
        <TwContainer twc={"flex-1 flex-col items-center justify-end"}>
          <TouchableOpacity
            tw={
              "flex-col justify-center items-center text-center text-white border border-slate-800 bg-slate-900 w-32 h-32 rounded-full mb-16"
            }
            onPress={handleContinue}
          >
            <TwText twc={"mb-0"}>Continue</TwText>
          </TouchableOpacity>
        </TwContainer>
      </TwContainer>
    </TwContainer>
  );
}
