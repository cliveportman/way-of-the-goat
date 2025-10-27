export type Servings = {
  id?: number;
  date: string;
  veg: number;
  fruit: number;
  nuts: number;
  wholegrains: number;
  dairy: number;
  leanproteins: number;
  beverages: number;
  refinedgrains: number;
  sweets: number;
  fattyproteins: number;
  friedfoods: number;
  alcohol: number;
  other: number;
};

export type SingleServingScore = -2 | -1 | 0 | 1 | 2;
export type PossibleSingleServingScores = [
  SingleServingScore,
  SingleServingScore,
  SingleServingScore,
  SingleServingScore,
  SingleServingScore,
  SingleServingScore,
];

/** Used for displaying in the UI, output type can vary */
export type DayTotalsForDisplay = {
  healthy: string | "---"; // Either "---" or "+n" where n is a number (+ sign)
  unhealthy: number | "---"; // Either "---" or n where n is a number (sign is part of the number)
  total: string | number | "---"; // Either "---" or "+n" where n is a number (+ sign) or n where n is a number (sign is part of the number)
  portions: number;
};
/** Used for maths, output type is always a number */
export type DayTotalsForMaths = {
  healthy: number;
  unhealthy: number;
  total: number;
  portions: number;
};

/**
 * Stolen from https://catchts.com/dates
 */
type PrependNextNum<A extends unknown[]> = A["length"] extends infer T
  ? ((t: T, ...a: A) => void) extends (...x: infer X) => void
    ? X
    : never
  : never;
type EnumerateInternal<A extends unknown[], N extends number> = {
  0: A;
  1: EnumerateInternal<PrependNextNum<A>, N>;
}[N extends A["length"] ? 0 : 1];
type Enumerate<N extends number> =
  EnumerateInternal<[], N> extends (infer E)[] ? E : never;

type NumberString<T extends number> = `${T}`;

type Year =
  `${NumberString<number>}${NumberString<number>}${NumberString<number>}${NumberString<number>}`;

type ZeroRequired = 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9;

type AddZero<T extends number> = T extends ZeroRequired ? `${0}${T}` : T;

type MakeString<T extends number | `${number}`> = `${T}`;

type Month = MakeString<AddZero<Exclude<Enumerate<13>, 0>>>;

type Day = MakeString<AddZero<Exclude<Enumerate<32>, 0>>>;

export type DateString = `${Year}-${Month}-${Day}`;
