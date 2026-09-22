package com.askaine_.ExoticScanMod;


import com.askaine_.ExoticScanMod.commands.ScanLobbyCommand;
import com.askaine_.ExoticScanMod.gui.OpenConfigGuiPacket;
import com.askaine_.ExoticScanMod.commands.ScanAuctionCommand;
import com.askaine_.ExoticScanMod.commands.ScanIslandCommand;
import com.askaine_.ExoticScanMod.Network.NetworkHandler;
import com.askaine_.ExoticScanMod.commands.ESMConfigCommand;


import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.client.ClientCommandHandler;

@Mod(modid = ExoticScanMod.MODID, name = ExoticScanMod.NAME, version = ExoticScanMod.VERSION)
public class ExoticScanMod {

    public static final String MODID = "ExoticsScanmod";
    public static final String NAME = "ESM";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Perform pre-initialization tasks
        ClientCommandHandler.instance.registerCommand(new ScanLobbyCommand());
        ClientCommandHandler.instance.registerCommand(new ScanIslandCommand());
        ClientCommandHandler.instance.registerCommand(new ScanAuctionCommand());
        ClientCommandHandler.instance.registerCommand(new ESMConfigCommand());
        NetworkHandler.init();  // Register the packet handlers





    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Perform initialization tasks
    	
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // Register commands here
        event.registerServerCommand(new ScanLobbyCommand());
        event.registerServerCommand(new ScanAuctionCommand());



    }
}