package com.gtnewhorizons.gwmclient.storage;

public abstract class TileLoaderBase {

    public abstract boolean isDone();

    public abstract boolean isSuccess();

    public void beginLoad(String worldName, String mapName, int x, int y, int zoom, Tile t) {

    }

    public abstract Tile getTile();

    public abstract void retry();
}
