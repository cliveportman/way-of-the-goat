import { FoodCat } from "@/core/enums";
import { DateString, Servings } from "@/core/types";

import { SQLiteDatabase } from "expo-sqlite";
import * as Haptics from "expo-haptics";

import { TwContainer } from "@/core/components/TwContainer";
import { TwText } from "@/core/components/TwText";
import { Score } from "@/features/scores/components/Score";
import { defaultServings, maxScores } from "@/core/constants";
import React, { useEffect, useMemo, useState, memo } from "react";
import database from "@/core/database";
import dayjs from "dayjs";
import { View } from "react-native";
import { foodCatToText, shortToast } from "@/core/helpers";
import { getTotalScoresForDisplay } from "@/core/helpers";

type DayProps = {
  db: SQLiteDatabase;
  date: Date;
  width: number;
};

export const Day = memo(
  ({ db, date, width }: DayProps) => {
    // Fetch servings for the current date from the database.
    const [servings, setServings] = useState<Servings>(defaultServings);
    useEffect(() => {
      const dateStr = date.toISOString().split("T")[0] as DateString;
      database.getServingsByDate(db, dateStr).then(async (results) => {
        if (results) setServings(results);
        // If there are no servings for the current date, add a new day to local state only.
        // This will be saved to the database when the user increments a category only, so we have no empty days in the database.
        else setServings({ ...defaultServings, date: dateStr });
      });
    }, [db, date]);

    /**
     * Press handler - increments the number of servings for a food category.
     * @param cat
     */
    async function handlePress(cat: FoodCat) {
      if (db && servings[cat] < 6) {
        let result: Servings | null = null;
        // If there is an existing record for this date (i.e. we have an ID), update it
        if (servings.id)
          result = await database.updateServingsCategory(
            db,
            servings.id,
            cat,
            servings[cat] + 1,
          );
        // Otherwise, insert a new record for this date
        else {
          // Create a copy of the current servings object
          const duplicate = { ...servings };
          duplicate[cat] = 1; // Update the category we're incrementing
          const id = await database.insertServings(db, duplicate); // Insert the new record
          if (id) result = { ...duplicate, id: id };
        }
        if (result) setServings(result);
        await Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
        shortToast(`+1 serving of ${foodCatToText(cat).toLowerCase()}`);
      }
    }

    /**
     * Long press handler - decrements the number of servings for a food category.
     * @param cat
     */
    async function handleLongPress(cat: FoodCat) {
      if (db && servings.id && servings[cat] > 0) {
        const result = await database.updateServingsCategory(
          db,
          servings.id,
          cat,
          servings[cat] - 1,
        );
        setServings(result);
        await Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
        shortToast(`-1 serving of ${foodCatToText(cat).toLowerCase()}`);
      }
    }

    const totals = useMemo(
      () => getTotalScoresForDisplay(servings),
      [servings],
    );

    return (
      <View
        tw={`flex-1 flex-col justify-between px-3 pt-12 bg-slate-950`}
        style={{ width: width }}
      >
        <TwContainer twc={"mb-3"}>
          <TwText variant="title" twc={"text-left"}>
            {dayjs(date).format("ddd DD MMM")}
          </TwText>
        </TwContainer>

        <View>
          <Score
            servings={servings.veg}
            maxScores={maxScores.veg}
            text={foodCatToText(FoodCat.veg)}
            cat={FoodCat.veg}
            onPress={handlePress}
            onLongPress={handleLongPress}
          />
          <Score
            servings={servings.fruit}
            maxScores={maxScores.fruit}
            text={foodCatToText(FoodCat.fruit)}
            cat={FoodCat.fruit}
            onPress={handlePress}
            onLongPress={handleLongPress}
          />
          <Score
            servings={servings.nuts}
            maxScores={maxScores.nuts}
            text={foodCatToText(FoodCat.nuts)}
            cat={FoodCat.nuts}
            onPress={handlePress}
            onLongPress={handleLongPress}
          />
          <Score
            servings={servings.wholegrains}
            maxScores={maxScores.wholegrains}
            text={foodCatToText(FoodCat.wholegrains)}
            cat={FoodCat.wholegrains}
            onPress={handlePress}
            onLongPress={handleLongPress}
          />
          <Score
            servings={servings.dairy}
            maxScores={maxScores.dairy}
            text={foodCatToText(FoodCat.dairy)}
            cat={FoodCat.dairy}
            onPress={handlePress}
            onLongPress={handleLongPress}
          />
          <Score
            servings={servings.leanproteins}
            maxScores={maxScores.leanproteins}
            text={foodCatToText(FoodCat.leanproteins)}
            cat={FoodCat.leanproteins}
            onPress={handlePress}
            onLongPress={handleLongPress}
          />
          {/*<Score servings={servings.beverages} maxScores={maxScores.beverages} text={foodCatToText(FoodCat.beverages)} cat={FoodCat.beverages} onPress={handlePress} onLongPress={handleLongPress} />*/}
          <Score
            servings={servings.refinedgrains}
            maxScores={maxScores.refinedgrains}
            text={foodCatToText(FoodCat.refinedgrains)}
            cat={FoodCat.refinedgrains}
            onPress={handlePress}
            onLongPress={handleLongPress}
          />
          <Score
            servings={servings.sweets}
            maxScores={maxScores.sweets}
            text={foodCatToText(FoodCat.sweets)}
            cat={FoodCat.sweets}
            onPress={handlePress}
            onLongPress={handleLongPress}
          />
          <Score
            servings={servings.fattyproteins}
            maxScores={maxScores.fattyproteins}
            text={foodCatToText(FoodCat.fattyproteins)}
            cat={FoodCat.fattyproteins}
            onPress={handlePress}
            onLongPress={handleLongPress}
          />
          <Score
            servings={servings.friedfoods}
            maxScores={maxScores.friedfoods}
            text={foodCatToText(FoodCat.friedfoods)}
            cat={FoodCat.friedfoods}
            onPress={handlePress}
            onLongPress={handleLongPress}
          />
          <Score
            servings={servings.alcohol}
            maxScores={maxScores.alcohol}
            text={foodCatToText(FoodCat.alcohol)}
            cat={FoodCat.alcohol}
            onPress={handlePress}
            onLongPress={handleLongPress}
          />
          {/*<Score servings={servings.other} maxScores={maxScores.other} text={foodCatToText(FoodCat.other)} cat={FoodCat.other} onPress={handlePress} onLongPress={handleLongPress} />*/}
        </View>
        <TwContainer twc={"flex-row justify-between px-1.5 mb-3"}>
          <TwContainer twc={"w-1/4"} />
          <TwContainer twc={"w-1/2 pt-3"}>
            <TwText variant="title" twc={"text-center text-5xl mb-0"}>
              {totals.total}
            </TwText>
            <TwText
              variant="copy"
              twc={"text-center"}
            >{`${totals.portions} portion${totals.portions !== 1 ? "s" : ""}`}</TwText>
          </TwContainer>
          <TwContainer twc={"w-1/4"}>
            <TwText variant="subheading" twc={"text-right text-green-400 mb-0"}>
              {totals.healthy}
            </TwText>
            <TwText variant="subheading" twc={"text-right text-red-400"}>
              {totals.unhealthy}
            </TwText>
          </TwContainer>
        </TwContainer>
      </View>
    );
  },
  (prevProps, nextProps) => {
    return prevProps.date === nextProps.date;
  },
);

Day.displayName = "Day";
