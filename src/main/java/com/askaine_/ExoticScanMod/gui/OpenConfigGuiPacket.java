package com.askaine_.ExoticScanMod.gui;

import javax.xml.ws.handler.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;


public class OpenConfigGuiPacket implements IMessage {
    public OpenConfigGuiPacket() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    // Handler class for handling the packet
    public static class Handler implements IMessageHandler<OpenConfigGuiPacket, IMessage> {

		@Override
		public IMessage onMessage(OpenConfigGuiPacket arg0,
				net.minecraftforge.fml.common.network.simpleimpl.MessageContext ctx) {
			// Ensure this runs on the client thread
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft.getMinecraft().displayGuiScreen(new ConfigGui());
            });
            return null;  // No response needed
		}
    }
}
