package com.gtnewhorizons.gwmclient.storage;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

public abstract class GenericMap {

    public abstract Point3d mapCoordToWorld(Point2d p);

    public abstract Point2d worldToMapCoord(Point3d p);

    public abstract PerMapTileDataBase getTileDataBase();
}
