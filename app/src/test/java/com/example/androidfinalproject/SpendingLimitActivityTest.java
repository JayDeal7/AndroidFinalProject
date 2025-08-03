package com.example.androidfinalproject;

import android.widget.EditText;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class SpendingLimitActivityTest {

    @Test
    public void testActivityLaunches() {
        ActivityController<SpendingLimitActivity> controller =
                Robolectric.buildActivity(SpendingLimitActivity.class).create().start().resume();
        SpendingLimitActivity activity = controller.get();

        EditText editLimit = activity.findViewById(R.id.editMonthlyLimit);
        assertNotNull(editLimit);
    }
}