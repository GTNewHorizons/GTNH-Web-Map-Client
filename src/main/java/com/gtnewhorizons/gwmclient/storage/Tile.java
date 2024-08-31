package com.gtnewhorizons.gwmclient.storage;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import net.minecraft.client.renderer.texture.TextureUtil;

import org.lwjgl.opengl.GL11;

public class Tile {

    BufferedImage currentImage;
    BufferedImage newlyLoadedImage;

    int width, height;

    protected ByteBuffer buffer;

    public int textureId = -1;

    public void update() {
        BufferedImage tmpNewlyLoadedImage = newlyLoadedImage;

        if (tmpNewlyLoadedImage != null && currentImage != tmpNewlyLoadedImage) {
            updateTexture(tmpNewlyLoadedImage);
            currentImage = tmpNewlyLoadedImage;
        }
    }

    void updateTexture(BufferedImage tmpImage) {
        this.width = tmpImage.getWidth();
        this.height = tmpImage.getHeight();
        int bufferSize = width * height * 4; //

        if (buffer == null || (buffer.capacity() != bufferSize)) {
            buffer = ByteBuffer.allocateDirect(bufferSize);
        }
        buffer.clear();

        int[] pixels = new int[width * height];
        tmpImage.getRGB(0, 0, width, height, pixels, 0, width);
        int pixel;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green
                buffer.put((byte) ((pixel & 0xFF))); // Blue
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
            }
        }
        buffer.flip();
        buffer.rewind();

        if (textureId == -1) textureId = TextureUtil.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL11.GL_RGBA8,
            width,
            height,
            0,
            GL11.GL_RGBA,
            GL11.GL_UNSIGNED_BYTE,
            buffer);
    }

    public void delete() {
        if (textureId != -1) {
            TextureUtil.deleteTexture(textureId);
            textureId = -1;
            currentImage = newlyLoadedImage = null;
        }
    }

    public float umin = 0, umax = 1, vmin = 0, vmax = 1;
}
