package com.gtnewhorizons.gwmclient.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class GwmcCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "gwmc";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

    }
}
