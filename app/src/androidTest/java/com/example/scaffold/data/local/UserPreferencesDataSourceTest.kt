package com.example.scaffold.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.scaffold.model.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * A real DataStore on a real filesystem, the key-value counterpart to
 * [com.example.scaffold.data.repository.MealRepositoryEndToEndTest]'s real Room database: what it
 * proves is that a value written under our key survives a round trip and decodes back to the same
 * [ThemeMode], which a fake data source can't.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserPreferencesDataSourceTest {
    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var dataSource: UserPreferencesDataSource

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        file = File(context.filesDir, "datastore/test-${System.nanoTime()}.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(produceFile = { file })
        dataSource = UserPreferencesDataSource(dataStore)
    }

    @After
    fun tearDown() {
        file.delete()
    }

    @Test
    fun themeMode_defaultsToSystemBeforeAnythingIsWritten() =
        runTest(UnconfinedTestDispatcher()) {
            assertEquals(ThemeMode.System, dataSource.observeThemeMode().first())
        }

    @Test
    fun themeMode_survivesAWriteAndReadBack() =
        runTest(UnconfinedTestDispatcher()) {
            dataSource.setThemeMode(ThemeMode.Dark)

            assertEquals(ThemeMode.Dark, dataSource.observeThemeMode().first())

            dataSource.setThemeMode(ThemeMode.Light)

            assertEquals(ThemeMode.Light, dataSource.observeThemeMode().first())
        }
}
