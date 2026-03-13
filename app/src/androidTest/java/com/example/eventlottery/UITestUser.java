package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UITestUser{

    @Rule
    public ActivityScenarioRule<WelcomePageActivity> scenario = new ActivityScenarioRule<>(WelcomePageActivity.class);

    //welcome page check
    @Test
    public void testtheUserButtonDisplayed() {
        onView(withId(R.id.userbutton)).check(matches(isDisplayed()));
    }
    @Test
    public void testtheOrganizerButtonDisplayed() {
        onView(withId(R.id.organizerbutton)).check(matches(isDisplayed()));
    }
    @Test
    public void testtheAdminButtonDisplayed() {
        onView(withId(R.id.adminbutton)).check(matches(isDisplayed()));
    }

    //start navigating to other pages
    @Test
    public void navigationtoAdmin() {
        onView(withId(R.id.adminbutton)).perform(click());
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));

        onView(withId(R.id.admin_event_filter_button)).check(matches(isDisplayed()));

        onView(withId(R.id.admin_event_search_inputbar)).check(matches(isDisplayed()));

        onView(withId(R.id.search_button)).check(matches(isDisplayed()));

        onView(withId(R.id.Events_history)).check(matches(isDisplayed()));

        //click on back
        onView(withId(R.id.back_button)).perform(click());
        onView(withId(R.id.adminbutton)).check(matches(isDisplayed()));
        onView(withId(R.id.organizerbutton)).check(matches(isDisplayed()));
        onView(withId(R.id.userbutton)).check(matches(isDisplayed()));
    }

    @Test
    public void navigationtoOrganizer() {
        onView(withId(R.id.organizerbutton)).perform(click());


    }

    @Test
    public void navigationtoUser() {
        onView(withId(R.id.userbutton)).perform(click());



    }

}
