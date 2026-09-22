package com.askaine_.ExoticScanMod.config;

public class Config {
    public static String apiKey = "";
    public static boolean includeFairy = false;
    public static int levelCap = 50; // Default level cap

    public static void setApiKey(String key) {
        apiKey = key;
    }

    public static void setIncludeFairy(boolean include) {
        includeFairy = include;
    }

    public static void setLevelCap(int cap) {
        levelCap = cap;
    }
}
