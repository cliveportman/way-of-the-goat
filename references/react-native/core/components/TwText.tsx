import React from "react";
import { Text } from "react-native";

type TwTextProps = {
  children?: React.ReactNode;
  variant?:
    | "title"
    | "subtitle"
    | "heading"
    | "subheading"
    | "copy"
    | "small"
    | "large";
  twc?: string;
};

export function TwText({ children, variant = "copy", twc = "" }: TwTextProps) {
  let txt: string;
  const copy =
    "mb-3 text-base font-light tracking-tight text-slate-100 leading-snug";
  switch (variant) {
    case "title":
      txt =
        "mb-3 text-3xl font-bold tracking-tight text-slate-100 leading-none";
      break;
    case "subtitle":
      txt =
        "-mt-1.5 mb-3 text-lg font-medium tracking-tight text-slate-500 leading-none";
      break;
    case "heading":
      txt =
        "mb-3 text-xl font-bold tracking-tight text-slate-100 leading-none uppercase";
      break;
    case "subheading":
      txt =
        "mb-1.5 text-lg font-bold tracking-tight text-slate-100 leading-snug";
      break;
    case "copy":
      txt = copy;
      break;
    case "small":
      txt = "text-sm font-light tracking-tight text-slate-100 leading-snug";
      break;
    case "large":
      txt =
        "mb-3 text-lg font-light tracking-tight text-slate-100 leading-none";
      break;
    default:
      txt = copy;
  }

  return <Text tw={`text-left ${txt} ${twc}`}>{children}</Text>;
}
