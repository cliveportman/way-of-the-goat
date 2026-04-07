import React, { useState } from "react";
import { TwContainer } from "@/core/components/TwContainer";
import { TwText } from "@/core/components/TwText";
import { Dimensions, FlatList } from "react-native";
import { getWeek } from "@/core/helpers";
import { Week } from "@/features/progress/components/Week";

export default function ProgressPage() {
  const { width } = Dimensions.get("window");

  const [weeks, setWeeks] = useState([getWeek(0), getWeek(1)]);
  function handleEndReached() {
    setWeeks([...weeks, getWeek(weeks.length)]);
  }

  return (
    <TwContainer twc="flex-1 bg-slate-950 pt-12">
      <TwContainer twc={"flex-col justify-end items-start px-3"}>
        <TwText variant="title">Progress</TwText>
      </TwContainer>
      <TwContainer twc="flex-1 flex-col mb-6">
        <FlatList
          data={weeks}
          renderItem={({ item }) => <Week dates={item} />}
          horizontal
          inverted
          snapToInterval={width}
          decelerationRate="fast"
          onEndReached={handleEndReached}
          onEndReachedThreshold={0.5}
          extraData={weeks}
          getItemLayout={(_, index) => ({
            length: width,
            offset: width * index,
            index,
          })}
        />
      </TwContainer>
    </TwContainer>
  );
}
