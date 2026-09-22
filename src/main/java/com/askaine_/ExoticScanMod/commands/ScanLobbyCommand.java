package com.askaine_.ExoticScanMod.commands;

import com.askaine_.ExoticScanMod.kotlin.Colours;
import com.askaine_.ExoticScanMod.kotlin.Colours.*;
import com.askaine_.ExoticScanMod.api.*;
import com.askaine_.ExoticScanMod.config.*;



import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import net.minecraft.util.IChatComponent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import com.mojang.authlib.GameProfile;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.querz.nbt.io.NamedTag;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.tag.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import net.querz.io.Deserializer;
import net.querz.nbt.tag.Tag;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.querz.io.MaxDepthReachedException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashSet;
	
import java.util.Collection;



public class ScanLobbyCommand extends CommandBase {

    private Map<String, String> playerProfiles = new HashMap<>();
    private boolean foundExotic = false;
    private boolean playerInDataBase = false;
    private final String databaseUrl = "YOUR_DATABASE_URL_HERE";
    private String path = "playerData";
    private String ESMAPIKey = "YOUR_ESM_API_KEY_HERE";
    private ESMAPI client = new ESMAPI(databaseUrl);
    
    private static final int MAX_REQUESTS_PER_MINUTE = 300;
    private static int apiRequestCount = 0;
    private static long firstRequestTime = 0;
    private boolean throttleMessagePrinted = false;
    
