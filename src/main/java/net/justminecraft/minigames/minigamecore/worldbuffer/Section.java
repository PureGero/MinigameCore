package net.justminecraft.minigames.minigamecore.worldbuffer;

import java.util.Arrays;

/**
 * It is recommended you do not use this class</br>
 * This class should only be used for advanced lighting
 *
 * @author PureGero
 */
public class Section {
    public final int y;
    public final Chunk chunk;
    public byte[] blocks = new byte[4096];
    public byte[] data = new byte[2048];
    public byte[] skylight = new byte[2048];
    public byte[] blocklight = new byte[2048];
    public Section(int y, Chunk c) {
        this.y = y;
        chunk = c;
        if (c.w.defaultBlockLight != 0)
            Arrays.fill(blocklight, (byte) (c.w.defaultBlockLight + (c.w.defaultBlockLight << 4)));
        if (c.w.defaultSkyLight != 0)
            Arrays.fill(skylight, (byte) (c.w.defaultSkyLight + (c.w.defaultSkyLight << 4)));
    }

    private static byte Nibble4(byte[] arr, int index) {
        return (byte) ((index & 1) == 0 ? arr[index >> 1] & 0x0F : (arr[index >> 1] >> 4) & 0x0F);
    }

    private static void Nibble4(byte[] arr, int index, byte value) {
        arr[index >> 1] = (byte) ((index & 1) == 0 ? ((arr[index >> 1] >> 4) << 4) + (value & 0x0F) : (arr[index >> 1] & 0x0F) + (value << 4));
    }

    public byte id(int x, int y, int z) {
        return blocks[(y << 8 | z << 4 | x)];
    }

    public byte dat(int x, int y, int z) {
        return Nibble4(data, (y << 8 | z << 4 | x));
    }

    public byte sl(int x, int y, int z) {
        return Nibble4(skylight, (y << 8 | z << 4 | x));
    }

    public byte bl(int x, int y, int z) {
        return Nibble4(blocklight, (y << 8 | z << 4 | x));
    }

    public void id(int x, int y, int z, byte b) {
        blocks[(y << 8 | z << 4 | x)] = b;
    }

    public void dat(int x, int y, int z, byte b) {
        Nibble4(data, (y << 8 | z << 4 | x), b);
    }

    public void sl(int x, int y, int z, byte b) {
        Nibble4(skylight, (y << 8 | z << 4 | x), b);
    }

    public void bl(int x, int y, int z, byte b) {
        Nibble4(blocklight, (y << 8 | z << 4 | x), b);
    }
}
