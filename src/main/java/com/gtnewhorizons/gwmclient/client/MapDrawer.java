package com.gtnewhorizons.gwmclient.client;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;
import org.lwjgl.util.Rectangle;

import com.cleanroommc.modularui.widget.sizer.Area;
import com.gtnewhorizons.gwmclient.storage.GenericMap;
import com.gtnewhorizons.gwmclient.storage.PerMapTileDataBase;
import com.gtnewhorizons.gwmclient.storage.Tile;

public class MapDrawer {

    private final int width;
    private final int height;
    Rectangle drawArea = new Rectangle(0, 0, 100, 100);

    PerMapTileDataBase currentTileDB;

    double viewPortX, viewPortY, viewPortW, viewPortH;

    private GenericMap currentMap;

    public MapDrawer(int width, int height) {
        this.width = width;
        this.height = height;

        viewPortX = viewPortY = 0;
        viewPortW = this.width / 128.0;
        viewPortH = this.height / 128.0;
    }

    public void setMap(GenericMap map) {
        if (currentTileDB != null) currentTileDB.clear();

        if (currentMap != null) {
            var centerPosWorld = currentMap
                .mapCoordToWorld(new Point2d(viewPortX + viewPortW / 2, viewPortY + viewPortH / 2));
            var newCenterMapCoord = map.worldToMapCoord(centerPosWorld);

            viewPortX = newCenterMapCoord.x - viewPortW / 2;
            viewPortY = newCenterMapCoord.y - viewPortH / 2;
        }

        currentTileDB = map.getTileDataBase();
        currentMap = map;
    }

    Tile getTile(int x, int y, int zoom) {

        return currentTileDB.getTileAt(x, y, zoom);
    }

    void draw() {
        if (currentTileDB == null) {
            return;
        }

        Tessellator tessellator = Tessellator.instance;
        tessellator.setTranslation(0, 0, 0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int zoom = 0;

        double tileWidthOnScreen = drawArea.getWidth() / viewPortW;
        int tileWidth = currentTileDB.getTileBaseWidth();

        while (zoom < 9 && tileWidthOnScreen < tileWidth / 2.0) {
            zoom++;
            tileWidthOnScreen *= 2;
        }

        int zoomStep = 1 << zoom;

        int minTileX = (int) viewPortX;
        int minTileY = (int) viewPortY;

        if (viewPortX < 0) minTileX -= 1;

        if (viewPortY < 0) minTileY -= 1;

        int maxTileX = (int) (viewPortX + viewPortW) + zoomStep;
        int maxTileY = (int) (viewPortY + viewPortH) + zoomStep;

        if (zoom != 0) {
            while (minTileX % zoomStep != 0) minTileX--;

            while (minTileY % zoomStep != 0) minTileY--;
        }

        for (int y = minTileY; y <= maxTileY; y += zoomStep) {
            for (int x = minTileX; x <= maxTileX; x += zoomStep) {
                Tile t = getTile(x, y, zoom);

                if (t == null) continue;
                t.update();

                double left, right, top, bottom;

                left = (x - viewPortX) * drawArea.getWidth() / viewPortW;
                right = (x - viewPortX + zoomStep) * drawArea.getWidth() / viewPortW;

                top = drawArea.getHeight() - (y - viewPortY + 1) * drawArea.getHeight() / viewPortH;
                bottom = drawArea.getHeight() - (y - viewPortY - zoomStep + 1) * drawArea.getHeight() / viewPortH;

                if (t.textureId != -1) {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, t.textureId);
                    // GL11.glColor4f(1, 1, 1, 1);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST); // GL11.GL_LINEAR_MIPMAP_NEAREST
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST); // GL11.GL_NEAREST
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

                    tessellator.startDrawingQuads();
                    tessellator.addVertexWithUV(left, top, 0, t.umin, t.vmin);
                    tessellator.addVertexWithUV(right, top, 0, t.umax, t.vmin);
                    tessellator.addVertexWithUV(right, bottom, 0, t.umax, t.vmax);
                    tessellator.addVertexWithUV(left, bottom, 0, t.umin, t.vmax);
                    tessellator.draw();
                }
            }
        }
    }

    Point lastMousePos;
    Point dragStartMousePos;
    double dragStartViewX, dragStartViewY;
    boolean dragging;

    public void handleMouseInput() {
        Minecraft mc = Minecraft.getMinecraft();
        boolean lmbDown = Mouse.isButtonDown(0);
        Point currentMousePos = new Point(
            (Mouse.getEventX() * width) / mc.displayWidth,
            (Mouse.getEventY() * height) / mc.displayHeight);
        int wheel = Mouse.getEventDWheel();

        if (dragging) {
            if (lmbDown) {
                if (dragStartMousePos != null) {

                    int dx = currentMousePos.getX() - dragStartMousePos.getX();
                    int dy = currentMousePos.getY() - dragStartMousePos.getY();

                    viewPortX = dragStartViewX - dx * viewPortW / drawArea.getWidth();
                    viewPortY = dragStartViewY - dy * viewPortH / drawArea.getHeight();
                }
            } else {
                dragging = false;
            }
        } else {
            if (lmbDown) {
                dragging = true;
                dragStartMousePos = currentMousePos;

                dragStartViewX = viewPortX;
                dragStartViewY = viewPortY;
            }
        }
        lastMousePos = currentMousePos;
    }

    public void zoomIn(Point currentMousePos) {
        double mouseOverX = (currentMousePos.getX() - drawArea.getX()) * viewPortW / drawArea.getWidth() + viewPortX;
        double mouseOverY = (currentMousePos.getY() - drawArea.getY()) * viewPortH / drawArea.getHeight() + viewPortY;

        viewPortX += (mouseOverX - viewPortX) / 2;
        viewPortY += (mouseOverY - viewPortY) / 2;

        viewPortW /= 2;
        viewPortH = viewPortW * drawArea.getHeight() / drawArea.getWidth();
    }

    public void zoomOut(Point currentMousePos) {
        viewPortW *= 2;
        viewPortH = viewPortW * drawArea.getHeight() / drawArea.getWidth();

        viewPortX -= viewPortW / 4;
        viewPortY -= viewPortH / 4;
    }

    public void updateScreen(Area a) {
        drawArea.setSize(a.w(), a.h());

        if (viewPortW <= 0) viewPortW = a.w() / 128.0;

        viewPortH = viewPortW * drawArea.getHeight() / drawArea.getWidth();
    }

    public void onClose() {
        if (currentTileDB != null) currentTileDB.clear();
    }

    public Point2d pointToMapCoord(Point p) {
        if (currentMap == null) return new Point2d();

        return new Point2d(
            (viewPortX + p.getX() * viewPortW / drawArea.getWidth()),
            (viewPortY + p.getY() * viewPortH / drawArea.getHeight()) - 1);
    }

    public void focusOnWorldPoint(Point3d worldPoint) {
        if (currentMap == null) return;

        var mapCoord = currentMap.worldToMapCoord(worldPoint);

        viewPortX = mapCoord.x - viewPortW / 2;
        viewPortY = mapCoord.y - viewPortH / 2;
    }

}
