package com.gtnewhorizons.gwmclient.client;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;
import org.lwjgl.util.Rectangle;

import com.cleanroommc.modularui.widget.sizer.Area;
import com.gtnewhorizons.gwmclient.storage.PerMapTileDataBase;
import com.gtnewhorizons.gwmclient.storage.RemoteConfiguration;
import com.gtnewhorizons.gwmclient.storage.Tile;

public class MapDrawer {

    static ExecutorService backgroundLoader = Executors.newSingleThreadExecutor();
    RemoteConfiguration remoteConfig;
    private final int width;
    private final int height;
    Rectangle drawArea = new Rectangle(0, 0, 100, 100);

    PerMapTileDataBase currentMap;
    int currentMapIndex = 0;

    ArrayList<PerMapTileDataBase> allMaps = new ArrayList<>();

    double viewPortX, viewPortY, viewPortW, viewPortH;

    boolean loadingMapList = true;

    public MapDrawer(int width, int height) {
        this.width = width;
        this.height = height;

        backgroundLoader.submit(() -> loadMapList());

        viewPortX = viewPortY = 0;
        viewPortW = this.width / 128.0;
        viewPortH = this.height / 128.0;
    }

    private void loadMapList() {

        remoteConfig = new RemoteConfiguration();
        remoteConfig.load();

        allMaps = remoteConfig.allMaps;

        if (allMaps.size() > 0) {
            currentMap = allMaps.get(0);
        }
        loadingMapList = false;
    }

    Tile getTile(int x, int y, int zoom) {

        return currentMap.getTileAt(x, y, zoom);
    }

    void draw() {
        if (currentMap == null) {
            // if (loadingMapList) drawString(fontRendererObj, "Loading maps...", 10, 10, 0xFFFFFF);
            // else drawString(fontRendererObj, "Failed to load maps!", 10, 10, 0xFF0000);
            return;
        }

        Tessellator tessellator = Tessellator.instance;
        tessellator.setTranslation(0, 0, 0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int zoom = 0;

        double tileWidthOnScreen = drawArea.getWidth() / viewPortW;
        int tileWidth = currentMap.getTileBaseWidth();

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
                top = drawArea.getHeight() - (y - viewPortY + zoomStep) * drawArea.getHeight() / viewPortH;
                bottom = drawArea.getHeight() - (y - viewPortY) * drawArea.getHeight() / viewPortH;

                if (t.textureId != -1) {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, t.textureId);
                    // GL11.glColor4f(1, 1, 1, 1);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); // GL11.GL_LINEAR_MIPMAP_NEAREST
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); // GL11.GL_NEAREST
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

        // drawString(fontRendererObj, currentMap.title, 10, 10, 0xFFFFFF);
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

    void setNewMapIndex(int newIndex) {
        if (newIndex >= allMaps.size()) newIndex = 0;
        if (newIndex < 0) newIndex = allMaps.size() - 1;

        currentMap.clear();
        currentMap = allMaps.get(newIndex);
        currentMapIndex = newIndex;
    }

    public void updateScreen(Area a) {
        drawArea.setSize(a.w(), a.h());

        if (viewPortW <= 0) viewPortW = a.w() / 128.0;

        viewPortH = viewPortW * drawArea.getHeight() / drawArea.getWidth();
    }

    public void onClose() {
        currentMap.clear();
    }

    public int getCurrentMapIndex() {
        return currentMapIndex;
    }
}
