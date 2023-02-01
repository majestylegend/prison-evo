package net.stickmix.prisonevo.utils;

import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Cuboid implements Iterable<Block> {

    private final World world;
    private BlockVector3 minPoint;
    private BlockVector3 maxPoint;

    private Cuboid(World world, BlockVector3 minPoint, BlockVector3 maxPoint) {
        this.world = world;
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
    }

    public static Cuboid exact(World world, BlockVector3 minPoint, BlockVector3 maxPoint) {
        Preconditions.checkNotNull(world, "world");
        Preconditions.checkNotNull(minPoint, "minPoint");
        Preconditions.checkNotNull(maxPoint, "maxPoint");

        return new Cuboid(world, minPoint, maxPoint);
    }

    public static Cuboid fromWorldAndPoints(World world, BlockVector3 firstPoint, BlockVector3 secondPoint) {
        Preconditions.checkNotNull(world, "world");
        Preconditions.checkNotNull(firstPoint, "firstPoint");
        Preconditions.checkNotNull(secondPoint, "secondPoint");

        return new Cuboid(world, firstPoint.getMinimum(secondPoint), firstPoint.getMaximum(secondPoint));
    }

    public static Cuboid fromWorldAndCoordinates(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        return fromWorldAndPoints(world, BlockVector3.at(x1, y1, z1), BlockVector3.at(x2, y2, z2));
    }

    public static Cuboid fromPointLocations(Location firstPoint, Location secondPoint) {
        Preconditions.checkNotNull(firstPoint, "firstPoint");
        Preconditions.checkNotNull(secondPoint, "secondPoint");
        Preconditions.checkArgument(firstPoint.getWorld() == secondPoint.getWorld(), "Locations are in different worlds");

        return fromWorldAndPoints(firstPoint.getWorld(), BlockVector3.atLocation(firstPoint), BlockVector3.atLocation(secondPoint));
    }

    public World getWorld() {
        return world;
    }

    public BlockVector3 getMinPoint() {
        return minPoint;
    }

    public void setMinPoint(BlockVector3 minPoint) {
        Preconditions.checkNotNull(minPoint, "minPoint");
        this.minPoint = minPoint;
    }

    public BlockVector3 getMaxPoint() {
        return maxPoint;
    }

    public void setMaxPoint(BlockVector3 maxPoint) {
        Preconditions.checkNotNull(maxPoint, "maxPoint");
        this.maxPoint = maxPoint;
    }

    public boolean contains(Location location) {
        Preconditions.checkNotNull(location, "location");
        return world == location.getWorld() && contains(location.getX(), location.getY(), location.getZ());
    }

    public boolean contains(World world, BlockVector3 point) {
        Preconditions.checkNotNull(world, "world");
        return this.world == world && contains(point);
    }

    public boolean contains(BlockVector3 point) {
        Preconditions.checkNotNull(point, "point");
        return point.containedWithin(minPoint, maxPoint);
    }

    public boolean contains(double x, double y, double z) {
        return x >= minPoint.getX() && x <= maxPoint.getX() && y >= minPoint.getY() && y <= maxPoint.getY() && z >= minPoint.getZ() && z <= maxPoint.getZ();
    }

    public boolean contains(Block block) {
        Preconditions.checkNotNull(block, "block");
        return world == block.getWorld() && contains(BlockVector3.atBlock(block));
    }

    public boolean contains(Entity entity) {
        Preconditions.checkNotNull(entity, "entity");
        return contains(entity.getLocation());
    }

    public Location getCenter() {
        return new Location(world, (minPoint.getX() + maxPoint.getX()) / 2.0, (minPoint.getY() + maxPoint.getY()) / 2.0, (minPoint.getZ() + maxPoint.getZ()) / 2.0);
    }

    public int getSizeX() {
        return (maxPoint.getX() - minPoint.getX()) + 1;
    }

    public int getSizeY() {
        return (maxPoint.getY() - minPoint.getY()) + 1;
    }

    public int getSizeZ() {
        return (maxPoint.getZ() - minPoint.getZ()) + 1;
    }

    public boolean intersectsWith(Cuboid other) {
        Preconditions.checkNotNull(other, "other cuboid");
        if (this.world != other.world) {
            return false;
        }

        return !(this.maxPoint.hasAnySmallerCoordinate(other.getMinPoint()) || this.minPoint.hasAnyBiggerCoordinate(other.getMaxPoint()));
    }

    public Stream<Block> blockStream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, minPoint, maxPoint);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Cuboid)) return false;
        final Cuboid other = (Cuboid) obj;
        return world.equals(other.world) &&
                minPoint.equals(other.minPoint) &&
                maxPoint.equals(other.maxPoint);
    }

    @Override
    public String toString() {
        return "Cuboid{" +
                "world=" + world.getName() +
                ", minPoint=" + minPoint +
                ", maxPoint=" + maxPoint +
                '}';
    }

    @Override
    @Nonnull
    public Iterator<Block> iterator() {
        return new CuboidIterator(world, minPoint, getSizeX(), getSizeY(), getSizeZ());
    }

    public List<Chunk> getChunks() {
        return createChunkData(world::getChunkAt);
    }

    private <T> List<T> createChunkData(ChunkDataFunction<T> chunkDataFunction) {
        int chunkXMin = minPoint.getX() >> 4, chunkZMin = minPoint.getZ() >> 4;
        int chunkXMax = maxPoint.getX() >> 4, chunkZMax = maxPoint.getZ() >> 4;
        List<T> result = new ArrayList<>((chunkXMax - chunkXMin) * (chunkZMax - chunkZMin));

        for (int x = chunkXMin; x <= chunkXMax; x++) {
            for (int z = chunkZMin; z <= chunkZMax; z++) {
                result.add(chunkDataFunction.apply(x, z));
            }
        }

        return result;
    }

    @FunctionalInterface
    private interface ChunkDataFunction<T> {

        T apply(int x, int z);

    }

    private static class CuboidIterator implements Iterator<Block> {

        private final World world;
        private final int baseX;
        private final int baseY;
        private final int baseZ;
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;
        private int x = 0, y = 0, z = 0;

        private CuboidIterator(World world, BlockVector3 minPoint, int sizeX, int sizeY, int sizeZ) {
            this.world = world;
            baseX = minPoint.getX();
            baseY = minPoint.getY();
            baseZ = minPoint.getZ();
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        @Override
        public boolean hasNext() {
            return x < sizeX && y < sizeY && z < sizeZ;
        }

        @Override
        public Block next() {
            Block block = world.getBlockAt(baseX + x, baseY + y, baseZ + z);
            if (++x >= sizeX) {
                x = 0;
                if (++y >= sizeY) {
                    y = 0;
                    ++z;
                }
            }
            return block;
        }

    }

}