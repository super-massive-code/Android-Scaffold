package com.example.scaffold.ui.feature.contactlist

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.scaffold.model.Contact
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun swipingARowInvokesOnDeleteContact() {
        var deleted: Contact? = null

        composeTestRule.setContent {
            ContactListScreen(
                uiState = ContactListUiState.Content(contacts = listOf(Ada)),
                onAddContactClick = {},
                onContactClick = {},
                onDeleteContact = { deleted = it },
                onUndoDelete = {},
                onUndoDismissed = {},
            )
        }

        composeTestRule.onNodeWithText("Ada Lovelace").performTouchInput { swipeLeft() }
        composeTestRule.waitForIdle()

        assertEquals(Ada, deleted)
    }

    @Test
    fun tappingUndoOnTheSnackbarInvokesOnUndoDelete() {
        var undone = false

        composeTestRule.setContent {
            ContactListScreen(
                uiState = ContactListUiState.Content(contacts = emptyList(), recentlyDeleted = Ada),
                onAddContactClick = {},
                onContactClick = {},
                onDeleteContact = {},
                onUndoDelete = { undone = true },
                onUndoDismissed = {},
            )
        }

        composeTestRule.onNodeWithText("Undo").performClick()
        composeTestRule.waitForIdle()

        assertTrue(undone)
    }

    @Test
    fun tappingARowInvokesOnContactClickWithItsId() {
        var clickedId: Long? = null

        composeTestRule.setContent {
            ContactListScreen(
                uiState = ContactListUiState.Content(contacts = listOf(Ada)),
                onAddContactClick = {},
                onContactClick = { clickedId = it },
                onDeleteContact = {},
                onUndoDelete = {},
                onUndoDismissed = {},
            )
        }

        composeTestRule.onNodeWithText("Ada Lovelace").performClick()

        assertEquals(Ada.id, clickedId)
    }
}

private val Ada =
    Contact(
        id = 1,
        firstName = "Ada",
        lastName = "Lovelace",
        addressLine1 = "12 Curzon Street",
        addressLine2 = null,
        city = "London",
        postcode = "W1J 5HN",
    )
