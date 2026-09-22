package com.askaine_.ExoticScanMod.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ScanAuctionCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "scanauction";  // Corrected method name for Minecraft 1.8.9
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/scanauction";  // This method returns the usage of the command
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // Here, you can scan the lobby for armor.
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Scanning Auction House..."));
    }
}