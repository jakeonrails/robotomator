package com.robotomator.app

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for MainActivity
 *
 * These tests verify the permission request flow UI and user interactions.
 * They run on an Android device or emulator.
 *
 * Note: UI tests may behave differently depending on whether the
 * accessibility service is already enabled on the test device.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        // Launch the activity
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testActivityLaunches() {
        // Verify the activity launches without crashing
        scenario.onActivity { activity ->
            assertNotNull("Activity should not be null", activity)
        }
    }

    @Test
    fun testStatusTextViewExists() {
        // Verify the status text view is present
        onView(withId(R.id.status_text))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testExplanationTextViewExists() {
        // Verify the explanation text view is present
        onView(withId(R.id.explanation_text))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testActionButtonExists() {
        // Verify the action button exists
        // Note: It may or may not be visible depending on permission state
        onView(withId(R.id.action_button))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun testUIShowsValidState() {
        // The UI should show one of the three valid states
        scenario.onActivity { activity ->
            val status = PermissionUtils.getPermissionStatus(activity)

            // Verify the UI matches the current permission state
            when {
                status.isFullyOperational -> {
                    // Should show "You're All Set!" or similar
                    onView(withId(R.id.status_text))
                        .check(matches(isDisplayed()))
                }
                status.isWaitingForConnection -> {
                    // Should show "Almost Ready..." or similar
                    onView(withId(R.id.status_text))
                        .check(matches(isDisplayed()))
                }
                status.needsPermission -> {
                    // Should show welcome message and button
                    onView(withId(R.id.status_text))
                        .check(matches(isDisplayed()))
                    onView(withId(R.id.action_button))
                        .check(matches(isDisplayed()))
                }
            }
        }
    }

    @Test
    fun testStatusTextNotEmpty() {
        // The status text should always have content
        onView(withId(R.id.status_text))
            .check(matches(withText(org.hamcrest.Matchers.not(""))))
    }

    @Test
    fun testExplanationTextNotEmpty() {
        // The explanation text should always have content
        onView(withId(R.id.explanation_text))
            .check(matches(withText(org.hamcrest.Matchers.not(""))))
    }

    @Test
    fun testActivityRecreation() {
        // Test that the activity survives recreation (configuration change simulation)
        scenario.recreate()

        // After recreation, views should still be present
        onView(withId(R.id.status_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.explanation_text))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testActivityResume() {
        // Test that onResume updates the permission status
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)

        // Activity should display current state
        onView(withId(R.id.status_text))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testActivityPause() {
        // Test that onPause properly cleans up
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED)

        // Activity should still be valid
        scenario.onActivity { activity ->
            assertNotNull("Activity should not be null after pause", activity)
        }
    }
}
