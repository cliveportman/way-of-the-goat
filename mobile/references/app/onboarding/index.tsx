import React from "react";
import { TouchableOpacity, Text } from "react-native";
import { router } from "expo-router";
import { TwContainer } from "@/core/components/TwContainer";
import { TwText } from "@/core/components/TwText";

export default function OnboardingPage1() {
  return (
    <TwContainer twc="flex-1 bg-slate-950">
      <TwContainer twc="flex-1 flex-col justify-between px-3 pt-12">
        <TwContainer twc={"flex-1 flex-col justify-start"}>
          <TwText variant="title" twc={""}>
            What?
          </TwText>
          <TwText variant="large" twc={"text-slate-300 mb-6"}>
            This app is mainly intended for endurance athletes who've read the
            book <Text style={{ fontStyle: "italic" }}>Racing Weight</Text> by
            Matt Fitzgerald. But can be used by anyone who wants to improve
            their diet.
          </TwText>
          <TwText variant="large" twc={"text-slate-300 mb-6"}>
            For athletes, the principle is that by working towards your optimal
            racing weight, you'll perform better in your races.
          </TwText>
          <TwText variant="large" twc={"text-slate-300 mb-6"}>
            And you achieve this by focusing on diet quality, not calorie
            counting.
          </TwText>
        </TwContainer>
        <TwContainer twc={"flex-1 flex-col items-center justify-end"}>
          <TouchableOpacity
            tw={
              "flex-col justify-center items-center text-center text-white border border-slate-800 bg-slate-900 w-32 h-32 rounded-full mb-16"
            }
            onPress={() => router.push("/onboarding/page2")}
          >
            <TwText twc={"mb-0"}>Continue</TwText>
          </TouchableOpacity>
        </TwContainer>
      </TwContainer>
    </TwContainer>
  );
}
