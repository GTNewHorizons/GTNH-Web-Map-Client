package com.gtnewhorizons.gwmclient.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;
import org.lwjgl.util.Rectangle;

import com.gtnewhorizons.gwmclient.storage.PerMapTileDataBase;
import com.gtnewhorizons.gwmclient.storage.Tile;

public class MapScreen extends GuiScreen {

    static ExecutorService backgroundLoader = Executors.newSingleThreadExecutor();
    Rectangle drawArea = new Rectangle(0, 0, 100, 100);

    PerMapTileDataBase currentMap;
    int currentMapIndex = 0;

    ArrayList<PerMapTileDataBase> allMaps = new ArrayList<>();

    double viewPortX, viewPortY, viewPortW, viewPortH;

    boolean loadingMapList = true;

    public MapScreen() {

        backgroundLoader.submit(() -> loadMapList());

        viewPortX = viewPortY = 0;
        viewPortW = this.width / 128.0;
        viewPortH = this.height / 128.0;
    }

    private void loadMapList() {
        try {
            URL imageURL = new URL("http://127.0.0.1:8123/up/configuration");
            InputStream is = imageURL.openConnection()
                .getInputStream();

            JsonObject jsonObject = (JsonObject) Jsoner.deserialize(new InputStreamReader(is));

            JsonArray worlds = (JsonArray) jsonObject.get("worlds");

            for (Object obj : worlds) {
                if (obj instanceof JsonObject worldObj) {
                    String worldName = (String) worldObj.get("name");
                    String worldTitle = (String) worldObj.get("title");

                    JsonArray mapsArr = (JsonArray) worldObj.get("maps");

                    for (Object tmpMapObj : mapsArr) {
                        if (tmpMapObj instanceof JsonObject mapObj) {
                            String mapName = (String) mapObj.get("name");
                            String mapTitle = (String) mapObj.get("title");
                            String mapPrefix = (String) mapObj.get("prefix");

                            allMaps.add(new PerMapTileDataBase(worldName, mapPrefix, worldTitle + " - " + mapTitle));
                        }
                    }
                }
            }

            if (allMaps.size() > 0) {
                currentMap = allMaps.get(0);
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JsonException e) {
            throw new RuntimeException(e);
        }
        loadingMapList = false;
    }

    Tile getTile(int x, int y, int zoom) {

        return currentMap.getTileAt(x, y, zoom);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawBackground(0);

        if (currentMap == null) {
            if (loadingMapList) drawString(fontRendererObj, "Loading maps...", 10, 10, 0xFFFFFF);
            else drawString(fontRendererObj, "Failed to load maps!", 10, 10, 0xFF0000);
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

        drawString(fontRendererObj, currentMap.title, 10, 10, 0xFFFFFF);
    }

    Point lastMousePos;
    Point dragStartMousePos;
    double dragStartViewX, dragStartViewY;
    boolean dragging;

    @Override
    public void handleMouseInput() {
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

        if (wheel < 0) {
            viewPortW *= 2;
            viewPortH = viewPortW * drawArea.getHeight() / drawArea.getWidth();

            viewPortX -= viewPortW / 4;
            viewPortY -= viewPortH / 4;
        } else if (wheel > 0) {
            double mouseOverX = (currentMousePos.getX() - drawArea.getX()) * viewPortW / drawArea.getWidth()
                + viewPortX;
            double mouseOverY = (currentMousePos.getY() - drawArea.getY()) * viewPortH / drawArea.getHeight()
                + viewPortY;

            viewPortX += (mouseOverX - viewPortX) / 2;
            viewPortY += (mouseOverY - viewPortY) / 2;

            viewPortW /= 2;
            viewPortH = viewPortW * drawArea.getHeight() / drawArea.getWidth();
        }

        lastMousePos = currentMousePos;
    }

    @Override
    public void handleKeyboardInput() {
        super.handleKeyboardInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);

        switch (keyCode) {
            case Keyboard.KEY_HOME:
                setNewMapIndex(currentMapIndex - 1);
                break;
            case Keyboard.KEY_END:
                setNewMapIndex(currentMapIndex + 1);

                break;
        }

    }

    void setNewMapIndex(int newIndex) {
        if (newIndex >= allMaps.size()) newIndex = 0;
        if (newIndex < 0) newIndex = allMaps.size() - 1;

        currentMap.clear();
        currentMap = allMaps.get(newIndex);
        currentMapIndex = newIndex;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        drawArea.setSize(this.width, this.height);

        if (viewPortW <= 0) viewPortW = this.width / 128.0;

        viewPortH = viewPortW * drawArea.getHeight() / drawArea.getWidth();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        currentMap.clear();
    }
}
