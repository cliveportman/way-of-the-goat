import { SQLiteDatabase } from "expo-sqlite";

export default function databaseMigrations(
  db: SQLiteDatabase,
  currentVersion: number,
  newVersion: number,
) {
  db.withTransactionSync(() => {
    /**
     * New tables.
     */
    if (currentVersion < 1) {
      db.runSync(
        `create table if not exists servings (id integer primary key not null, date TEXT, veg int, fruit int, nuts int, wholegrains int, dairy int, leanproteins int, beverages int, refinedgrains int, sweets int, fattyproteins int, friedfoods int, other int);`,
      );
    }
    /**
     * Version 2 updated the type for the date column.
     */
    if (currentVersion < 2) {
      // Create a new table with the desired schema
      db.runSync(
        `create table if not exists servings_new (id integer primary key not null, date TEXT, veg int, fruit int, nuts int, wholegrains int, dairy int, leanproteins int, beverages int, refinedgrains int, sweets int, fattyproteins int, friedfoods int, other int);`,
      );
      // Copy data from the old table to the new table
      db.runSync(
        `insert into servings_new (id, date, veg, fruit, nuts, wholegrains, dairy, leanproteins, beverages, refinedgrains, sweets, fattyproteins, friedfoods, other) select id, date, veg, fruit, nuts, wholegrains, dairy, leanproteins, beverages, refinedgrains, sweets, fattyproteins, friedfoods, other from servings;`,
      );
      // Drop the old table then rename the new one back to the old name
      db.runSync(`drop table servings;`);
      db.runSync(`alter table servings_new rename to servings;`);
    }
    /**
     * Version 3 renamed the "scores" table to "servings".
     */
    if (currentVersion < 3) {
      // Create a new table with the desired schema
      db.runSync(
        `create table if not exists servings_new (id integer primary key not null, date TEXT, veg int, fruit int, nuts int, wholegrains int, dairy int, leanproteins int, beverages int, refinedgrains int, sweets int, fattyproteins int, friedfoods int, other int);`,
      );
      // Copy data from the old table to the new table
      db.runSync(
        `insert into servings_new (id, date, veg, fruit, nuts, wholegrains, dairy, leanproteins, beverages, refinedgrains, sweets, fattyproteins, friedfoods, other) select id, date, veg, fruit, nuts, wholegrains, dairy, leanproteins, beverages, refinedgrains, sweets, fattyproteins, friedfoods, other from servings;`,
      );
      // Drop the old table then rename the new one back to the old name
      db.runSync(`drop table servings;`);
      db.runSync(`alter table servings_new rename to servings;`);
    }
    /**
     * Version 4 adds an "alcohol" column
     */
    if (currentVersion < 4) {
      // Create a new table with the desired schema
      db.runSync(
        `create table if not exists servings_new (id integer primary key not null, date TEXT, veg int, fruit int, nuts int, wholegrains int, dairy int, leanproteins int, beverages int, refinedgrains int, sweets int, fattyproteins int, friedfoods int, alcohol int, other int);`,
      );
      // Copy data from the old table to the new table
      db.runSync(
        `insert into servings_new (id, date, veg, fruit, nuts, wholegrains, dairy, leanproteins, beverages, refinedgrains, sweets, fattyproteins, friedfoods, other) select id, date, veg, fruit, nuts, wholegrains, dairy, leanproteins, beverages, refinedgrains, sweets, fattyproteins, friedfoods, other from servings;`,
      );
      // Drop the old table then rename the new one back to the old name
      db.runSync(`drop table servings;`);
      db.runSync(`alter table servings_new rename to servings;`);
    }
    if (currentVersion < 5) {
      // Create a new table for storing meta data, e.g. onboarded date
      db.runSync(
        `create table if not exists meta (id integer primary key not null, onboardedDate TEXT);`,
      );
      // Add a record to the meta table
      db.runSync(`insert into meta (id, onboardedDate) values (1, null);`);
    }

    // Update the user_version pragma to the new version
    db.runSync(`PRAGMA user_version = ${newVersion}`);
    console.log(`Database migrated to version ${newVersion}`);
  });
}
