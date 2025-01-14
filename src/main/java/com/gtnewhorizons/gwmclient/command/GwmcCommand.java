package com.gtnewhorizons.gwmclient.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import com.gtnewhorizons.gwmclient.Config;
import com.gtnewhorizons.gwmclient.storage.RemoteConfiguration;

public class GwmcCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "gwmc";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/gwmc <url>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        String url = args[0];
        while (url.endsWith("/")) url = url.substring(0, url.length() - 1);

        Config.setRemoteUrlBase(url);
        RemoteConfiguration.baseUrl = url;
    }

}
