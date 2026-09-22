package com.askaine_.ExoticScanMod.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.command.ICommandSender;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ChatComponentText;
import net.minecraft.client.Minecraft;

public class HypixelAPI {

    private static final String BASE_URL = "https://api.hypixel.net/";



    

    // Fetch player data based on profile ID
    public static JsonObject getPlayerData(String apiKey, String uuid) {
        try {
            String urlString = "https://api.hypixel.net/v2/skyblock/profiles?key=" + apiKey + "&uuid=" + uuid;
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Convert the response to a JsonObject
            JsonParser parser = new JsonParser();
            JsonObject jsonResponse = parser.parse(response.toString()).getAsJsonObject();
            return jsonResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Extract inventory data by inventory type (e.g., inv_contents, inv_armor)
    
    
}