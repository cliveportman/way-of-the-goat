import React from "react";
import { TouchableOpacity } from "react-native";
import { router } from "expo-router";
import { TwContainer } from "@/core/components/TwContainer";
import { TwText } from "@/core/components/TwText";

export default function OnboardingPage2() {
  return (
    <TwContainer twc="flex-1 bg-slate-950">
      <TwContainer twc="flex-1 flex-col justify-between px-3 pt-12">
        <TwContainer twc={"flex-1 flex-col justify-start"}>
          <TwText variant="title" twc={""}>
            Why?
          </TwText>
          <TwText variant="large" twc={"text-slate-300 mb-6"}>
            Weigh yourself regularly, and you're more likely to hit your weight
            goal. Monitor your diet regularly, you're more likely to nail your
            nutrition, too.
          </TwText>
          <TwText variant="large" twc={"text-slate-300 mb-6"}>
            The benefits of a nutritious diet are well known. But for endurance
            athletes, there's no escaping that body-fat percentage is a better
            predictor of success than training miles. What to do?
          </TwText>
          <TwText variant="large" twc={"text-slate-300 mb-6"}>
            This app exists to help you quickly and easily monitor your diet,
            and score it, helping you achieve those podium places. It's super
            quick and really requires no effort at all. The app, that is...
          </TwText>
        </TwContainer>
        <TwContainer twc={"flex-1 flex-col items-center justify-end"}>
          <TouchableOpacity
            tw={
              "flex-col justify-center items-center text-center text-white border border-slate-800 bg-slate-900 w-32 h-32 rounded-full mb-16"
            }
            onPress={() => router.push("/onboarding/page3")}
          >
            <TwText twc={"mb-0"}>Continue</TwText>
          </TouchableOpacity>
        </TwContainer>
      </TwContainer>
    </TwContainer>
  );
}
