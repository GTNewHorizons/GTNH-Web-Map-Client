package com.gtnewhorizons.gwmclient.client;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Point;

import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.gtnewhorizons.gwmclient.storage.GenericMap;
import com.gtnewhorizons.gwmclient.storage.RemoteConfiguration;
import com.gtnewhorizons.gwmclient.storage.RemoteMap;
import com.gtnewhorizons.gwmclient.storage.RemoteWorld;

public class ModularTest {

    public static ModularScreen createGUI() {
        var area = GuiContext.getDefault()
            .getScreenArea();
        MapDrawer drawer = new MapDrawer(area.w(), area.h());
        ModularPanel pnl = new MapPanel("Map", drawer).size(area.w(), area.h())
            .pos(0, 0);

        return new ModularScreen(pnl);
    }

    static class MapPanel extends ModularPanel {

        private final MapDrawer drawer;

        static ExecutorService backgroundLoader = Executors.newSingleThreadExecutor();
        RemoteConfiguration remoteConfig;

        boolean loadingMapList = true;
        int currentMapIndex = 0;

        ArrayList<GenericMap> maps = new ArrayList<>();

        GenericMap currentMap;
        boolean first = true;

        private void loadMapList() {

            remoteConfig = new RemoteConfiguration();
            remoteConfig.load();

            for (RemoteWorld rw : remoteConfig.getWorlds()) {
                for (RemoteMap rm : rw.getMaps()) {
                    maps.add(rm);
                }
            }

            if (maps.size() > 0) {
                currentMap = maps.get(0);
                drawer.setMap(currentMap);
            }

            loadingMapList = false;
        }

        public MapPanel(String name, MapDrawer drawer) {
            super(name);
            this.drawer = drawer;
            backgroundLoader.submit(() -> loadMapList());
        }

        Rectangle rect;

        @Override
        public void drawBackground(ModularGuiContext context, WidgetTheme widgetTheme) {
            if (rect == null) {
                rect = new Rectangle();
                rect.setColor(0xFF000000);
            }
            Area area = getArea();

            rect.draw(context, area.x, area.y, area.width, area.height);

            if (loadingMapList) return;

            if (first) {
                EntityClientPlayerMP thePlayer = Minecraft.getMinecraft().thePlayer;
                drawer.focusOnWorldPoint(new Point3d(thePlayer.posX, thePlayer.posY, thePlayer.posZ));
                first = false;
            }

            drawer.updateScreen(area);
            drawer.draw();

            TextRenderer renderer = TextRenderer.SHARED;
            renderer.setColor(-1);
            renderer.setAlignment(Alignment.CenterLeft, getArea().w() + 1, getArea().h());
            renderer.setShadow(widgetTheme.getTextShadow());
            renderer.setPos(getArea().getPadding().left, getArea().getPadding().top);
            renderer.setScale(1);
            renderer.setSimulate(false);

            Point mouse = getMousePos();
            Point2d mapCoord = drawer.pointToMapCoord(mouse);

            renderer.draw("Map: " + mapCoord + "\nWorld: " + currentMap.mapCoordToWorld(mapCoord));
        }

        @Override
        public void drawForeground(ModularGuiContext context) {
            super.drawForeground(context);
        }

        @Override
        public void onClose() {
            super.onClose();
            drawer.onClose();
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            drawer.handleMouseInput();
        }

        @Override
        public boolean onMouseDrag(int mouseButton, long timeSinceClick) {
            return super.onMouseDrag(mouseButton, timeSinceClick);
        }

        @Override
        public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
            Point currentMousePos = getMousePos();
            if (scrollDirection == ModularScreen.UpOrDown.DOWN) {
                drawer.zoomOut(currentMousePos);
            } else if (scrollDirection == ModularScreen.UpOrDown.UP) {
                drawer.zoomIn(currentMousePos);
            }
            return super.onMouseScroll(scrollDirection, amount);
        }

        private Point getMousePos() {
            Minecraft mc = Minecraft.getMinecraft();
            Area area = getArea();
            Point currentMousePos = new Point(
                (Mouse.getEventX() * area.w()) / mc.displayWidth,
                (Mouse.getEventY() * area.h()) / mc.displayHeight);
            return currentMousePos;
        }

        @Override
        public boolean onKeyPressed(char typedChar, int keyCode) {
            switch (keyCode) {
                case Keyboard.KEY_HOME:
                    currentMapIndex--;
                    if (currentMapIndex < 0) currentMapIndex = maps.size() - 1;

                    currentMap = maps.get(currentMapIndex);
                    drawer.setMap(currentMap);
                    break;
                case Keyboard.KEY_END:
                    currentMapIndex++;
                    if (currentMapIndex >= maps.size()) currentMapIndex = 0;

                    currentMap = maps.get(currentMapIndex);
                    drawer.setMap(currentMap);
                    break;
            }
            return super.onKeyPressed(typedChar, keyCode);
        }
    }
}
