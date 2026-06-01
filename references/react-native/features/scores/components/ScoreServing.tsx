import { TwContainer } from "@/core/components/TwContainer";
import { TwText } from "@/core/components/TwText";
import React from "react";

type ScoreServingProps = {
  serving: 0 | 0.5 | 1;
  maxScore: -2 | -1 | 0 | 1 | 2;
  twc?: string;
};

export function ScoreServing({
  serving,
  maxScore,
  twc = "",
}: ScoreServingProps) {
  let bg: string;
  switch (maxScore) {
    case -2:
      bg = "bg-orange-400";
      break;
    case -1:
      bg = "bg-orange-300";
      break;
    case 0:
      bg = "bg-green-300";
      break;
    case 1:
      bg = "bg-lime-300";
      break;
    case 2:
      bg = "bg-lime-400";
      break;
  }

  return (
    <TwContainer
      twc={`h-10 w-9 flex-col justify-center align-center ml-1 rounded-xs ${bg} shadow-sm ${twc} ${!serving ? "opacity-60" : ""}`}
    >
      {serving > 0 && (
        <TwText twc={`font-bold text-center text-lg text-slate-900 mb-0`}>
          {serving * maxScore > 0 ? "+" : ""}
          {serving * maxScore}
        </TwText>
      )}
    </TwContainer>
  );
}
