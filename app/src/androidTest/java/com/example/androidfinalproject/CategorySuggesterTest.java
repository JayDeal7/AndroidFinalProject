package com.example.androidfinalproject;

import org.junit.Test;
import static org.junit.Assert.*;

public class CategorySuggesterTest {

    @Test
    public void testSuggestFood() {
        assertEquals("Food", CategorySuggester.suggest("Walmart", "Some text"));
    }

    @Test
    public void testSuggestTravel() {
        assertEquals("Travel", CategorySuggester.suggest("Uber", "Ride details"));
    }

    @Test
    public void testSuggestUncategorized() {
        assertEquals("Uncategorized", CategorySuggester.suggest("Unknown", "No match"));
    }

    @Test
    public void testSuggestHealth() {
        assertEquals("Health", CategorySuggester.suggest("Shoppers", "Pharmacy"));
    }

    @Test
    public void testSuggestElectronics() {
        assertEquals("Electronics", CategorySuggester.suggest("Best Buy", "Electronics purchase"));
    }

    @Test
    public void testSuggestClothing() {
        assertEquals("Clothing", CategorySuggester.suggest("H&M", "Clothing"));
    }

    @Test
    public void testSuggestGames() {
        assertEquals("Games", CategorySuggester.suggest("Steam", "Game purchase"));
    }

    @Test
    public void testSuggestNullVendor() {
        assertEquals("Uncategorized", CategorySuggester.suggest(null, null));
    }

    @Test
    public void testSuggestEmptyStrings() {
        assertEquals("Uncategorized", CategorySuggester.suggest("", ""));
    }

    @Test
    public void testSuggestMixedCase() {
        assertEquals("Food", CategorySuggester.suggest("wAlMaRt", "gRoCeRy"));
    }
}