import { Text, View } from "react-native";
import React from "react";

type ScoreLabelProps = {
  text: string;
  twc?: string;
};

export function ScoreLabel({ text, twc = "" }: ScoreLabelProps) {
  return (
    <View
      tw={`grow flex flex-col justify-center items-start pl-1.5 h-10 rounded-xs bg-slate-800 shadow-sm ${twc}`}
    >
      <Text tw={`font-regular text-left text-base text-white`}>{text}</Text>
    </View>
  );
}
