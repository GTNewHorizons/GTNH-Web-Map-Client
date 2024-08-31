package com.gtnewhorizons.gwmclient.client;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ClientEvents {

    public static final ClientEvents INSTANCE = new ClientEvents();

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKey(InputEvent.KeyInputEvent event) {
        if (KeyBindings.openMap.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.displayGuiScreen(new MapScreen());
        }
    }
}
