import * as SQLite from "expo-sqlite";
import { SQLiteDatabase } from "expo-sqlite";
import type { Servings } from "@/core/types";
import databaseMigrations from "@/core/database-migrations";

const DATABASE_VERSION = 5;

export default {
  openDatabase,

  getMetaField,
  updateMetaField,

  getAllDays,
  getServingsByDate,
  getServingsBetweenDates,

  insertServings,
  updateServings,
  updateServingsCategory,
  deleteDuplicateDays,
  deleteEmptyDays,
};

function openDatabase() {
  const db = SQLite.openDatabaseSync("db.db");
  db.withTransactionSync(async () => {
    // @ts-expect-error
    const versionResult: [{ user_version: number }] = await db.getAllAsync(
      "PRAGMA user_version",
    );
    const currentVersion = versionResult[0]["user_version"] ?? 0;
    if (currentVersion < DATABASE_VERSION) {
      console.log(
        `Migrating database from version ${currentVersion} to version ${DATABASE_VERSION}`,
      );
      databaseMigrations(db, currentVersion, DATABASE_VERSION);
    }
  });
  db.withTransactionSync(() => {
    db.runSync(
      `create table if not exists servings (id integer primary key not null, date TEXT, veg int, fruit int, nuts int, wholegrains int, dairy int, leanproteins int, beverages int, refinedgrains int, sweets int, fattyproteins int, friedfoods int, alcohol int, other int );`,
    );
    db.runSync(
      `create table if not exists meta (id integer primary key not null, onboardedDate TEXT);`,
    );
  });
  return db;
}

async function getAllDays(db: SQLiteDatabase): Promise<Servings[]> {
  const results: Servings[] = await db.getAllAsync(
    `select * from servings order by date desc;`,
  );
  return results;
}

/**
 * Method for deleting duplicate days in the database.
 * It shouldn't be needed now we are preventing the insertion of duplicates.
 * @param db
 */
async function deleteDuplicateDays(db: SQLiteDatabase) {
  await db.runAsync(`
        DELETE FROM servings
        WHERE id NOT IN (
            SELECT MIN(id)
            FROM servings
            GROUP BY date
        );
    `);
  //console.log(`Duplicate rows deleted`);
}

async function deleteEmptyDays(db: SQLiteDatabase) {
  await db.runAsync(`
        DELETE FROM servings
        WHERE veg = 0
          AND fruit = 0
          AND nuts = 0
          AND wholegrains = 0
          AND dairy = 0
          AND leanproteins = 0
          AND beverages = 0
          AND refinedgrains = 0
          AND sweets = 0
          AND fattyproteins = 0
          AND friedfoods = 0
          AND alcohol = 0;
    `);
  //console.log(`Empty servings rows deleted`);
}

async function getMetaField(
  db: SQLiteDatabase,
  field: string,
): Promise<string | null> {
  const results: { [key: string]: string }[] = await db.getAllAsync(
    `select ${field} from meta;`,
  );
  if (results.length === 0) {
    return null;
  }
  return results[0][field];
}

/**
 * Method for updating the meta table in the database.
 */
async function updateMetaField(
  db: SQLiteDatabase,
  field: string,
  value: string | null,
) {
  await db.runAsync(
    `update meta
         set ${field}=?
         where id = 1`,
    [value],
  );
  //console.log(`Meta field updated`);
}

/**
 * Method for fetching the data for a single day from the database.
 * @param db
 * @param date YYYY-MM-DD
 */
async function getServingsByDate(
  db: SQLiteDatabase,
  date: string,
): Promise<Servings | null> {
  const results: Servings[] = await db.getAllAsync(
    `select *
                                                      from servings
                                                      where date =?;`,
    [date],
  );
  if (results.length === 0) {
    return null;
  }
  return results[0];
}

async function getServingsBetweenDates(
  db: SQLiteDatabase,
  startDate: string,
  endDate: string,
): Promise<Servings[]> {
  const results: Servings[] = await db.getAllAsync(
    `select *
                                                      from servings
                                                      where date >= ?
                                                      and date <= ?
                                                      order by date asc;`,
    [startDate, endDate],
  );
  return results;
}

async function insertServings(db: SQLiteDatabase, servings: Servings) {
  // Avoid inserting duplicate days
  const existingDay = await getServingsByDate(db, servings.date);
  if (existingDay) {
    console.log(
      `Servings for date ${servings.date} already exist. Skipping insertion.`,
    );
    return null;
  }
  const result = await db.runAsync(
    `insert into servings (date, veg, fruit, nuts, wholegrains, dairy, leanproteins, beverages, refinedgrains, sweets, fattyproteins, friedfoods, alcohol, other) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
    [
      servings.date,
      servings.veg,
      servings.fruit,
      servings.nuts,
      servings.wholegrains,
      servings.dairy,
      servings.leanproteins,
      servings.beverages,
      servings.refinedgrains,
      servings.sweets,
      servings.fattyproteins,
      servings.friedfoods,
      servings.alcohol,
      servings.other,
    ],
  );
  //console.log(`Servings added with the row ID:`, result.lastInsertRowId);
  return result.lastInsertRowId;
}

/**
 * Method for updating the data for a single food category on a single day in the database.
 * @param db
 * @param id
 * @param category
 * @param numberOfServings
 */
async function updateServingsCategory(
  db: SQLiteDatabase,
  id: number,
  category: string,
  numberOfServings: number,
) {
  await db.runAsync(`update servings set ${category}=? where id=${id}`, [
    numberOfServings,
  ]);
  const results: Servings[] = await db.getAllAsync<Servings>(
    `select * from servings where id = ?`,
    [id],
  );
  return results[0];
}

async function updateServings(
  db: SQLiteDatabase,
  id: number,
  servings: Servings,
) {
  await db.runAsync(
    `update servings set date=?, veg=?, fruit=?, nuts=?, wholegrains=?, dairy=?, leanproteins=?, beverages=?, refinedgrains=?, sweets=?, fattyproteins=?, friedfoods=?, alcohol=?, other=? where id=${id}`,
    [
      servings.date,
      servings.veg,
      servings.fruit,
      servings.nuts,
      servings.wholegrains,
      servings.dairy,
      servings.leanproteins,
      servings.beverages,
      servings.refinedgrains,
      servings.sweets,
      servings.fattyproteins,
      servings.friedfoods,
      servings.alcohol,
      servings.other,
    ],
  );
  //console.log(`Servings updated`);
}
