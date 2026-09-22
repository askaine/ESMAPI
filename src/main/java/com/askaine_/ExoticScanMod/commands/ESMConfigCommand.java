package com.askaine_.ExoticScanMod.commands;

import com.askaine_.ExoticScanMod.gui.ConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.Arrays;
import java.util.List;

public class ESMConfigCommand extends CommandBase {

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true; // Allows all players to use the command
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
    public List<String> getCommandAliases() {
        return Arrays.asList("esmconfig", "ESMCONFIG", "EsMcOnFiG");
    }

    @Override
    public String getCommandName() {
        return "ESMconfig";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ESMconfig - Opens the ExoticScanMod configuration GUI";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayer && sender.getEntityWorld().isRemote) {
            sender.addChatMessage(new ChatComponentText("Attempting to open Config GUI"));
            Minecraft.getMinecraft().displayGuiScreen(new ConfigGui());
        } else {
            sender.addChatMessage(new ChatComponentText("This command can only be used in-game."));
        }
    }

    @Override
    public int compareTo(ICommand o) {
        return this.getCommandName().compareToIgnoreCase(o.getCommandName());
    }
}
