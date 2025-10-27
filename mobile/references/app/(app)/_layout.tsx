import React from "react";
import { Tabs } from "expo-router";
import TailwindColors from "tailwindcss/colors";
import { MaterialIcons } from "@expo/vector-icons";

export default function TabLayout() {
  return (
    <Tabs
      initialRouteName={"scores/index"}
      backBehavior={"history"}
      screenOptions={{
        tabBarActiveTintColor: TailwindColors.slate[100],
        tabBarActiveBackgroundColor: TailwindColors.slate[800],
        tabBarInactiveTintColor: TailwindColors.slate[500],
        tabBarStyle: {
          height: 64,
          paddingTop: 0,
          backgroundColor: TailwindColors.slate[900],
          borderTopWidth: 0,
        },
        headerShown: false,
      }}
    >
      <Tabs.Screen
        name="progress/index"
        options={{
          title: "Progress",
          tabBarIcon: ({ color }) => (
            <MaterialIcons name={"show-chart"} size={28} color={color} />
          ),
          tabBarIconStyle: { marginTop: 6 },
          tabBarLabelStyle: { fontSize: 14, paddingBottom: 6 },
        }}
      />
      <Tabs.Screen
        name="scores/index"
        options={{
          title: "Scores",
          tabBarIcon: ({ color }) => (
            <MaterialIcons name={"apps"} size={36} color={color} />
          ),
          tabBarIconStyle: { marginTop: 6 },
          tabBarLabelStyle: { fontSize: 15, paddingBottom: 6 },
        }}
      />
      <Tabs.Screen
        name="scores/[day]"
        options={{
          href: null,
        }}
      />
      <Tabs.Screen
        name="user-guide/index"
        options={{
          title: "User guide",
          tabBarIcon: ({ color }) => (
            <MaterialIcons name={"menu-book"} size={28} color={color} />
          ),
          tabBarIconStyle: { marginTop: 6 },
          tabBarLabelStyle: { fontSize: 14, paddingBottom: 6 },
        }}
      />
    </Tabs>
  );
}
