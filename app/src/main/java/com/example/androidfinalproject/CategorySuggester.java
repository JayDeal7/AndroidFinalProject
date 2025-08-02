package com.example.androidfinalproject;

public class CategorySuggester {
    public static String suggest(String vendor, String fullText) {
        String s = ((vendor == null ? "" : vendor) + " " + (fullText == null ? "" : fullText)).toLowerCase();

        if (s.contains("freshco") || s.contains("walmart") || s.contains("sobeys")
                || s.contains("costco") || s.contains("no frills") || s.contains("grocery"))
            return "Food";
        if (s.contains("uber") || s.contains("lyft") || s.contains("metro") || s.contains("bus")
                || s.contains("air canada") || s.contains("via rail") || s.contains("taxi"))
            return "Travel";
        if (s.contains("pharmacy") || s.contains("shoppers") || s.contains("guardian")
                || s.contains("health") || s.contains("clinic"))
            return "Health";
        if (s.contains("best buy") || s.contains("electronics") || s.contains("apple")
                || s.contains("samsung"))
            return "Electronics";
        if (s.contains("h&m") || s.contains("zara") || s.contains("uniqlo") || s.contains("clothing"))
            return "Clothing";
        if (s.contains("game") || s.contains("steam") || s.contains("playstation"))
            return "Games";
        return "Uncategorized";
    }
}
