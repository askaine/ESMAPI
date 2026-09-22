package com.askaine_.ExoticScanMod.Network;

import com.askaine_.ExoticScanMod.gui.OpenConfigGuiPacket;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
	
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("yourmodid");

    public static void init() {
        int discriminator = 0;
        INSTANCE.registerMessage(OpenConfigGuiPacket.Handler.class, OpenConfigGuiPacket.class, discriminator++, Side.SERVER);
    }
}
