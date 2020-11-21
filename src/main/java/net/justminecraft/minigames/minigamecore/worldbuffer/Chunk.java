package net.justminecraft.minigames.minigamecore.worldbuffer;

import net.minecraft.server.v1_8_R3.*;

import java.io.*;
import java.util.zip.DeflaterOutputStream;

public class Chunk {
    public final WorldBuffer w;
    public Section[] sections = new Section[16];
    public int x;
    public int y;
    public byte[] biomes = new byte[256];

    public Chunk(int x, int y, WorldBuffer w) {
        this.x = x;
        this.y = y;
        this.w = w;
        w.chunks.add(this);
        for (int i = 0; i < 256; i++)
            biomes[i] = 1; // Default to plains
    }

    public Section createSection(int y) {
        Section s = sections[y];
        if (s == null) {
            s = new Section(y, this);
            sections[y] = s;
        }
        return s;
    }

    public int getBlockId(int x, int y, int z) {
        Section s = sections[y / 16];
        if (s == null) return 0;
        else return s.id(x, y & 15, z);
    }

    public byte getBlockData(int x, int y, int z) {
        Section s = sections[y / 16];
        if (s == null) return 0;
        else return s.dat(x, y & 15, z);
    }

    public void setBlockId(int x, int y, int z, int id) {
        Section s = sections[y / 16];
        if (s == null) {
            s = new Section(y / 16, this);
            sections[y / 16] = s;
        }
        s.id(x, y & 15, z, (byte) id);
    }

    public void setBlockIdAndData(int x, int y, int z, int id, int data) {
        Section s = sections[y / 16];
        if (s == null) {
            s = new Section(y / 16, this);
            sections[y / 16] = s;
        }
        s.id(x, y & 15, z, (byte) id);
        s.dat(x, y & 15, z, (byte) data);
    }

    /**
     * @return the bytes that would be used to load this chunk from a file
     */
    public byte[] compileOutput() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NBTTagCompound t = compile();
        DataOutputStream d = new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(baos)));
        try {
            NBTCompressedStreamTools.a(t, (DataOutput) d);
            d.flush();
            d.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    /**
     * Writes the chunk data to an {@code OutputStream} object
     */
    public NBTTagCompound compile(/*DataOutputStream o*/) {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound level = new NBTTagCompound();
        level.setInt("xPos", x);
        level.setInt("zPos", y);
        level.setLong("LastUpdate", 0);
        level.setLong("InhabitedTime", 0);
        level.setBoolean("LightPopulated", true);
        level.setBoolean("TerrainPopulated", true);
        level.setByteArray("Biomes", biomes);
        //level.setByte("V", (byte) 1);
        int[] heightmap = new int[256];
        for (int z = 0; z < 16; z++)
            for (int x = 0; x < 16; x++) {
                for (int y = 255; y > 0; y--)
                    if (getBlockId(x, y, z) != 0) {
                        heightmap[(z << 4) + x] = y + 1;
                        break;
                    }
            }
        //System.out.println(Arrays.toString(heightmap));
        level.setIntArray("HeightMap", heightmap);
        NBTTagList Sections = new NBTTagList();
        for (Section s : sections) {
            if (s != null) {
                NBTTagCompound a = new NBTTagCompound();
                a.setByte("Y", (byte) s.y);
                a.setByteArray("Blocks", s.blocks);
				/*System.out.println();
				System.out.println(x);
				System.out.println(y);
				System.out.println(s.y);
				System.out.println(s.blocks.length);
				for(int i=0;i<s.blocks.length;i++){
					if(s.blocks[i] != 0)System.out.println((i&15) + "," + (i/256 + s.y*16) + "," + (i/16&15) + " = " + (s.blocks[i]&0xFF));
				}*/
                a.setByteArray("Data", s.data);
                a.setByteArray("BlockLight", s.blocklight);
                a.setByteArray("SkyLight", s.skylight);
                Sections.add(a);
            }
        }
        level.set("Sections", Sections);
        level.set("Entities", new NBTTagList());
        level.set("TileEntities", new NBTTagList());
        root.set("Level", level);
        root.setInt("DataVersion", 819);

        return root;
    }
}
