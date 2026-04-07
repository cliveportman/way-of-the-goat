import React from "react";
import { TwContainer } from "@/core/components/TwContainer";
import { Bar, type BarData } from "@/features/progress/components/Bar";
import { Dimensions } from "react-native";

type ChartProps = {
  data: BarData[];
  maxValue: number;
  minValue: number;
  height: number; // Desired height for the chart's bars
  labelHeights: number; // Space to add on for the bottom labels
  horizontalLines: number[]; // Values you'd like horizontal lines
};

export function Chart({
  data,
  maxValue,
  minValue,
  height = 200,
  labelHeights = 20,
  horizontalLines = [0],
}: ChartProps) {
  const { width } = Dimensions.get("window");

  return (
    <TwContainer
      twc={"relative mb-6"}
      style={{ height: height + labelHeights }}
    >
      <TwContainer twc={"absolute left-0 top-0 right-0 bottom-0 mt-6"}>
        {horizontalLines.map((v) => (
          <TwContainer
            key={v}
            twc={"absolute w-full bg-slate-900"}
            style={{
              top: ((maxValue - v) / (maxValue - minValue)) * height,
              height: 1,
            }}
          />
        ))}
      </TwContainer>
      <TwContainer
        twc={"flex-row justify-center pt-6"}
        style={{ width: width - 24 }}
      >
        {data.map((bar, index) => {
          return (
            <Bar
              key={index}
              data={bar}
              width={32}
              maxHeight={height}
              maxValue={maxValue}
              minValue={minValue}
            />
          );
        })}
      </TwContainer>
    </TwContainer>
  );
}
