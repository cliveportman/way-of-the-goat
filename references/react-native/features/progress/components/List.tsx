import { Servings } from "@/core/types";
import React from "react";
import { TwContainer } from "@/core/components/TwContainer";
import { FlatList } from "react-native";
import { TwText } from "@/core/components/TwText";
import { DaySummary } from "@/features/progress/components/DaySummary";

type ListProps = {
  data: Servings[];
};

export function List({ data }: ListProps) {
  return (
    <TwContainer twc={"flex-1 mt-12 mx-1"}>
      <TwContainer twc={"flex-row justify-between items-start mx-3"}>
        <TwText twc={"w-28"}></TwText>
        <TwText variant={"small"} twc={`w-16 text-right text-slate-300`}>
          Servings
        </TwText>
        <TwText variant={"small"} twc={`w-10 text-right text-slate-300`}>
          For
        </TwText>
        <TwText variant={"small"} twc={`w-16 text-right text-slate-300`}>
          Against
        </TwText>
        <TwText variant={"small"} twc={`w-10 text-right text-slate-300`}>
          Score
        </TwText>
      </TwContainer>
      <FlatList
        data={data}
        renderItem={({ item }) => <DaySummary data={item} />}
        keyExtractor={(item) => String(item.id)}
      />
    </TwContainer>
  );
}
