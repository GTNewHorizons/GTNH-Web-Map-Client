package com.gtnewhorizons.gwmclient;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

import com.gtnewhorizons.gwmclient.client.ClientEvents;
import com.gtnewhorizons.gwmclient.client.KeyBindings;
import com.gtnewhorizons.gwmclient.command.GwmcCommand;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        KeyBindings.init();

        MinecraftForge.EVENT_BUS.register(ClientEvents.INSTANCE);
        FMLCommonHandler.instance()
            .bus()
            .register(ClientEvents.INSTANCE);

        ClientCommandHandler.instance.registerCommand(new GwmcCommand());
    }
}
