package com.askaine_.ExoticScanMod.gui;

import com.askaine_.ExoticScanMod.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;

public class ConfigGui extends GuiScreen {
    private GuiTextField apiKeyField;
    private GuiTextField levelCapField;
    private GuiButton toggleFairyButton;
    private GuiButton saveButton;

    private boolean includeFairy = Config.includeFairy;

    public ConfigGui() {
        Minecraft.getMinecraft().thePlayer.addChatMessage(
            new ChatComponentText(EnumChatFormatting.YELLOW + "Config GUI Constructor Called")
        );
        Minecraft.getMinecraft().displayGuiScreen(this); // Force set the screen to prevent it from closing immediately
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;  // So the game doesn't pause when opening the GUI
    }

    @Override
    public void initGui() {	
        Minecraft.getMinecraft().thePlayer.addChatMessage(
            new ChatComponentText(EnumChatFormatting.GREEN + "Config GUI Initialized")
        );
        super.initGui();
        this.buttonList.clear();  // Clear existing buttons

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        apiKeyField = new GuiTextField(0, this.fontRendererObj, centerX - 100, centerY - 50, 200, 20);
        apiKeyField.setText(Config.apiKey);

        levelCapField = new GuiTextField(1, this.fontRendererObj, centerX - 100, centerY - 20, 200, 20);
        levelCapField.setText(String.valueOf(Config.levelCap));

        toggleFairyButton = new GuiButton(2, centerX - 100, centerY + 10, 200, 20, getFairyButtonText());
        saveButton = new GuiButton(3, centerX - 100, centerY + 40, 200, 20, "Save Config");

        this.buttonList.add(toggleFairyButton);
        this.buttonList.add(saveButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        Minecraft.getMinecraft().thePlayer.addChatMessage(
            new ChatComponentText(EnumChatFormatting.RED + "Updating screen...")
        );
        apiKeyField.updateCursorCounter();
        levelCapField.updateCursorCounter();
    }

    private String getFairyButtonText() {
        return "Include Fairy: " + (includeFairy ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 2) {
            includeFairy = !includeFairy;
            button.displayString = getFairyButtonText();
        } else if (button.id == 3) {
            Config.setApiKey(apiKeyField.getText());
            Config.setIncludeFairy(includeFairy);

            try {
                int levelCap = Integer.parseInt(levelCapField.getText());
                Config.setLevelCap(levelCap);
            } catch (NumberFormatException e) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.RED + "Invalid Level Cap Input!")
                );
            }

            Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText(EnumChatFormatting.RED + "Closing GUI from button click")
            );
            mc.displayGuiScreen(null); // Close GUI
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // Keycode 1 is ESC
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText(EnumChatFormatting.RED + "ESC pressed - ignoring")
            );
            return; // Prevent closing the GUI
        }
        apiKeyField.textboxKeyTyped(typedChar, keyCode);
        levelCapField.textboxKeyTyped(typedChar, keyCode);
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        apiKeyField.mouseClicked(mouseX, mouseY, mouseButton);
        levelCapField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks); // Move this to the top
        Minecraft.getMinecraft().thePlayer.addChatMessage(
            new ChatComponentText(EnumChatFormatting.GREEN + "Draw screen")
        );
        drawDefaultBackground();
        drawCenteredString(this.fontRendererObj, "Exotic Scan Config", this.width / 2, this.height / 2 - 80, 0xFFFFFF);
        apiKeyField.drawTextBox();
        levelCapField.drawTextBox();
    }
    @Override
    public void onGuiClosed() {
        Minecraft.getMinecraft().thePlayer.addChatMessage(
            new ChatComponentText(EnumChatFormatting.RED + "GUI was closed")
        );
        super.onGuiClosed();
    }
}
