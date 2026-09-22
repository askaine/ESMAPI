package com.askaine_.ExoticScanMod.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;


public class ESMAPI {

    private final String databaseUrl;

    public ESMAPI(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }
    
    

    // Fetch data from the database
    public JsonObject getData(String path,String ESMAPIKey) throws IOException {
        URL url = new URL(databaseUrl + path + ".json");  // Make sure the URL ends with ".json"
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parse the response into a JsonObject
        JsonParser parser = new JsonParser();
        return parser.parse(response.toString()).getAsJsonObject();
    }

    // Add or update player data in the database using the PATCH method
    public void sendData(String path, Map<String, JsonObject> batchPlayerData, Map<String, JsonArray> batchedExotics, ICommandSender sender, String ESMAPIKey, JsonObject DataBase) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Merge batchPlayerData into DataBase
            for (Map.Entry<String, JsonObject> entry : batchPlayerData.entrySet()) {
                DataBase.add(entry.getKey(), entry.getValue());
            }

            // Merge batchedExotics into DataBase
            for (Map.Entry<String, JsonArray> entry : batchedExotics.entrySet()) {
                DataBase.add(entry.getKey(), entry.getValue());
            }

            // Send the updated database in a single request
            HttpPatch patch = new HttpPatch(databaseUrl + path + ".json");
            patch.setHeader("Content-Type", "application/json");
            patch.setEntity(new StringEntity(DataBase.toString()));

            int responseCode = client.execute(patch).getStatusLine().getStatusCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 200) {
                System.out.println("All batch data sent successfully in one request!");
            } else {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "ERROR:" + responseCode));
            }
        } catch (Exception e) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Error:" + e.toString()));
        }
    }
    


}
