package org.stupidcraft.linearpaper.region;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public interface IRegionFile extends AutoCloseable {
    Path getPath();
    void flush() throws IOException;
    void clear(ChunkPos pos) throws IOException;
    void close() throws IOException;
    void setOversized(int x, int z, boolean b) throws IOException;
    void write(ChunkPos pos, ByteBuffer buffer) throws IOException;

    boolean hasChunk(ChunkPos pos);
    boolean doesChunkExist(ChunkPos pos) throws Exception;
    boolean isOversized(int x, int z);
    boolean recalculateHeader() throws IOException;

    DataOutputStream getChunkDataOutputStream(ChunkPos pos) throws IOException;
    DataInputStream getChunkDataInputStream(ChunkPos pos) throws IOException;
    CompoundTag getOversizedData(int x, int z) throws IOException;
}
