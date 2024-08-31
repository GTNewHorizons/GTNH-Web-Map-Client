package com.gtnewhorizons.gwmclient.client;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.gtnewhorizons.gwmclient.GwmClientMod;

import cpw.mods.fml.client.registry.ClientRegistry;

public class KeyBindings {

    static KeyBinding openMap;

    public static void init() {
        openMap = new KeyBinding("key.gwmc.openmap", Keyboard.KEY_M, GwmClientMod.NAME);
        ClientRegistry.registerKeyBinding(openMap);
    }
}
