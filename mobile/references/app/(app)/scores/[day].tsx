import React, { useState } from "react";
import { useLocalSearchParams } from "expo-router";

import { TwContainer } from "@/core/components/TwContainer";
import { Day } from "@/features/scores/components/Day";
import { Dimensions, FlatList, View } from "react-native";
import { useDatabase } from "@/core/hooks";
import dayjs from "dayjs";

export default function Scores() {
  const db = useDatabase();
  const { day } = useLocalSearchParams<{ day: string }>();

  const { width } = Dimensions.get("window");
  const [days, setDays] = useState(() => {
    /**
     * Because this screen is referring to a specific date,
     * we're setting the initial state to be a list of all the
     * dates from the selected date to today's date.
     * This performs much better than sticking with just a date either side and
     * using onStartReached.
     * It's not an issue because it's only a list of dates anyway.
     */
    const today = dayjs();
    const selectedDay = dayjs(day);
    const diffInDays = today.diff(selectedDay, "day");
    const daysArray = [];
    for (let i = diffInDays; i >= 0; i--) {
      daysArray.push({ date: selectedDay.add(+i, "day").toDate() }); // Use dayjs.add
    }
    return daysArray;
  });

  function handleEndReached() {
    const lastDate = new Date(days[days.length - 1].date);
    const newDate = new Date(lastDate.setDate(lastDate.getDate() - 1));
    setDays([...days, { date: newDate }]);
  }

  return (
    <TwContainer twc="flex-1 flex-col justify-center">
      {db && days.length && (
        <FlatList
          data={days}
          renderItem={({ item }) => (
            <Day db={db} date={item.date} width={width} />
          )}
          keyExtractor={(item) => item.date.toISOString()}
          horizontal
          inverted
          snapToInterval={width}
          decelerationRate="fast"
          initialScrollIndex={days.length - 1}
          onEndReached={handleEndReached}
          onEndReachedThreshold={0.5}
          extraData={days}
          getItemLayout={(_, index) => ({
            length: width,
            offset: width * index,
            index,
          })}
        />
      )}
    </TwContainer>
  );
}
