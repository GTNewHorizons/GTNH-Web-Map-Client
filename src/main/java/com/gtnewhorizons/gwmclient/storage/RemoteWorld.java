package com.gtnewhorizons.gwmclient.storage;

import java.util.ArrayList;

public class RemoteWorld {

    private final String name;
    private final String title;

    private final ArrayList<RemoteMap> maps = new ArrayList<>();

    public RemoteWorld(String worldName, String worldTitle) {
        this.name = worldName;
        this.title = worldTitle;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<RemoteMap> getMaps() {
        return maps;
    }
}
