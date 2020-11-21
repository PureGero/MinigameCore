package net.justminecraft.minigames.minigamecore.worldbuffer;

import com.sk89q.jnbt.*;
import net.minecraft.server.v1_8_R3.RegionFileCache;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * A basic API that allows for fast world modifications on unloaded chunks
 *
 * @author PureGero
 */
public class WorldBuffer {

    public byte defaultBlockLight = 0x0;
    public byte defaultSkyLight = 0xF;

    public File worldFolder;
    public ArrayList<Chunk> chunks = new ArrayList<>();

    public WorldBuffer(File worldFolder) {
        this.worldFolder = worldFolder;
    }

    private static byte Nibble4(byte[] arr, int index) {
        return (byte) ((index & 1) == 0 ? arr[index >> 1] & 0x0F : (arr[index >> 1] >> 4) & 0x0F);
    }

    private static void Nibble4(byte[] arr, int index, byte value) {
        arr[index >> 1] = (byte) ((index & 1) == 0 ? ((arr[index >> 1] >> 4) << 4) + (value & 0x0F) : (arr[index >> 1] & 0x0F) + (value << 4));
    }

    /**
     * Get child tag of a NBT structure.
     *
     * @param items    The parent tag map
     * @param key      The name of the tag to get
     * @param expected The expected type of the tag
     * @return child tag casted to the expected type
     * @throws IOException if the tag does not exist or the tag is not of the expected type
     */
    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws IOException {
        if (!items.containsKey(key)) {
            throw new IOException("Schematic file is missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IOException(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }

    public Chunk getChunkAt(int x, int z) {
        for (int i = 0; i < chunks.size(); i++)
            if (chunks.get(i).x == x && chunks.get(i).y == z)
                return chunks.get(i);
        return null;
    }

    public void blankChunk(int x, int z) {
        Chunk c = getChunkAt(x, z);
        if (c == null) new Chunk(x, z, this);
    }

    public void setBlockAt(Location l, Material m) {
        setBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ(), m.getId());
    }

    public void setBlockAt(int x, int y, int z, int id) {
        Chunk c = getChunkAt(x / 16, z / 16);
        if (c == null) c = new Chunk(x / 16, z / 16, this);
        c.setBlockId(x & 15, y, z & 15, id);
    }

    public void setBlockAt(int x, int y, int z, int id, byte dat) {
        Chunk c = getChunkAt(x / 16, z / 16);
        if (c == null) c = new Chunk(x / 16, z / 16, this);
        c.setBlockIdAndData(x & 15, y, z & 15, id, dat);
    }

    /**
     * @return The block id of the block at {@code x,y,z}
     */
    public int getBlockAt(int x, int y, int z) {
        Chunk c = getChunkAt(x / 16, z / 16);
        if (c == null) return 0;
        return c.getBlockId(x & 15, y, z & 15);
    }

    public int getBlockAt(Location l) {
        return getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public HashMap<Material, ArrayList<Location>> placeSchematic(Location l, File schem, Material... search) {
        return placeSchematic(l.getBlockX(), l.getBlockY(), l.getBlockZ(), schem, search);
    }

    public HashMap<Material, ArrayList<Location>> placeSchematic(int x, int y, int z, File schem, Material... search) {
        HashMap<Material, ArrayList<Location>> l = new HashMap<>();
        for (int i = 0; i < search.length; i++)
            l.put(search[i], new ArrayList<>());
        try {
            // Based off https://github.com/sk89q/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/schematic/MCEditSchematicFormat.java
            NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(new FileInputStream(schem)));
            NamedTag rootTag = nbtStream.readNamedTag();
            nbtStream.close();
            if (!rootTag.getName().equals("Schematic")) {
                throw new IOException("Tag \"Schematic\" does not exist or is not first");
            }
            CompoundTag schematicTag = (CompoundTag) rootTag.getTag();

            Map<String, Tag> schematic = schematicTag.getValue();
            if (!schematic.containsKey("Blocks")) {
                throw new IOException("Schematic file is missing a \"Blocks\" tag");
            }
            short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
            short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
            short height = getChildTag(schematic, "Height", ShortTag.class).getValue();

            int ox = getChildTag(schematic, "WEOffsetX", IntTag.class).getValue();
            int oy = getChildTag(schematic, "WEOffsetY", IntTag.class).getValue();
            int oz = getChildTag(schematic, "WEOffsetZ", IntTag.class).getValue();

            byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
            byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
            for (int j = 0; j < height; j++)
                for (int k = 0; k < length; k++)
                    for (int i = 0; i < width; i++) {
                        byte id = blockId[((j * length) + k) * width + i];
                        setBlockAt(x + i + ox, y + j + oy, z + k + oz,
                                id, blockData[((j * length) + k) * width + i]);
                        for (int a = 0; a < search.length; a++)
                            if (id == search[a].getId())
                                l.get(search[a]).add(new Location(null, x + i + ox, y + j + oy, z + k + oz));
                    }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return l;
    }

    /**
     * Saves the modified chunks to the world file
     */
    public void save() {
        for (int i = 0; i < chunks.size(); i++) {
            Chunk c = chunks.get(i);
            try (DataOutputStream out = RegionFileCache.d(new File(worldFolder, "region"), c.x, c.y)) {
                out.write(c.compileOutput());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
