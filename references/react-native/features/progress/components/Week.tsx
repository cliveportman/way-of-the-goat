import { DateString, Servings } from "@/core/types";
import React, { useEffect } from "react";
import { TwContainer } from "@/core/components/TwContainer";
import dayjs from "dayjs";

import { getTotalScoresForMaths } from "@/core/helpers";
import { Chart } from "@/features/progress/components/Chart";
import { type BarData } from "@/features/progress/components/Bar";
import { Dimensions } from "react-native";
import database from "@/core/database";
import { useDatabase } from "@/core/hooks";
import { TwText } from "@/core/components/TwText";
import { List } from "@/features/progress/components/List";

type ProgressByWeekChartProps = {
  dates: DateString[];
};

export function Week({ dates }: ProgressByWeekChartProps) {
  const db = useDatabase();
  const [chartData, setChartData] = React.useState<BarData[]>([]);
  const [listData, setListData] = React.useState<Servings[]>([]);
  const { width } = Dimensions.get("window");

  useEffect(() => {
    async function fetchData() {
      if (!db) return;
      const data = await database.getServingsBetweenDates(
        db,
        dates[0],
        dates[6],
      );
      setChartData(
        dates.map((date) => {
          const day = data.find((item) => item.date === date);
          return {
            value: day ? getTotalScoresForMaths(day).total : 0,
            label: dayjs(date).format("ddd").charAt(0),
          };
        }),
      );
      setListData(data);
    }

    fetchData();
  }, [db, dates]);

  return (
    <TwContainer style={{ width: width }} twc={"px-3"}>
      <TwText variant={"subtitle"} twc={"text-left mb-3"}>
        {dayjs(dates[0]).format("DD MMM")} -{" "}
        {dayjs(dates[6]).format("DD MMM YYYY")}
      </TwText>
      {listData.length > 0 && (
        <>
          <Chart
            data={chartData}
            height={200}
            labelHeights={20}
            maxValue={32}
            minValue={0}
            horizontalLines={[30, 20, 10, 0]}
          />
          <List data={listData} />
        </>
      )}
      {listData.length === 0 && (
        <TwContainer twc={"flex-1 flex-col justify-center"}>
          <TwText twc={"text-center"}>No data available</TwText>
        </TwContainer>
      )}
    </TwContainer>
  );
}
