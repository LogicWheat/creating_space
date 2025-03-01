package com.rae.creatingspace.content.life_support.sealer;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class RoomShape {
    ArrayList<AABB> listOfBox;
    boolean closed = true;
    float xRot;
    float yRot;
    float zRot;
    int volume;

    RoomShape(List<AABB> listOfBox) {
        this.listOfBox = new ArrayList<>(listOfBox);
        calculateVolume();
    }

    RoomShape(List<AABB> listOfBox, int volume) {
        this.listOfBox = new ArrayList<>(listOfBox);
        this.volume = volume;
    }

    RoomShape(List<AABB> listOfBox, int volume, boolean closed) {
        this(listOfBox, volume);
        this.closed = closed;
    }

    public static RoomShape fromNbt(CompoundTag nbt) {
        long[] serialized = nbt.getLongArray("listOfBox");
        ArrayList<AABB> listOfBox = new ArrayList<>(serialized.length / 2);
        for (int i = 0; i < serialized.length / 2; i++) {
            listOfBox.add(new AABB(BlockPos.of(serialized[i]), BlockPos.of(serialized[i + 1])));
        }
        return new RoomShape(listOfBox, nbt.getInt("volume"), nbt.getBoolean("closed"));
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ArrayList<Long> listOfPos = new ArrayList<>();
        for (AABB aabb : listOfBox) {
            listOfPos.add((new BlockPos(aabb.minX, aabb.minY, aabb.minZ)).asLong());
            listOfPos.add((new BlockPos(aabb.maxX, aabb.maxY, aabb.maxZ)).asLong());

        }
        tag.putLongArray("listOfBox", listOfPos);
        tag.putInt("volume", volume);
        tag.putBoolean("closed", closed);
        return tag;
    }

    public ArrayList<AABB> getListOfBox() {
        return listOfBox;
    }

    public void add(BlockPos pos) {
        AABB firstBlock = new AABB(pos);
        //expand the AABB to the frontier ? ( optimise will merge AABB)
        add(firstBlock);
    }

    //TODO use AABB::minmax
    public AABB getEncapsulatingBox() {
        Double minX = null;
        Double minY = null;
        Double minZ = null;
        Double maxX = null;
        Double maxY = null;
        Double maxZ = null;
        for (AABB aabb : listOfBox) {
            if (minX == null || minX > aabb.minX) {
                minX = aabb.minX;
            }
            if (minY == null || minY > aabb.minY) {
                minY = aabb.minY;
            }
            if (minZ == null || minZ > aabb.minZ) {
                minZ = aabb.minZ;
            }
            if (maxX == null || maxX < aabb.maxX) {
                maxX = aabb.maxX;
            }
            if (maxY == null || maxY < aabb.maxY) {
                maxY = aabb.maxY;
            }
            if (maxZ == null || maxZ < aabb.maxZ) {
                maxZ = aabb.maxZ;
            }
        }
        if (listOfBox.isEmpty()) {
            return null;
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    //TODO optimise
    public void addAll(List<BlockPos> posList) {
        for (BlockPos pos :
                posList) {
            add(pos);
        }
    }

    private void add(AABB aabbs) {
        add(List.of(aabbs));
        calculateVolume();
    }
    private void add(List<AABB> aabbs) {
        listOfBox.addAll(aabbs);
    }
    public List<Entity> getEntitiesInside(Level level) {
        ArrayList<Entity> entities = new ArrayList<>();
        for (AABB box :
                listOfBox) {
            entities.addAll(level.getEntities(null, box));
        }
        return entities.stream().distinct().toList();
    }

    public void calculateVolume() {
        double volume = 0;
        for (AABB aabb :
                listOfBox) {
            volume += getVolume(aabb);

        }
        this.volume = (int) volume;
    }

    public double getVolume(AABB aabb) {
        return (aabb.getXsize() * aabb.getYsize() * aabb.getZsize());
    }

    public double getVolume() {
        return volume;
    }
    public boolean inside(AABB colBox) {
        for (AABB box : listOfBox) {
            if (box.intersects(colBox)) return true;
        }
        return false;
    }

    public void remove(BlockPos pos) {
        remove(new AABB(pos));
    }

    public void remove(AABB toRemove) {

    }

    public void setClosed() {
        this.closed = true;
    }

    public void setOpen() {
        this.closed = false;
    }

    public boolean isClosed() {
        return closed;
    }

    private List<AABB> carve(AABB carver, AABB carved) {
        if (carved.intersects(carver)) {
            return List.of(carved);
        }
        AABB iCarver = carver.intersect(carved);
        ArrayList<AABB> aabbs = new ArrayList<>(6);
        aabbs.add(new AABB(carved.minX, carved.minY, carved.minZ, iCarver.minX, carved.maxY, carved.maxZ));
        aabbs.add(new AABB(iCarver.maxX, carved.minY, carved.minZ, carver.maxX, carved.maxY, carved.maxZ));

        aabbs.add(new AABB(iCarver.minX, carved.minY, carved.minZ, iCarver.maxX, carved.maxY, iCarver.minZ));
        aabbs.add(new AABB(iCarver.minX, carved.minY, iCarver.maxZ, iCarver.maxX, carved.maxY, carved.maxZ));

        aabbs.add(new AABB(iCarver.minX, carved.minY, iCarver.minZ, iCarver.maxX, iCarver.minY, iCarver.maxZ));
        aabbs.add(new AABB(iCarver.minX, iCarver.maxY, iCarver.minZ, iCarver.maxX, carved.maxY, iCarver.maxZ));

        return aabbs.stream().filter(aabb -> aabb.getSize() != 0).toList();
    }
}