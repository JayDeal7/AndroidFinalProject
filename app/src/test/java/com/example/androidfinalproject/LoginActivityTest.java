package com.example.androidfinalproject;

import android.widget.Button;
import android.widget.EditText;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class LoginActivityTest {

    @Test
    public void testActivityLaunches() {
        ActivityController<LoginActivity> controller =
                Robolectric.buildActivity(LoginActivity.class).create().start().resume();
        LoginActivity activity = controller.get();

        EditText email = activity.findViewById(R.id.editTextEmail);
        EditText password = activity.findViewById(R.id.editTextPassword);
        Button loginButton = activity.findViewById(R.id.loginButton);

        assertNotNull(email);
        assertNotNull(password);
        assertNotNull(loginButton);
    }
}