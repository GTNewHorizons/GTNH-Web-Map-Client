package com.gtnewhorizons.gwmclient.storage;

public class RemoteMap {

    private final RemoteWorld world;
    private final String name;
    private final String title;
    private final String prefix;

    private final PerMapTileDataBase tileDataBase;

    public RemoteMap(RemoteWorld world, String name, String title, String prefix) {
        this.world = world;
        this.name = name;
        this.title = title;
        this.prefix = prefix;

        tileDataBase = new PerMapTileDataBase(world.getName(), prefix, world.getTitle() + " - " + title);
    }

    public PerMapTileDataBase getTileDataBase() {
        return tileDataBase;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }
}
