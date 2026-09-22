package com.askaine_.ExoticScanMod.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

import javax.swing.text.html.parser.Entity;


import net.minecraft.entity.EntityLivingBase;
import com.askaine_.ExoticScanMod.commands.ScanLobbyCommand;




public class ScanIslandCommand extends CommandBase {
	
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
        return "scanisland";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/scanisland";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            World world = player.worldObj;

            // Check if the player is on a Skyblock island
            if (!isSkyblockIsland(world)) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You must be on a Skyblock island to use this command!"));
                return;
            }

            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Scanning island for exotic items..."));

            // Scan entities on the island
            scanEntities(world, player);

            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Island scan complete."));
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This command can only be used by players."));
        }
    }

    private boolean isSkyblockIsland(World world) {
        // Add logic to determine if the world is a Skyblock island
        // This might involve checking for certain world names or dimensions
        return true; // Placeholder
    }

    private void scanEntities(World world, EntityPlayer player) {
        // Get all entities within a radius
    	List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, player.getEntityBoundingBox().expand(50, 50, 50));

        // Process item frames
    	List<EntityItemFrame> itemFrames = world.getEntitiesWithinAABB(EntityItemFrame.class, player.getEntityBoundingBox().expand(50, 50, 50));
    	for (EntityItemFrame itemFrame : itemFrames) {
    	    if (itemFrame.getDisplayedItem() != null) {
    	        processItem(itemFrame.getDisplayedItem(), player);
    	    }
    	}

    	// Process armor stands
    	List<EntityArmorStand> armorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, player.getEntityBoundingBox().expand(50, 50, 50));
    	for (EntityArmorStand armorStand : armorStands) {
    	    ItemStack head = armorStand.getCurrentArmor(3);
    	    ItemStack chest = armorStand.getCurrentArmor(2);
    	    ItemStack legs = armorStand.getCurrentArmor(1);
    	    ItemStack boots = armorStand.getCurrentArmor(0);

    	    if (head != null) processItem(head, player);
    	    if (chest != null) processItem(chest, player);
    	    if (legs != null) processItem(legs, player);
    	    if (boots != null) processItem(boots, player);
    	}
    }



    private void processItem(ItemStack item, EntityPlayer player) {
        if (item.hasTagCompound()) {
            NBTTagCompound nbt = item.getTagCompound();
            
            // Directly process the item without expecting a return value
            checkExoticItem(nbt, player);
        }
    }


    

    private void checkExoticItem(NBTTagCompound nbt, EntityPlayer player) {
        if (nbt.hasKey("display")) {
            NBTTagCompound displayTag = nbt.getCompoundTag("display");

            // Get armor name
            String armorName = displayTag.hasKey("Name") ? displayTag.getString("Name") : "SB_Util";

            // Check for color to determine exotic status
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD  + "" + armorName));
            if (displayTag.hasKey("color") && armorName!="SB_Util") {
                int color = displayTag.getInteger("color");
                String colorHex = String.format("#%06X", (0xFFFFFF & color));
                
                // Call checkExotic function
                String exoticStatus = ScanLobbyCommand.checkExotic(colorHex, armorName);

                // Determine closest chat color
                EnumChatFormatting closestColor = ScanLobbyCommand.getClosestChatColor(colorHex);

                // Print the result in chat
                if(exoticStatus!=null) {
                	Minecraft.getMinecraft().addScheduledTask(() -> 
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD 
                        + "Found: " + exoticStatus + closestColor 
                        + " Armor Name - " + armorName + " - Hex: " + colorHex))
                );
                } 
            }
        }
    }
}