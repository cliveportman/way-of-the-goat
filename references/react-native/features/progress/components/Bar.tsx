import React from "react";
import { TwContainer } from "@/core/components/TwContainer";
import { TwText } from "@/core/components/TwText";

type BarProps = {
  data: BarData;
  maxValue: number;
  minValue: number;
  width: number;
  maxHeight: number;
};
export type BarData = {
  value: number;
  label: string;
};

function zeroValueBar(
  data: BarData,
  maxHeight: number,
  maxValue: number,
  minValue: number,
) {
  return (
    <TwContainer twc={"relative flex-col"} style={{ height: maxHeight }}>
      <TwContainer
        twc={"absolute flex-col w-8 bg-slate-200"}
        style={{
          top: (maxValue / (maxValue - minValue)) * maxHeight,
          height: 1,
        }}
      />
      <TwContainer twc={"absolute w-full"} style={{ top: maxHeight + 7 }}>
        <TwText
          variant={"small"}
          twc={"text-center text-slate-400 font-semibold"}
        >
          {data.label}
        </TwText>
      </TwContainer>
    </TwContainer>
  );
}

function negativeValueBar(
  data: BarData,
  maxHeight: number,
  maxValue: number,
  minValue: number,
) {
  return (
    <TwContainer twc={"relative flex-col"} style={{ height: maxHeight }}>
      <TwContainer
        twc={"absolute flex-col w-8 bg-red-400"}
        style={{
          top: (maxValue / (maxValue - minValue)) * maxHeight,
          height: 1,
        }}
      />
      <TwContainer
        twc={"absolute w-full"}
        style={{
          top: ((maxValue - 0) / (maxValue - minValue)) * maxHeight - 24,
        }}
      >
        <TwText
          variant={"small"}
          twc={`text-center ${data.value < 20 ? "text-red-400" : "text-lime-400"}`}
        >
          {data.value}
        </TwText>
      </TwContainer>
      <TwContainer twc={"absolute w-full"} style={{ top: maxHeight + 7 }}>
        <TwText
          variant={"small"}
          twc={"text-center text-slate-400 font-semibold"}
        >
          {data.label}
        </TwText>
      </TwContainer>
    </TwContainer>
  );
}

function positiveValueBar(
  data: BarData,
  maxHeight: number,
  maxValue: number,
  minValue: number,
) {
  return (
    <TwContainer twc={"relative flex-col"} style={{ height: maxHeight }}>
      <TwContainer
        twc={`absolute flex-col w-8 ${data.value < 20 ? "bg-slate-200" : "bg-lime-400"}`}
        style={{
          top: ((maxValue - data.value) / (maxValue - minValue)) * maxHeight,
          height: (data.value / (maxValue - minValue)) * maxHeight,
        }}
      />
      <TwContainer
        twc={"absolute w-full"}
        style={{
          top:
            ((maxValue - data.value) / (maxValue - minValue)) * maxHeight - 24,
        }}
      >
        <TwText
          variant={"small"}
          twc={`text-center ${data.value < 20 ? "text-slate-100" : "text-lime-400"}`}
        >
          +{data.value}
        </TwText>
      </TwContainer>
      <TwContainer twc={"absolute w-full"} style={{ top: maxHeight + 7 }}>
        <TwText
          variant={"small"}
          twc={"text-center text-slate-400 font-semibold"}
        >
          {data.label}
        </TwText>
      </TwContainer>
    </TwContainer>
  );
}

function getBar(
  data: BarData,
  maxValue: number,
  minValue: number,
  maxHeight: number,
) {
  if (data.value === 0)
    return zeroValueBar(data, maxHeight, maxValue, minValue);
  if (data.value < 0)
    return negativeValueBar(data, maxHeight, maxValue, minValue);
  if (data.value > 0)
    return positiveValueBar(data, maxHeight, maxValue, minValue);
}

export function Bar({
  data,
  width = 16,
  maxHeight,
  maxValue,
  minValue,
}: BarProps) {
  return (
    <TwContainer
      twc={"relative flex-col mx-1.5"}
      style={{ height: maxHeight, width: width }}
    >
      {getBar(data, maxValue, minValue, maxHeight)}
    </TwContainer>
  );
}
