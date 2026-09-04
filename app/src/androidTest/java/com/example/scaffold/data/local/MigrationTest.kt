package com.example.scaffold.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration-test"

/**
 * The template every future migration test extends. [AppDatabase] has no migration history yet,
 * so this only asserts that the committed `app/schemas/.../1.json` describes the database the
 * compiled code actually opens — which is the thing a `MIGRATION_1_2` would be validated against.
 *
 * When a schema change lands: bump the version, commit the new `<n>.json`, add `MIGRATION_x_y`
 * to `data/local/Migrations.kt`, and add a test here that creates the database at the old
 * version, writes a row, then calls
 * `helper.runMigrationsAndValidate(TEST_DB, <new version>, true, MIGRATION_x_y)` and asserts the
 * row survived.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java,
        )

    @Test
    fun version1MatchesTheCommittedSchema() {
        helper.createDatabase(TEST_DB, 1).close()

        // Room validates the identity hash of the file it opens against the schema compiled into
        // AppDatabase, so this throws if the two have drifted apart.
        val database =
            Room
                .databaseBuilder(
                    InstrumentationRegistry.getInstrumentation().targetContext,
                    AppDatabase::class.java,
                    TEST_DB,
                ).build()
        database.openHelper.writableDatabase
        database.close()
    }
}
