package com.example.androidfinalproject;

import org.junit.Test;
import static org.junit.Assert.*;

public class CategorySuggesterTest {

    @Test
    public void testSuggestFood() {
        String result = CategorySuggester.suggest("Walmart", "Some text");
        assertEquals("Food", result);
    }

    @Test
    public void testSuggestTravel() {
        String result = CategorySuggester.suggest("Uber", "Ride details");
        assertEquals("Travel", result);
    }

    @Test
    public void testSuggestUncategorized() {
        String result = CategorySuggester.suggest("Unknown", "No match");
        assertEquals("Uncategorized", result);
    }
}