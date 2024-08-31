package com.gtnewhorizons.gwmclient.storage;

import java.util.HashMap;

import org.lwjgl.util.Point;

public class PerMapTileDataBase {

    PerZoomLevel[] zoomLevels = new PerZoomLevel[10];

    public PerMapTileDataBase(String worldName, String mapPrefix, String title) {
        this.worldName = worldName;
        this.mapPrefix = mapPrefix;
        this.title = title;
        for (int i = 0; i < 10; i++) zoomLevels[i] = new PerZoomLevel(i);
    }

    public Tile getTileAt(int x, int y, int z) {
        return zoomLevels[z].getTileAt(x, y);
    }

    public int getTileBaseWidth() {
        return 128;
    }

    public void clear() {
        for (int i = 0; i < 10; i++) zoomLevels[i].clear();
    }

    final String worldName;
    final String mapPrefix;
    public final String title;

    class PerZoomLevel {

        private final int zoomLevel;
        HashMap<Point, TileLoaderBase> tileLoaders = new HashMap<>();
        HashMap<Point, Tile> tiles = new HashMap<>();

        public PerZoomLevel(int zoomLevel) {

            this.zoomLevel = zoomLevel;
        }

        public Tile getTileAt(int x, int y) {

            Point p = new Point(x, y);

            Tile t = tiles.get(p);

            if (t == null) {
                TileLoaderBase tlb = tileLoaders.get(p);

                if (tlb == null) {
                    tlb = new TileLoaderUrl();
                    tlb.beginLoad(worldName, mapPrefix, x, y, zoomLevel, new Tile());
                    tileLoaders.put(p, tlb);
                } else {
                    if (tlb.isDone()) {
                        if (tlb.isSuccess()) {
                            t = tlb.getTile();
                            tiles.put(p, t);

                            tileLoaders.remove(p);
                        } else {
                            tlb.retry();
                        }
                    }
                }
            }

            return t;
        }

        public void clear() {
            for (var t : tiles.values()) {
                t.delete();
            }
            tiles.clear();
            tileLoaders.clear();
        }
    }
}
