package com.gtnewhorizons.gwmclient.storage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

public class TileLoaderUrl extends TileLoaderBase {

    static ExecutorService threadpool = Executors.newCachedThreadPool();

    @Override
    public void beginLoad(String worldName, String mapName, int x, int y, int zoom, Tile t) {
        this.mapName = mapName;
        this.worldName = worldName;

        this.tileName = x + "_" + y;
        if (zoom > 0) {
            tileName = "zzzzzzzzzz".substring(0, zoom) + "_" + tileName;
        }

        this.tileGroup = (x / 32) + "_" + (y / 32);
        this.tile = t;

        task = threadpool.submit(() -> load());
    }

    @Override
    public Tile getTile() {
        return tile;
    }

    @Override
    public void retry() {
        retries++;

        if (retries > 10) retries = 10;

        task = threadpool.submit(() -> loadAfterDelay());
    }

    private Boolean loadAfterDelay() {
        try {

            Thread.sleep(60 * (1 << retries));
            return load();
        } catch (InterruptedException e) {
            return false;
        }
    }

    Future<Boolean> task;
    String mapName;
    String tileName;
    String worldName;
    private String tileGroup;

    Tile tile;

    int retries = 0;

    Boolean load() {
        URL imageURL = null;
        try {
            imageURL = new URL(
                RemoteConfiguration.baseUrl + "/tiles/"
                    + worldName
                    + "/"
                    + mapName
                    + "/"
                    + tileGroup
                    + "/"
                    + tileName
                    + ".png");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        // Case 1
        try {
            URLConnection urlConnection = imageURL.openConnection();
            InputStream is = urlConnection.getInputStream();
            BufferedImage img = ImageIO.read(is);
            tile.newlyLoadedImage = img;
            is.close();

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isDone() {
        return task != null && task.isDone();
    }

    @Override
    public boolean isSuccess() {
        try {
            return task != null && task.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
