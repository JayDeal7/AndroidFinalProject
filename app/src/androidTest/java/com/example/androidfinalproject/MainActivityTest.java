package com.example.androidfinalproject;

import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import static org.junit.Assert.*;

public class MainActivityTest {

    @Test
    public void mainActivity_Launches() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.mainLayout));
                assertNotNull(activity.findViewById(R.id.welcomeText));
                assertNotNull(activity.findViewById(R.id.featureGrid));
                assertNotNull(activity.findViewById(R.id.mainToolbar));
            });
        }
    }
}