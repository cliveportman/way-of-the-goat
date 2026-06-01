import { TouchableOpacity } from "react-native";
import { router } from "expo-router";
import { TwContainer } from "@/core/components/TwContainer";
import { TwText } from "@/core/components/TwText";
import React, { useEffect, useState } from "react";
import database from "@/core/database";
import { useDatabase } from "@/core/hooks";
import { GoatMoonSvg } from "@/core/svgs/GoatMoonSvg";

export default function Homepage() {
  const db = useDatabase();

  // If the user has onboarded, they should be taken to the scores page, so find out here.
  const [onboardedDate, setOnboardedDate] = useState<string | null>(null);
  useEffect(() => {
    if (db) {
      database.getMetaField(db, "onboardedDate").then((date) => {
        if (date) setOnboardedDate(date);
      });
    }
    // Use this for resetting the onboardedDate field in the database during development.
    // if (db) database.updateMetaField(db, "onboardedDate", null);
    // Use this for deleting duplicate days in the database during development.
    // if (db) database.deleteDuplicateDays(db);
  }, [db]);

  return (
    <TwContainer twc="flex-1 bg-slate-950">
      <TwContainer twc="flex-1 flex-col justify-between px-6">
        <TwContainer twc={"flex-1 flex-col justify-end items-center"}>
          <GoatMoonSvg tw={"block w-64 h-64 mb-6"} />
          <TwText variant="title" twc={"text-4xl text-center mb-3"}>
            Way of the Goat
          </TwText>
          <TwText variant="subtitle" twc={"text-slate-400 text-center mb-6"}>
            Diet scoring for endurance athletes
          </TwText>
          <TwText twc={"text-slate-200 mb-0.5"}>Based on the book</TwText>
          <TwText twc={"text-slate-200 mb-0.5 italic"}>Racing Weight</TwText>
          <TwText twc={"text-slate-200 mb-0"}>by Matt Fitzgerald</TwText>
        </TwContainer>
        <TwContainer twc={"flex-1 flex-col items-center justify-end"}>
          <TouchableOpacity
            tw={
              "flex-col justify-center items-center text-center text-white border border-slate-800 bg-slate-900 w-32 h-32 rounded-full mb-16"
            }
            onPress={() =>
              router.push(onboardedDate ? "/(app)/scores" : "/onboarding")
            }
          >
            <TwText twc={"mb-0"}>Continue</TwText>
          </TouchableOpacity>
          <TwText twc={"text-slate-700 text-xs mb-3"}>
            Onboarded {onboardedDate}
          </TwText>
        </TwContainer>
      </TwContainer>
    </TwContainer>
  );
}
