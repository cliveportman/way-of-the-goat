import React from "react";
import { View, ViewProps } from "react-native";

type TwContainerProps = ViewProps & {
  children?: React.ReactNode;
  twc?: string;
};

export function TwContainer({
  children,
  twc = "",
  ...props
}: TwContainerProps) {
  return (
    <View tw={twc} {...props}>
      {children}
    </View>
  );
}