    private int count =0;

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }
    @Override
    public String getCommandName() {
        return "scan";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/scan";
    }

    

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        throttleMessagePrinted = false;
        String HypixelAPIKey = Config.apiKey;
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Scanning Lobby..."));

        ExecutorService executor = Executors.newFixedThreadPool(8);
        Set<String> processedUUIDs = Collections.synchronizedSet(new HashSet<>());
        Map<String, JsonObject> batchPlayerData = new ConcurrentHashMap<>();
        Map<String, JsonArray> batchedExotics = new ConcurrentHashMap<>();

        new Thread(() -> {
            try {
                // 1. Download the current database once
                JsonObject DataBase = loadJsonFromFile("DataBase.json");
                getPlayerUUIDsInLobby();

                for (String playerUUID : playerProfiles.keySet()) {
                    if (playerUUID.equals(Minecraft.getMinecraft().thePlayer.getUniqueID().toString())) continue;

                    final String finalUUID = playerUUID.replaceAll("-", "");
                    final String playerName = playerProfiles.get(playerUUID);

                    // **Check if player is already in the database**
                    if (DataBase != null && DataBase.has(playerName)) {
                    	JsonObject player = (JsonObject) DataBase.get(playerName);
                    	JsonArray exotics = (JsonArray) player.get("exotics");
                    	StringBuilder hoverText = new StringBuilder(EnumChatFormatting.GOLD + playerName + "'s Exotics:\n");
                    	boolean print = false;
                        	for (int i = 0; i < exotics.size(); i++) {
                        	    JsonObject Exotics = exotics.get(i).getAsJsonObject();
                        	    if(!Exotics.toString().contains("None")) {
                        	        print = true;
                            		JsonObject exotic = Exotics.getAsJsonObject();
                                    String armorName = exotic.get("armorName").getAsString();
                                    String colorHex = exotic.get("colorHex").getAsString();
                                    EnumChatFormatting closestColor = getClosestChatColor(colorHex);
                                    hoverText.append(EnumChatFormatting.AQUA).append(armorName)
                                    .append(EnumChatFormatting.DARK_GRAY).append(" (").append(closestColor).append(colorHex).append(")\n");
                            		
                            	} 
                        	}
                        	


                        // Create hoverable chat component
                        	if(print==true) {
                        IChatComponent playerComponent = new ChatComponentText(EnumChatFormatting.AQUA + 
                            "Player: " + playerName + " has " + exotics.size() + " exotics");
                        playerComponent.setChatStyle(new ChatStyle().setChatHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(hoverText.toString()))
                        ));
                        sender.addChatMessage(playerComponent);
                        	}
                        continue; // Skip scanning this player
                    }

                    executor.submit(() -> {
                        if (!processedUUIDs.add(finalUUID)) return;

                        int playerLevel = getPlayerSkyblockLevel(playerUUID);
                        if (playerLevel > Config.levelCap) return;

                        try {
                            JsonObject playerData = HypixelAPI.getPlayerData(HypixelAPIKey, finalUUID);
                            incrementApiRequestCount();

                            if (playerData != null && playerData.has("profiles") && canMakeApiRequest(sender)) {
                                JsonArray profilesArray = playerData.getAsJsonArray("profiles");

                                for (JsonElement profileElement : profilesArray) {
                                    JsonObject profileData = profileElement.getAsJsonObject();
                                    String profileID = profileData.get("profile_id").getAsString();
                                    String cuteName = profileData.get("cute_name").getAsString();

                                    if (profileData.has("members")) {
                                        JsonObject members = profileData.getAsJsonObject("members");
                                        if (members.has(finalUUID)) {
                                            JsonObject UUID = members.getAsJsonObject(finalUUID);
                                            if (UUID.has("inventory")) {
                                                JsonObject inventories = UUID.getAsJsonObject("inventory");
                                                processPlayerInventory(inventories, finalUUID, sender, playerName, cuteName, ESMAPIKey, DataBase, batchedExotics);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Error for player " + finalUUID + ": " + e.getMessage()));
                        }
                    });
                }

                executor.shutdown();
                while (!executor.isTerminated()) Thread.sleep(50);

                // 3. Send all the processed data in ONE request
                sendData("DataBase.json", batchPlayerData, batchedExotics, sender, DataBase);

                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Lobby scan completed!"));
            } catch (Exception e) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Error during scan: " + e.getMessage()));
            }
        }).start();
    }


    public static JsonObject loadJsonFromFile(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
        	JsonParser parser = new JsonParser();
            return parser.parse(reader).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
            return new JsonObject();  // Return empty JSON if file read fails
        }
    }
    public void sendData(String filePath, Map<String, JsonObject> batchPlayerData, Map<String, JsonArray> batchedExotics, ICommandSender sender, JsonObject DataBase) {
        try {
            // Merge batchPlayerData into DataBase
            for (Map.Entry<String, JsonObject> entry : batchPlayerData.entrySet()) {
                String playerName = entry.getKey();
                JsonObject playerData = entry.getValue();

                // Ensure player data exists in DataBase
                if (!DataBase.has(playerName)) {
                    DataBase.add(playerName, new JsonObject());
                }

                // Merge player data into DataBase
                JsonObject existingPlayerData = DataBase.getAsJsonObject(playerName);
                for (Map.Entry<String, JsonElement> dataEntry : playerData.entrySet()) {
                    existingPlayerData.add(dataEntry.getKey(), dataEntry.getValue());
                }
            }

            // Merge batchedExotics into each player's "exotics" array
            for (Map.Entry<String, JsonArray> entry : batchedExotics.entrySet()) {
                String playerName = entry.getKey();
                JsonArray exotics = entry.getValue();

                // Ensure player data exists in DataBase
                if (!DataBase.has(playerName)) {
                    DataBase.add(playerName, new JsonObject());
                }

                // Ensure "exotics" array exists under the player entry
                JsonObject playerData = DataBase.getAsJsonObject(playerName);
                if (!playerData.has("exotics")) {
                    playerData.add("exotics", new JsonArray());
                }

                // Merge exotics into the player's "exotics" array
                JsonArray existingExotics = playerData.getAsJsonArray("exotics");
                for (JsonElement exoticItem : exotics) {
                    existingExotics.add(exoticItem);
                }
            }

            // Save DataBase to JSON file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(filePath)) {
                gson.toJson(DataBase, writer);
            }

            System.out.println("All batch data saved successfully to " + filePath + "!");
        } catch (IOException e) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Error: " + e.toString()));
        }
    }


    private synchronized boolean canMakeApiRequest(ICommandSender sender) {
        long currentTime = System.currentTimeMillis();

        if (firstRequestTime == 0) {
            firstRequestTime = currentTime;
        }

        if (currentTime - firstRequestTime >= 60000) {
            apiRequestCount = 0;
            firstRequestTime = currentTime;
            throttleMessagePrinted = false; // Reset the flag after 1 minute
        }

        if (apiRequestCount >= MAX_REQUESTS_PER_MINUTE) {
            if (!throttleMessagePrinted) {
                Minecraft.getMinecraft().addScheduledTask(() ->
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Key throttle: Too many API requests. Please wait 5 minutes."))
                );
                throttleMessagePrinted = true; // Set the flag to true after printing
            }
            return false;
        }
        return true;
    }


    private synchronized void incrementApiRequestCount() {
        apiRequestCount++;
    }

    private void processPlayerInventory(JsonObject playerData, String UUID, ICommandSender sender, 
            String playerName, String profileName, String ESMAPIKey, 
            JsonObject DataBase, Map<String, JsonArray> batchedExotics) {
    try {
        if (playerData != null) {
            boolean foundExotic = false; // Track if any exotics were found

            for (Map.Entry<String, JsonElement> entry : playerData.entrySet()) {
                String key = entry.getKey();
                JsonElement element = entry.getValue();

                if (element.isJsonObject() && element.getAsJsonObject().has("data")) {
                    String data = element.getAsJsonObject().get("data").getAsString();
                    try {
                        ByteArrayInputStream stream = getItem(data, sender);
                        NamedTag nbtData = new NBTDeserializer(false).fromStream(stream);
                        processArmorData(nbtData, sender, playerName, profileName, batchedExotics,DataBase);
                        foundExotic = true;
                    } catch (Exception e) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + 
                            "Error processing " + key + ": " + e.getMessage()));
                    }
                } 
                else if (element.isJsonObject() && key.equals("backpack_icons")) {
                    JsonObject backpackIcons = element.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> backpackEntry : backpackIcons.entrySet()) {
                        JsonObject backpackData = backpackEntry.getValue().getAsJsonObject();
                        if (backpackData.has("data")) {
                            String data = backpackData.get("data").getAsString();
                            try {
                                ByteArrayInputStream stream = getItem(data, sender);
                                NamedTag nbtData = new NBTDeserializer(false).fromStream(stream);
                                processArmorData(nbtData, sender, playerName, profileName, batchedExotics,DataBase);
                                foundExotic = true;
                            } catch (Exception e) {
                                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + 
                                    "Error processing backpack " + backpackEntry.getKey() + ": " + e.getMessage()));
                            }
                        }
                    }
                }
            }

            // If no exotics were found, mark the player as "None"
            if (!foundExotic) {
                JsonObject noExotics = new JsonObject();
                noExotics.addProperty("None", "None");
                batchedExotics.computeIfAbsent(playerName, k -> new JsonArray()).add(noExotics);
            }

        }
    } catch (Exception e) {
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + 
            "Error in processPlayerInventory: " + e.getMessage()));
    }
}


    public ByteArrayInputStream getItem(String data,ICommandSender sender) throws IOException {
        // Replace escaped equals sign with regular equals
    	String cleanedData = data.replace("\\u003d", "=").trim();

        // Ensure the length is a multiple of 4 by adjusting padding
        while (cleanedData.length() % 4 != 0) {
            cleanedData += "=";
        }

        // Decode Base64 string
        byte[] buf = Base64.getDecoder().decode(cleanedData.getBytes(StandardCharsets.UTF_8));

        // Decompress the GZIP data if necessary
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buf);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);

        // Read decompressed data into a byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len;
        while ((len = gzipInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        byte[] decompressedData = byteArrayOutputStream.toByteArray();
        // Deserialize the NBT data from the decompressed byte array
        try (ByteArrayInputStream byteArrayStream = new ByteArrayInputStream(decompressedData)) {
        	return byteArrayStream;
        	
        	}
        catch(IOException e){
            e.printStackTrace();
        	String errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "NBT Deserialization failed: " + errorMessage));
            throw e;
        }
        
    }

    private void processArmorData(NamedTag nbtData, ICommandSender sender, String playerName, 
            String profileName, Map<String, JsonArray> batchedExotics, JsonObject DataBase) {
    try {
        if (nbtData.getTag() instanceof CompoundTag) {
            CompoundTag rootTag = (CompoundTag) nbtData.getTag();
            JsonArray exotics = new JsonArray();

            if (rootTag.containsKey("i")) {
                ListTag<CompoundTag> iList = rootTag.getListTag("i").asCompoundTagList();
                for (CompoundTag tag : iList) {
                    if (tag.containsKey("tag")) {
                        CompoundTag itemTag = tag.getCompoundTag("tag");

                        if (itemTag.containsKey("display")) {
                            CompoundTag displayTag = itemTag.getCompoundTag("display");
                            String armorName = itemTag.getCompoundTag("ExtraAttributes").getString("id");
                            String colorHex = displayTag.containsKey("color") 
                                ? String.format("#%06X", (0xFFFFFF & displayTag.getInt("color"))) 
                                : "No Color";

                            String exoticDetails = checkExotic(colorHex, armorName); // Store result once

                            if (armorName.toLowerCase().matches(".*(leggings|helmet|chestplate|boots).*") 
                                    && exoticDetails != null 
                                    && !colorHex.equals("No Color")) {

                                JsonObject exoticArmor = new JsonObject();
                                exoticArmor.addProperty("armorName", armorName);
                                exoticArmor.addProperty("exoticDetails", exoticDetails);
                                exoticArmor.addProperty("colorHex", colorHex);
                                exotics.add(exoticArmor);
                            }
                        }
                    }
                }
            }

            if (exotics.size() > 0) {
                batchedExotics.computeIfAbsent(playerName, k -> new JsonArray()).addAll(exotics);

                // Construct hover text
                StringBuilder hoverText = new StringBuilder(EnumChatFormatting.GOLD + playerName + "'s Exotics:\n");
                for (JsonElement exoticElement : exotics) {
                    JsonObject exotic = exoticElement.getAsJsonObject();
                    String armorName = exotic.get("armorName").getAsString();
                    String colorHex = exotic.get("colorHex").getAsString();
                    EnumChatFormatting closestColor = getClosestChatColor(colorHex);

                    hoverText.append(EnumChatFormatting.AQUA).append(armorName)
                             .append(EnumChatFormatting.DARK_GRAY).append(" (").append(closestColor).append(colorHex).append(")\n");
                }

                // Create hoverable chat component
                IChatComponent playerComponent = new ChatComponentText(EnumChatFormatting.GREEN + 
                    "Player: " + playerName + " Profile: " + profileName + " has " + exotics.size() + " exotics");
                playerComponent.setChatStyle(new ChatStyle().setChatHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(hoverText.toString()))
                ));

                sender.addChatMessage(playerComponent);
            } else {
                JsonObject noExotics = new JsonObject();
                if (!DataBase.has(playerName)) {
                    noExotics.addProperty("None", "None");
                }
                batchedExotics.computeIfAbsent(playerName, k -> new JsonArray()).add(noExotics);
            }
        }
    } catch (Exception e) {
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + 
            "Error in processArmorData: " + e.getMessage()));
    }
}


    

    

    private String checkExoticArmor(int hex, String armorName) {
        try {
        	Colours.INSTANCE.fetchColours();
        	String result = null;
        	if (Colours.getFairy().contains(hex) && !Colours.getFairyIds().contains(armorName)) {
                result = "Fairy";
            } else if (Colours.getCrystal().contains(hex) && !Colours.getCrystalIds().contains(armorName)) {
                result = "Crystal";
            } else if (Colours.getGlitched().containsKey(armorName) && Colours.getGlitched().get(armorName).contains(hex)) {
                result = "Glitched";
            } else if (Colours.getExotic().containsKey(armorName) && !Colours.getExotic().get(armorName).contains(hex)) {
                result = "Exotic";
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Error: " + errorMessage));
            return null;
        }

    }
    public static String checkExotic(String hex,String armorName) {
    	String exoticData = "\"exotics\": {\r\n"
    			+ "        \"RANCHERS_BOOTS\": \"0x000000\",\r\n"
    			+ "        \"SQUID_BOOTS\": \"0x000000\",\r\n"
    			+ "        \"FARMER_BOOTS\": \"0xcc5500\",\r\n"
    			+ "        \"MUSIC_PANTS\": \"0x4cfd3\",\r\n"
    			+ "        \"OBSIDIAN_CHESTPLATE\": \"0x000000\",\r\n"
    			+ "        \"HOLY_DRAGON_BOOTS\": \"0x47d147\",\r\n"
    			+ "        \"HOLY_DRAGON_LEGGINGS\": \"0x47d147\",\r\n"
    			+ "        \"SUPERIOR_DRAGON_BOOTS\": \"0xf25d18\",\r\n"
    			+ "        \"HOLY_DRAGON_CHESTPLATE\": \"0x47d147\",\r\n"
    			+ "        \"SUPERIOR_DRAGON_LEGGINGS\": \"0xf2df11\",\r\n"
    			+ "        \"SUPERIOR_DRAGON_CHESTPLATE\": \"0xf2df11\",\r\n"
    			+ "        \"UNSTABLE_DRAGON_BOOTS\": \"0xb212e3\",\r\n"
    			+ "        \"UNSTABLE_DRAGON_LEGGINGS\": \"0xb212e3\",\r\n"
    			+ "        \"STRONG_DRAGON_LEGGINGS\": \"0xe09419\",\r\n"
    			+ "        \"UNSTABLE_DRAGON_CHESTPLATE\": \"0xb212e3\",\r\n"
    			+ "        \"UNSTABLE_DRAGON_HELMET\": \"0xb212e3\",\r\n"
    			+ "        \"STRONG_DRAGON_BOOTS\": \"0xf0d124\",\r\n"
    			+ "        \"STRONG_DRAGON_CHESTPLATE\": \"0xd91e41\",\r\n"
    			+ "        \"PROTECTOR_DRAGON_BOOTS\": \"0x99978b\",\r\n"
    			+ "        \"PROTECTOR_DRAGON_LEGGINGS\": \"0x99978b\",\r\n"
    			+ "        \"WISE_DRAGON_LEGGINGS\": \"0x29f0e9\",\r\n"
    			+ "        \"PROTECTOR_DRAGON_CHESTPLATE\": \"0x99978b\",\r\n"
    			+ "        \"WISE_DRAGON_BOOTS\": \"0x29f0e9\",\r\n"
    			+ "        \"WISE_DRAGON_CHESTPLATE\": \"0x29f0e9\",\r\n"
    			+ "        \"OLD_DRAGON_BOOTS\": \"0xf0e6aa\",\r\n"
    			+ "        \"OLD_DRAGON_LEGGINGS\": \"0xf0e6aa\",\r\n"
    			+ "        \"OLD_DRAGON_CHESTPLATE\": \"0xf0e6aa\",\r\n"
    			+ "        \"YOUNG_DRAGON_BOOTS\": \"0xdde4f0\",\r\n"
    			+ "        \"YOUNG_DRAGON_LEGGINGS\": \"0xdde4f0\",\r\n"
    			+ "        \"ELEGANT_TUXEDO_LEGGINGS\": \"0xfefdfc\",\r\n"
    			+ "        \"YOUNG_DRAGON_CHESTPLATE\": \"0xdde4f0\",\r\n"
    			+ "        \"ELEGANT_TUXEDO_BOOTS\": \"0x191919\",\r\n"
    			+ "        \"ELEGANT_TUXEDO_CHESTPLATE\": \"0x191919\",\r\n"
    			+ "        \"FANCY_TUXEDO_CHESTPLATE\": \"0x332a2a\",\r\n"
    			+ "        \"FANCY_TUXEDO_BOOTS\": \"0x332a2a\",\r\n"
    			+ "        \"FANCY_TUXEDO_LEGGINGS\": \"0xd4d4d4\",\r\n"
    			+ "        \"WEREWOLF_BOOTS\": \"0x1d1105\",\r\n"
    			+ "        \"WEREWOLF_LEGGINGS\": \"0x1d1105\",\r\n"
    			+ "        \"SHARK_SCALE_HELMET\": \"0x2ca6\",\r\n"
    			+ "        \"WEREWOLF_CHESTPLATE\": \"0x1d1105\",\r\n"
    			+ "        \"BAT_PERSON_BOOTS\": \"0x000000\",\r\n"
    			+ "        \"BAT_PERSON_CHESTPLATE\": \"0x000000\",\r\n"
    			+ "        \"SHARK_SCALE_BOOTS\": \"0x002ca6\",\r\n"
    			+ "        \"SHARK_SCALE_LEGGINGS\": \"0x002ca6\",\r\n"
    			+ "        \"SHARK_SCALE_CHESTPLATE\": \"0x002ca6\",\r\n"
    			+ "        \"BAT_PERSON_LEGGINGS\": \"0x000000\",\r\n"
    			+ "        \"GLACITE_BOOTS\": \"0x03fcf8\",\r\n"
    			+ "        \"MINERAL_LEGGINGS\": \"0xcce5ff\",\r\n"
    			+ "        \"GLACITE_LEGGINGS\": \"0x03fcf8\",\r\n"
    			+ "        \"GLACITE_CHESTPLATE\": \"0x03fcf8\",\r\n"
    			+ "        \"MINERAL_BOOTS\": \"0xcce5ff\",\r\n"
    			+ "        \"SNOW_SUIT_LEGGINGS\": \"0xffffff\",\r\n"
    			+ "        \"MINERAL_CHESTPLATE\": \"0xcce5ff\",\r\n"
    			+ "        \"SNOW_SUIT_BOOTS\": \"0xffffff\",\r\n"
    			+ "        \"SNOW_SUIT_CHESTPLATE\": \"0xffffff\",\r\n"
    			+ "        \"SPOOKY_LEGGINGS\": \"0x606060\",\r\n"
    			+ "        \"SPOOKY_BOOTS\": \"0x606060\",\r\n"
    			+ "        \"SPOOKY_CHESTPLATE\": \"0x606060\",\r\n"
    			+ "        \"TARANTULA_LEGGINGS\": \"0x000000\",\r\n"
    			+ "        \"TARANTULA_BOOTS\": \"0x000000\",\r\n"
    			+ "        \"SPONGE_CHESTPLATE\": \"0xffdc51\",\r\n"
    			+ "        \"TARANTULA_CHESTPLATE\": \"0x000000\",\r\n"
    			+ "        \"TARANTULA_HELMET\": \"0x000000\",\r\n"
    			+ "        \"SPONGE_LEGGINGS\": \"0xffdc51\",\r\n"
    			+ "        \"SPONGE_BOOTS\": \"0xffdc51\",\r\n"
    			+ "        \"SPEEDSTER_BOOTS\": \"0xe0fcf7\",\r\n"
    			+ "        \"CHEAP_TUXEDO_BOOTS\": \"0x383838\",\r\n"
    			+ "        \"SPEEDSTER_LEGGINGS\": \"0xe0fcf7\",\r\n"
    			+ "        \"SPEEDSTER_CHESTPLATE\": \"0xe0fcf7\",\r\n"
    			+ "        \"SPEEDSTER_HELMET\": \"0xe0fcf7\",\r\n"
    			+ "        \"CHEAP_TUXEDO_LEGGINGS\": \"0xc7c7c7\",\r\n"
    			+ "        \"FROZEN_BLAZE_CHESTPLATE\": \"0xa0daef\",\r\n"
    			+ "        \"FROZEN_BLAZE_BOOTS\": \"0xa0daef\",\r\n"
    			+ "        \"FROZEN_BLAZE_LEGGINGS\": \"0xa0daef\",\r\n"
    			+ "        \"FROZEN_BLAZE_HELMET\": \"0xa0daef\",\r\n"
    			+ "        \"CHEAP_TUXEDO_CHESTPLATE\": \"0x383838\",\r\n"
    			+ "        \"BLAZE_BOOTS\": \"0xf7da33\",\r\n"
    			+ "        \"BLAZE_LEGGINGS\": \"0xf7da33\",\r\n"
    			+ "        \"EMERALD_ARMOR_BOOTS\": \"0x00ff00\",\r\n"
    			+ "        \"EMERALD_ARMOR_LEGGINGS\": \"0x00ff00\",\r\n"
    			+ "        \"EMERALD_ARMOR_CHESTPLATE\": \"0x00ff00\",\r\n"
    			+ "        \"BLAZE_CHESTPLATE\": \"0xf7da33\",\r\n"
    			+ "        \"CHESTPLATE_OF_THE_PACK\": \"0xff0000\",\r\n"
    			+ "        \"ARMOR_OF_MAGMA_LEGGINGS\": \"0xff9300\",\r\n"
    			+ "        \"EMERALD_ARMOR_HELMET\": \"0x00ff00\",\r\n"
    			+ "        \"ARMOR_OF_MAGMA_CHESTPLATE\": \"0xff9300\",\r\n"
    			+ "        \"ARMOR_OF_MAGMA_BOOTS\": \"0xff9300\",\r\n"
    			+ "        \"ARMOR_OF_MAGMA_HELMET\": \"0xff9300\",\r\n"
    			+ "        \"SALMON_LEGGINGS_NEW\": \"0xa82b76\",\r\n"
    			+ "        \"CREEPER_LEGGINGS\": \"0x7ae82c\",\r\n"
    			+ "        \"GUARDIAN_CHESTPLATE\": \"0x117391\",\r\n"
    			+ "        \"HELMET_OF_THE_PACK\": \"0xffffff\",\r\n"
    			+ "        \"SALMON_BOOTS_NEW\": \"0xc13c0f\",\r\n"
    			+ "        \"SALMON_HELMET_NEW\": \"0xc13c0f\",\r\n"
    			+ "        \"GROWTH_BOOTS\": \"0x00be00\",\r\n"
    			+ "        \"SALMON_BOOTS\": \"0xc13c0f\",\r\n"
    			+ "        \"SALMON_CHESTPLATE_NEW\": \"0xa82b76\",\r\n"
    			+ "        \"SALMON_HELMET\": \"0xc13c0f\",\r\n"
    			+ "        \"SALMON_CHESTPLATE\": \"0xa82b76\",\r\n"
    			+ "        \"SALMON_LEGGINGS\": \"0xa82b76\",\r\n"
    			+ "        \"GROWTH_CHESTPLATE\": \"0x00be00\",\r\n"
    			+ "        \"GROWTH_LEGGINGS\": \"0x00be00\",\r\n"
    			+ "        \"LAPIS_ARMOR_HELMET\": \"0x0000ff\",\r\n"
    			+ "        \"FARM_ARMOR_CHESTPLATE\": \"0xffd700\",\r\n"
    			+ "        \"FARM_ARMOR_BOOTS\": \"0xffd700\",\r\n"
    			+ "        \"FARM_ARMOR_LEGGINGS\": \"0xffd700\",\r\n"
    			+ "        \"GROWTH_HELMET\": \"0x00be00\",\r\n"
    			+ "        \"FARM_ARMOR_HELMET\": \"0xffd700\",\r\n"
    			+ "        \"MINER_OUTFIT_BOOTS\": \"0x7a7964\",\r\n"
    			+ "        \"MINER_OUTFIT_LEGGINGS\": \"0x7a7964\",\r\n"
    			+ "        \"MINER_OUTFIT_CHESTPLATE\": \"0x7a7964\",\r\n"
    			+ "        \"LAPIS_ARMOR_BOOTS\": \"0x0000ff\",\r\n"
    			+ "        \"MINER_OUTFIT_HELMET\": \"0x7a7964\",\r\n"
    			+ "        \"LAPIS_ARMOR_CHESTPLATE\": \"0x0000ff\",\r\n"
    			+ "        \"LEAFLET_LEGGINGS\": \"0x4dcc4d\",\r\n"
    			+ "        \"LAPIS_ARMOR_LEGGINGS\": \"0x0000ff\",\r\n"
    			+ "        \"LEAFLET_CHESTPLATE\": \"0x4dcc4d\",\r\n"
    			+ "        \"CACTUS_LEGGINGS\": \"0x00ff00\",\r\n"
    			+ "        \"CACTUS_CHESTPLATE\": \"0x00ff00\",\r\n"
    			+ "        \"CACTUS_BOOTS\": \"0x00ff00\",\r\n"
    			+ "        \"PUMPKIN_LEGGINGS\": \"0xedaa36\",\r\n"
    			+ "        \"LEAFLET_BOOTS\": \"0x4dcc4d\",\r\n"
    			+ "        \"CACTUS_HELMET\": \"0x00ff00\",\r\n"
    			+ "        \"PUMPKIN_BOOTS\": \"0xedaa36\",\r\n"
    			+ "        \"ANGLER_BOOTS\": \"0x0b004f\",\r\n"
    			+ "        \"PUMPKIN_CHESTPLATE\": \"0xedaa36\",\r\n"
    			+ "        \"PUMPKIN_HELMET\": \"0xedaa36\",\r\n"
    			+ "        \"ANGLER_LEGGINGS\": \"0x0b004f\",\r\n"
    			+ "        \"MUSHROOM_BOOTS\": \"0xff0000\",\r\n"
    			+ "        \"ANGLER_CHESTPLATE\": \"0x0b004f\",\r\n"
    			+ "        \"MUSHROOM_LEGGINGS\": \"0xff0000\",\r\n"
    			+ "        \"FARM_SUIT_BOOTS\": \"0xffff00\",\r\n"
    			+ "        \"MUSHROOM_CHESTPLATE\": \"0xff0000\",\r\n"
    			+ "        \"MUSHROOM_HELMET\": \"0xff0000\",\r\n"
    			+ "        \"FARM_SUIT_HELMET\": \"0xffff00\",\r\n"
    			+ "        \"FARM_SUIT_LEGGINGS\": \"0xffff00\",\r\n"
    			+ "        \"FARM_SUIT_CHESTPLATE\": \"0xffff00\"\r\n"
    			+ "    }";	
    	String Crystal = " \"0x1f0030\",\r\n"
    			+ "        \"0x46085e\",\r\n"
    			+ "        \"0x54146e\",\r\n"
    			+ "        \"0x5d1c78\",\r\n"
    			+ "        \"0x63237d\",\r\n"
    			+ "        \"0x6a2c82\",\r\n"
    			+ "        \"0x7e4196\",\r\n"
    			+ "        \"0x8e51a6\",\r\n"
    			+ "        \"0x9c64b3\",\r\n"
    			+ "        \"0xa875bd\",\r\n"
    			+ "        \"0xb88bc9\",\r\n"
    			+ "        \"0xc6a3d4\",\r\n"
    			+ "        \"0xd9c1e3\",\r\n"
    			+ "        \"0xe5d1ed\",\r\n"
    			+ "        \"0xefe1f5\",\r\n"
    			+ "        \"0xfcf3ff\"";
    	String Fairy = "\"0x660066\",\r\n"
    			+ "        \"0x660033\",\r\n"
    			+ "        \"0x99004c\",\r\n"
    			+ "        \"0xcc0066\",\r\n"
    			+ "        \"0xff007f\",\r\n"
    			+ "        \"0xff3399\",\r\n"
    			+ "        \"0xff66b2\",\r\n"
    			+ "        \"0xff99cc\",\r\n"
    			+ "        \"0xffcce5\",\r\n"
    			+ "        \"0xff99cc\",\r\n"
    			+ "        \"0xff66b2\",\r\n"
    			+ "        \"0xff3399\",\r\n"
    			+ "        \"0xff007f\",\r\n"
    			+ "        \"0xcc0066\",\r\n"
    			+ "        \"0x99004c\",\r\n"
    			+ "        \"0x660033\",\r\n"
    			+ "        \"0x660066\",\r\n"
    			+ "        \"0x990099\",\r\n"
    			+ "        \"0xcc00cc\",\r\n"
    			+ "        \"0xff00ff\",\r\n"
    			+ "        \"0xff33ff\",\r\n"
    			+ "        \"0xff66ff\",\r\n"
    			+ "        \"0xff99ff\",\r\n"
    			+ "        \"0xffccff\",\r\n"
    			+ "        \"0xe5ccff\",\r\n"
    			+ "        \"0xcc99ff\",\r\n"
    			+ "        \"0xb266ff\",\r\n"
    			+ "        \"0x9933ff\",\r\n"
    			+ "        \"0x7f00ff\",\r\n"
    			+ "        \"0x6600cc\",\r\n"
    			+ "        \"0x4c0099\",\r\n"
    			+ "        \"0x330066\",\r\n"
    			+ "        \"0x4c0099\",\r\n"
    			+ "        \"0x6600cc\",\r\n"
    			+ "        \"0x7f00ff\",\r\n"
    			+ "        \"0x9933ff\",\r\n"
    			+ "        \"0xb266ff\",\r\n"
    			+ "        \"0xcc99ff\",\r\n"
    			+ "        \"0xe5ccff\",\r\n"
    			+ "        \"0xffccff\",\r\n"
    			+ "        \"0xff99ff\",\r\n"
    			+ "        \"0xff66ff\",\r\n"
    			+ "        \"0xff33ff\",\r\n"
    			+ "        \"0xff00ff\",\r\n"
    			+ "        \"0xcc00cc\",\r\n"
    			+ "        \"0x990099\"";
    	String result = null;
    	hex=hex.replaceAll("#", "");
    	hex= hex.toLowerCase();

    	if (!exoticData.contains(('"'+armorName+ "\\\": \\\"0x" + hex).replaceAll("\\\\", "")) && exoticData.contains(armorName) && !Crystal.contains(hex)) {
            result = "Exotic";
        }
    	else if(Fairy.contains(hex) && !armorName.contains("FAIRY")) {
    		result = "Fairy";
    	}
    	else if(Crystal.contains(hex) && !armorName.contains("CRYSTAL")) {
    		result = "Crystal";
    	}
        return result;
    }
    public static EnumChatFormatting getClosestChatColor(String hexColor) {
        try {
            // Parse the hex string into RGB values
            int hexR = Integer.parseInt(hexColor.substring(1, 3), 16);
            int hexG = Integer.parseInt(hexColor.substring(3, 5), 16);
            int hexB = Integer.parseInt(hexColor.substring(5, 7), 16);

            EnumChatFormatting closestColor = EnumChatFormatting.WHITE; // Default color
            double minDistance = Double.MAX_VALUE;

            // Predefined RGB values for EnumChatFormatting
            int[][] predefinedColors = {
                {0, 0, 0},      // BLACK
                {0, 0, 170},    // DARK_BLUE
                {0, 170, 0},    // DARK_GREEN
                {0, 170, 170},  // DARK_AQUA
                {170, 0, 0},    // DARK_RED
                {170, 0, 170},  // DARK_PURPLE
                {255, 170, 0},  // GOLD
                {170, 170, 170},// GRAY
                {85, 85, 85},   // DARK_GRAY
                {85, 85, 255},  // BLUE
                {85, 255, 85},  // GREEN
                {85, 255, 255}, // AQUA
                {255, 85, 85},  // RED
                {255, 85, 255}, // LIGHT_PURPLE
                {255, 255, 85}, // YELLOW
                {255, 255, 255} // WHITE
            };

            EnumChatFormatting[] colors = {
                EnumChatFormatting.BLACK, EnumChatFormatting.DARK_BLUE, EnumChatFormatting.DARK_GREEN,
                EnumChatFormatting.DARK_AQUA, EnumChatFormatting.DARK_RED, EnumChatFormatting.DARK_PURPLE,
                EnumChatFormatting.GOLD, EnumChatFormatting.GRAY, EnumChatFormatting.DARK_GRAY,
                EnumChatFormatting.BLUE, EnumChatFormatting.GREEN, EnumChatFormatting.AQUA,
                EnumChatFormatting.RED, EnumChatFormatting.LIGHT_PURPLE, EnumChatFormatting.YELLOW,
                EnumChatFormatting.WHITE
            };

            // Compare the RGB values with the predefined colors
            for (int i = 0; i < predefinedColors.length; i++) {
                int[] rgb = predefinedColors[i];
                double distance = Math.sqrt(
                    Math.pow(hexR - rgb[0], 2) +
                    Math.pow(hexG - rgb[1], 2) +
                    Math.pow(hexB - rgb[2], 2)
                );

                if (distance < minDistance) {
                    minDistance = distance;
                    closestColor = colors[i];
                }
            }

            return closestColor;
        } catch (Exception e) {
            // Return default if there's an error parsing the hex
            return EnumChatFormatting.WHITE;
        }
    }
    
    private void getPlayerUUIDsInLobby() {
        playerProfiles.clear(); // Clear the map to start fresh

        // Get the current player's UUID
        GameProfile currentPlayerProfile = Minecraft.getMinecraft().getSession().getProfile();
        String currentPlayerUUID = currentPlayerProfile.getId().toString();

        // Deduplicate UUIDs with a Set
        Set<String> uniqueUUIDs = new HashSet<>();

        for (NetworkPlayerInfo networkPlayer : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
            GameProfile profile = networkPlayer.getGameProfile();
            String uuid = profile.getId().toString();
            String playerName = profile.getName();

            // Skip the current player's UUID
            if (uuid.equals(currentPlayerUUID)) {
                continue;
            }

            // Check if the player is visible in the world
            if (Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(profile.getId()) == null) {
                continue;
            }

            // Skip known bot names
            if (playerName.startsWith("NPC_") || playerName.startsWith("Bot_")) {
                continue;
            }

            // Skip bots with unusual pings
            if (networkPlayer.getResponseTime() == 0) {
                continue;
            }

            // Deduplicate and add to the map
            if (uniqueUUIDs.add(uuid)) {
                playerProfiles.put(uuid, playerName); // Add UUID and player name to the map
            }
        }

        // Log the final playerProfiles map
    }
    
    private int getPlayerSkyblockLevel(String playerUUID) {
        Collection<NetworkPlayerInfo> players = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();

        for (NetworkPlayerInfo info : players) {
            if (info.getGameProfile().getId().toString().equals(playerUUID)) {
                String displayName = info.getDisplayName() != null ? info.getDisplayName().getUnformattedText() : "";

                // Example format: "[LvXX] PlayerName"
                Pattern pattern = Pattern.compile("\\[Lv(\\d+)]");
                Matcher matcher = pattern.matcher(displayName);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            }
        }
        return -1; // Return -1 if level not found
    }


   
}