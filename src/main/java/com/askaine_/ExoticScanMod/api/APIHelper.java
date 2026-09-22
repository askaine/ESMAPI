package com.askaine_.ExoticScanMod.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class APIHelper {

    // Method to fetch player data from the API
	public static JsonObject getPlayerData(String apiUrl) {
	    try {
	        // Construct the URL for the API call with the playerUUID and API key
	        URL url = new URL(apiUrl);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod("GET");
	        connection.setRequestProperty("Content-Type", "application/json");

	        // Get the response code
	        int responseCode = connection.getResponseCode();
	        if (responseCode == HttpURLConnection.HTTP_OK) {
	            // Parse the JSON response from the input stream
	        	try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
	        	    JsonParser parser = new JsonParser(); // Create an instance of JsonParser
	        	    JsonObject response = parser.parse(reader).getAsJsonObject();  // Use instance method parse()
	        	    return response;
	        	}
	        } else {
	            // Log the error if response is not OK
	            System.out.println("Error: Failed to fetch player data. HTTP Code: " + responseCode);
	            return null;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
}