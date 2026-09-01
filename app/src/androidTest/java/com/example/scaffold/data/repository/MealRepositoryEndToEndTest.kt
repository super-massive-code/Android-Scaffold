package com.example.scaffold.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.scaffold.data.local.AppDatabase
import com.example.scaffold.data.remote.MealApi
import com.example.scaffold.model.MealCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Drives [MealRepositoryImpl] through its real collaborators end to end: a real Retrofit
 * client with the app's actual kotlinx.serialization config, a real in-memory Room database,
 * and [MealRepository]'s own public contract. The only thing swapped out is the socket
 * themealdb.com sits behind — replaced with a local [MockWebServer] serving canned copies of
 * its real responses — so no interface of ours is faked or mocked anywhere in this test.
 */
@RunWith(AndroidJUnit4::class)
class MealRepositoryEndToEndTest {
    private lateinit var server: MockWebServer
    private lateinit var database: AppDatabase
    private lateinit var repository: MealRepository

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }

        val json =
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()

        database =
            Room
                .inMemoryDatabaseBuilder(
                    InstrumentationRegistry.getInstrumentation().targetContext,
                    AppDatabase::class.java,
                ).build()

        repository = MealRepositoryImpl(retrofit.create(MealApi::class.java), database.mealDao())
    }

    @After
    fun tearDown() {
        database.close()
        server.close()
    }

    @Test
    fun refresh_parsesRealFilterJsonAndPersistsThroughRoom() =
        runTest {
            // A trimmed but real capture of themealdb.com/api/json/v1/1/filter.php?c=Chicken,
            // including the strArea/strCountry fields MealSummaryDto doesn't declare.
            server.enqueue(
                MockResponse
                    .Builder()
                    .addHeader("Content-Type", "application/json")
                    .body(
                        """
                        {"meals":[
                            {
                                "strMeal": "Apam balik",
                                "strMealThumb": "https://www.themealdb.com/images/media/meals/adxcbq1619787919.jpg",
                                "idMeal": "53049",
                                "strArea": "Malaysian",
                                "strCountry": "Malaysia"
                            }
                        ]}
                        """.trimIndent(),
                    ).build(),
            )

            repository.refreshByCategory(MealCategory.Chicken)

            val request = server.takeRequest()
            assertEquals("/filter.php", request.url.encodedPath)
            assertEquals("Chicken", request.url.queryParameter("c"))

            val meals = repository.observeMeals().first()
            assertEquals(1, meals.size)
            assertEquals("53049", meals.first().id)
            assertEquals("Apam balik", meals.first().title)
            assertEquals(
                "https://www.themealdb.com/images/media/meals/adxcbq1619787919.jpg",
                meals.first().thumbnailUrl,
            )
        }

    @Test
    fun refreshWithId_parsesRealLookupJsonAndMergesInstructions() =
        runTest {
            // A trimmed but real capture of themealdb.com/api/json/v1/1/lookup.php?i=52772,
            // including several ingredient/tag fields MealDetailDto doesn't declare.
            server.enqueue(
                MockResponse
                    .Builder()
                    .addHeader("Content-Type", "application/json")
                    .body(
                        """
                        {"meals":[
                            {
                                "idMeal": "52772",
                                "strMeal": "Teriyaki Chicken Casserole",
                                "strMealThumb": "https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg",
                                "strCategory": "Chicken",
                                "strArea": "Japanese",
                                "strInstructions": "Preheat oven to 350F. Combine soy sauce and sugar.",
                                "strIngredient1": "soy sauce",
                                "strTags": null,
                                "strYoutube": "https://www.youtube.com/watch?v=4aZr5hZXP_s"
                            }
                        ]}
                        """.trimIndent(),
                    ).build(),
            )

            repository.refresh("52772")

            val request = server.takeRequest()
            assertEquals("/lookup.php", request.url.encodedPath)
            assertEquals("52772", request.url.queryParameter("i"))

            val meal = repository.observeMeal("52772").first()
            assertEquals("Teriyaki Chicken Casserole", meal?.title)
            assertEquals("Preheat oven to 350F. Combine soy sauce and sugar.", meal?.instructions)
        }

    @Test
    fun refreshWithId_whenTheRealApiHasNoMatch_doesNotCacheAnything() =
        runTest {
            // TheMealDB's real response for an id with no match, e.g. lookup.php?i=999999.
            server.enqueue(
                MockResponse
                    .Builder()
                    .addHeader("Content-Type", "application/json")
                    .body("""{"meals":null}""")
                    .build(),
            )

            repository.refresh("999999")

            assertNull(repository.observeMeal("999999").first())
        }
}
