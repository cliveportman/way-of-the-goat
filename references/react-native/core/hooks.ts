import { useState, useEffect } from "react";
import { SQLiteDatabase } from "expo-sqlite";
import database from "@/core/database";

export function useDatabase(): SQLiteDatabase | null {
  const [db, setDb] = useState<SQLiteDatabase | null>(null);

  useEffect(() => {
    setDb(database.openDatabase());
  }, []);

  return db;
}
