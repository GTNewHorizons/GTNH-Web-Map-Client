package com.gtnewhorizons.gwmclient.client;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Point;

import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.sizer.Area;

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

        public MapPanel(@NotNull String name, MapDrawer drawer) {
            super(name);
            this.drawer = drawer;
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

            drawer.updateScreen(area);
            drawer.draw();
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
            Minecraft mc = Minecraft.getMinecraft();
            Area area = getArea();
            Point currentMousePos = new Point(
                (Mouse.getEventX() * area.w()) / mc.displayWidth,
                (Mouse.getEventY() * area.h()) / mc.displayHeight);
            if (scrollDirection == ModularScreen.UpOrDown.DOWN) {
                drawer.zoomOut(currentMousePos);
            } else if (scrollDirection == ModularScreen.UpOrDown.UP) {
                drawer.zoomIn(currentMousePos);
            }
            return super.onMouseScroll(scrollDirection, amount);
        }

        @Override
        public boolean onKeyPressed(char typedChar, int keyCode) {
            switch (keyCode) {
                case Keyboard.KEY_HOME:
                    drawer.setNewMapIndex(drawer.getCurrentMapIndex() - 1);
                    break;
                case Keyboard.KEY_END:
                    drawer.setNewMapIndex(drawer.getCurrentMapIndex() + 1);

                    break;
            }
            return super.onKeyPressed(typedChar, keyCode);
        }
    }
}
