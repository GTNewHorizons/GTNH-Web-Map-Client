package com.gtnewhorizons.gwmclient.storage;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

public class RemoteMap extends GenericMap {

    private final RemoteWorld world;
    private final String name;
    private final String title;
    private final String prefix;

    private final PerMapTileDataBase tileDataBase;
    private double[] mapToWorld;
    private double[] worldToMap;

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

    public void setMatrices(double[] mapToWorldArr, double[] worldToMapArr) {
        this.mapToWorld = mapToWorldArr;
        this.worldToMap = worldToMapArr;
    }

    public Point3d mapCoordToWorld(Point2d p) {
        double lat = p.x * 128, lon = p.y * 128 + 128, y = 64;

        double wx = mapToWorld[0] * lat + mapToWorld[1] * lon + mapToWorld[2] * y;
        double wz = mapToWorld[6] * lat + mapToWorld[7] * lon + mapToWorld[8] * y;

        Point3d ret = new Point3d(wx, y, wz);
        return ret;
    }

    @Override
    public Point2d worldToMapCoord(Point3d p) {
        double x = worldToMap[0] * p.x + worldToMap[1] * p.y + worldToMap[2] * p.z;
        double y = worldToMap[3] * p.x + worldToMap[4] * p.y + worldToMap[5] * p.z;

        return new Point2d(x / 128, y / 128);
    }
}
