package com.gtnewhorizons.gwmclient;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config {

    private static File configFile;
    private static String remoteUrlBase;
    private static Property remoteUrlProperty;
    static Configuration configuration;

    public static void synchronizeConfiguration(File configFile) {
        Config.configFile = configFile;
        if (configuration == null) configuration = new Configuration(configFile);

        if (remoteUrlProperty == null)
            remoteUrlProperty = configuration.get("remote", "baseUrl", "http://127.0.0.1:8123");

        remoteUrlBase = remoteUrlProperty.getString();

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static void setRemoteUrlBase(String newRemoteUrlBase) {
        remoteUrlBase = newRemoteUrlBase;
        remoteUrlProperty.set(remoteUrlBase);
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

}
