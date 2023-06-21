package com.supermartijn642.wormhole.portal;

import com.google.common.collect.Lists;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.StabilizerBlockEntity;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.*;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
public class PortalShape {

    public static final Map<EnumFacing.Axis,List<BlockPos>> DIRECT_OFFSETS = new EnumMap<>(EnumFacing.Axis.class);
    private static final Map<EnumFacing.Axis,List<BlockPos>> INDIRECT_OFFSETS = new EnumMap<>(EnumFacing.Axis.class);
    private static final Map<EnumFacing.Axis,List<BlockPos>> ALL_OFFSETS = new EnumMap<>(EnumFacing.Axis.class);

    static{
        DIRECT_OFFSETS.put(EnumFacing.Axis.X, Lists.newArrayList(BlockPos.ORIGIN.up(), BlockPos.ORIGIN.down(), BlockPos.ORIGIN.north(), BlockPos.ORIGIN.south()));
        DIRECT_OFFSETS.put(EnumFacing.Axis.Y, Lists.newArrayList(BlockPos.ORIGIN.north(), BlockPos.ORIGIN.east(), BlockPos.ORIGIN.south(), BlockPos.ORIGIN.west()));
        DIRECT_OFFSETS.put(EnumFacing.Axis.Z, Lists.newArrayList(BlockPos.ORIGIN.up(), BlockPos.ORIGIN.down(), BlockPos.ORIGIN.east(), BlockPos.ORIGIN.west()));

        INDIRECT_OFFSETS.put(EnumFacing.Axis.X, Lists.newArrayList(BlockPos.ORIGIN.up().north(), BlockPos.ORIGIN.up().south(), BlockPos.ORIGIN.down().north(), BlockPos.ORIGIN.down().south()));
        INDIRECT_OFFSETS.put(EnumFacing.Axis.Y, Lists.newArrayList(BlockPos.ORIGIN.north().east(), BlockPos.ORIGIN.north().west(), BlockPos.ORIGIN.south().east(), BlockPos.ORIGIN.south().west()));
        INDIRECT_OFFSETS.put(EnumFacing.Axis.Z, Lists.newArrayList(BlockPos.ORIGIN.up().east(), BlockPos.ORIGIN.up().west(), BlockPos.ORIGIN.down().east(), BlockPos.ORIGIN.down().west()));

        for(EnumFacing.Axis axis : EnumFacing.Axis.values()){
            List<BlockPos> pos = new ArrayList<>();
            pos.addAll(DIRECT_OFFSETS.get(axis));
            pos.addAll(INDIRECT_OFFSETS.get(axis));
            ALL_OFFSETS.put(axis, pos);
        }
    }

    public static PortalShape find(IBlockAccess level, BlockPos center){
        for(EnumFacing.Axis axis : EnumFacing.Axis.values()){
            PortalShape shape = find(level, center, axis);
            if(shape != null)
                return shape;
        }
        return null;
    }

    private static PortalShape find(IBlockAccess level, BlockPos center, EnumFacing.Axis axis){
        for(BlockPos offset : ALL_OFFSETS.get(axis)){
            IBlockState offsetBlock = level.getBlockState(center.add(offset));
            if(offsetBlock.getBlock().isAir(offsetBlock, level, center.add(offset)) || offsetBlock.getBlock() == Blocks.WATER){
                PortalShape shape = findArea(level, center.add(offset), axis);
                if(shape != null)
                    return shape;
            }
        }
        return null;
    }

    private static PortalShape findArea(IBlockAccess level, BlockPos start, EnumFacing.Axis axis){
        List<BlockPos> next = new LinkedList<>();
        List<BlockPos> current = new LinkedList<>();
        current.add(start);
        List<BlockPos> done = new ArrayList<>();
        List<BlockPos> frame = new ArrayList<>();
        List<BlockPos> corners = new ArrayList<>();
        List<BlockPos> stabilizers = new ArrayList<>();
        List<BlockPos> energyCells = new ArrayList<>();
        List<BlockPos> targetCells = new ArrayList<>();
        while(!current.isEmpty()){
            for(BlockPos pos : current){
                int frames = 0;
                for(BlockPos offset : DIRECT_OFFSETS.get(axis)){
                    BlockPos offPos = pos.add(offset);
                    IBlockState state = level.getBlockState(offPos);
                    TileEntity entity = level.getTileEntity(offPos);
                    if(state.getBlock().isAir(state, level, offPos) || state.getBlock() == Blocks.WATER){
                        if(!done.contains(offPos) && !current.contains(offPos) && !next.contains(offPos))
                            next.add(offPos);
                    }else if(entity instanceof IPortalGroupEntity && !((IPortalGroupEntity)entity).hasGroup()){
                        if(!frame.contains(offPos)){
                            frame.add(offPos);
                            if(entity instanceof StabilizerBlockEntity)
                                stabilizers.add(offPos);
                            if(entity instanceof IEnergyCellEntity)
                                energyCells.add(offPos);
                            if(entity instanceof ITargetCellEntity)
                                targetCells.add(offPos);
                        }
                        frames++;
                    }else
                        return null;
                }
                if(frames >= 2)
                    corners.add(pos);
            }
            if(done.size() + current.size() + next.size() > WormholeConfig.maxPortalSize.get())
                return null;
            done.addAll(current);
            current.clear();
            current.addAll(next);
            next.clear();
        }

        if(WormholeConfig.requireCorners.get()){
            if(!validateCorners(level, done, frame, corners, stabilizers, energyCells, targetCells, axis))
                return null;
        }else
            collectCorners(level, done, frame, corners, stabilizers, energyCells, targetCells, axis);

        if(stabilizers.size() == 0)
            return null;

        return new PortalShape(axis, done, frame, stabilizers, energyCells, targetCells);
    }

    private static void collectCorners(IBlockAccess level, List<BlockPos> area, List<BlockPos> frame, List<BlockPos> corners, List<BlockPos> stabilizers, List<BlockPos> energyCells, List<BlockPos> targetCells, EnumFacing.Axis axis){
        BlockPos dir1pos = axis == EnumFacing.Axis.Y ? BlockPos.ORIGIN.east() : BlockPos.ORIGIN.up();
        BlockPos dir1neg = axis == EnumFacing.Axis.Y ? BlockPos.ORIGIN.west() : BlockPos.ORIGIN.down();
        BlockPos dir2pos = axis == EnumFacing.Axis.Z ? BlockPos.ORIGIN.east() : BlockPos.ORIGIN.north();
        BlockPos dir2neg = axis == EnumFacing.Axis.Z ? BlockPos.ORIGIN.west() : BlockPos.ORIGIN.south();
        for(BlockPos corner : corners){
            collectCorner(level, area, frame, stabilizers, energyCells, targetCells, corner, dir1pos, dir2pos);
            collectCorner(level, area, frame, stabilizers, energyCells, targetCells, corner, dir1pos, dir2neg);
            collectCorner(level, area, frame, stabilizers, energyCells, targetCells, corner, dir1neg, dir2pos);
            collectCorner(level, area, frame, stabilizers, energyCells, targetCells, corner, dir1neg, dir2neg);
        }
    }

    private static boolean validateCorners(IBlockAccess level, List<BlockPos> area, List<BlockPos> frame, List<BlockPos> corners, List<BlockPos> stabilizers, List<BlockPos> energyCells, List<BlockPos> targetCells, EnumFacing.Axis axis){
        BlockPos dir1pos = axis == EnumFacing.Axis.Y ? BlockPos.ORIGIN.east() : BlockPos.ORIGIN.up();
        BlockPos dir1neg = axis == EnumFacing.Axis.Y ? BlockPos.ORIGIN.west() : BlockPos.ORIGIN.down();
        BlockPos dir2pos = axis == EnumFacing.Axis.Z ? BlockPos.ORIGIN.east() : BlockPos.ORIGIN.north();
        BlockPos dir2neg = axis == EnumFacing.Axis.Z ? BlockPos.ORIGIN.west() : BlockPos.ORIGIN.south();
        for(BlockPos corner : corners){
            if(!collectCorner(level, area, frame, stabilizers, energyCells, targetCells, corner, dir1pos, dir2pos))
                return false;
            if(!collectCorner(level, area, frame, stabilizers, energyCells, targetCells, corner, dir1pos, dir2neg))
                return false;
            if(!collectCorner(level, area, frame, stabilizers, energyCells, targetCells, corner, dir1neg, dir2pos))
                return false;
            if(!collectCorner(level, area, frame, stabilizers, energyCells, targetCells, corner, dir1neg, dir2neg))
                return false;
        }
        return true;
    }

    private static boolean collectCorner(IBlockAccess level, List<BlockPos> area, List<BlockPos> frame, List<BlockPos> stabilizers, List<BlockPos> energyCells, List<BlockPos> targetCells, BlockPos corner, BlockPos dir1, BlockPos dir2){
        if(frame.contains(corner.add(dir1)) && frame.contains(corner.add(dir2))){
            BlockPos pos = corner.add(dir1).add(dir2);
            TileEntity entity = level.getTileEntity(pos);
            if(entity instanceof IPortalGroupEntity ? ((IPortalGroupEntity)entity).hasGroup() : !area.contains(pos))
                return false;
            else if(!frame.contains(pos)){
                frame.add(pos);
                if(entity instanceof StabilizerBlockEntity)
                    stabilizers.add(pos);
                if(entity instanceof IEnergyCellEntity)
                    energyCells.add(pos);
                if(entity instanceof ITargetCellEntity)
                    targetCells.add(pos);
            }
        }
        return true;
    }

    public final EnumFacing.Axis axis;

    public final List<BlockPos> area = new ArrayList<>();
    public final List<BlockPos> frame = new ArrayList<>();
    public final List<BlockPos> stabilizers = new ArrayList<>();
    public final List<BlockPos> energyCells = new ArrayList<>();
    public final List<BlockPos> targetCells = new ArrayList<>();

    /**
     * The distance the portal stretches
     */
    public final double span;
    /**
     * The lowest xyz corner, not necessarily part of the portal
     */
    public final BlockPos minCorner;
    /**
     * The highest xyz corner, not necessarily part of the portal
     */
    public final BlockPos maxCorner;

    public PortalShape(EnumFacing.Axis axis, List<BlockPos> area, List<BlockPos> frame, List<BlockPos> stabilizers, List<BlockPos> energyCells, List<BlockPos> targetCells){
        this.axis = axis;
        this.area.addAll(area);
        this.frame.addAll(frame);
        this.stabilizers.addAll(stabilizers);
        this.energyCells.addAll(energyCells);
        this.targetCells.addAll(targetCells);

        double span = 0;
        int minX = frame.get(0).getX(), minY = frame.get(0).getY(), minZ = frame.get(0).getZ();
        int maxX = frame.get(0).getX(), maxY = frame.get(0).getY(), maxZ = frame.get(0).getZ();
        for(int i = 0; i < frame.size(); i++){
            BlockPos pos1 = frame.get(i);
            for(int j = i + 1; j < frame.size(); j++){
                BlockPos pos2 = frame.get(j);
                double distance = pos1.distanceSq(pos2);
                if(distance > span)
                    span = distance;
            }
            if(pos1.getX() < minX)
                minX = pos1.getX();
            if(pos1.getY() < minY)
                minY = pos1.getY();
            if(pos1.getZ() < minZ)
                minZ = pos1.getZ();
            if(pos1.getX() > maxX)
                maxX = pos1.getX();
            if(pos1.getY() > maxY)
                maxY = pos1.getY();
            if(pos1.getZ() > maxZ)
                maxZ = pos1.getZ();
        }
        this.span = Math.sqrt((maxX - minX + 1) * (maxX - minX + 1) + (maxY - minY + 1) * (maxY - minY + 1) + (maxZ - minZ + 1) * (maxZ - minZ + 1));
        this.minCorner = new BlockPos(minX, minY, minZ);
        this.maxCorner = new BlockPos(maxX, maxY, maxZ);
    }

    public PortalShape(NBTTagCompound tag){
        this.axis = Enum.valueOf(EnumFacing.Axis.class, tag.getString("axis"));

        NBTTagCompound areaTag = tag.getCompoundTag("area");
        for(String key : areaTag.getKeySet()){
            NBTTagCompound pos = areaTag.getCompoundTag(key);
            this.area.add(new BlockPos(pos.getInteger("x"), pos.getInteger("y"), pos.getInteger("z")));
        }

        NBTTagCompound frameTag = tag.getCompoundTag("frame");
        for(String key : frameTag.getKeySet()){
            NBTTagCompound pos = frameTag.getCompoundTag(key);
            this.frame.add(new BlockPos(pos.getInteger("x"), pos.getInteger("y"), pos.getInteger("z")));
        }

        NBTTagCompound stabilizerTag = tag.getCompoundTag("stabilizers");
        for(String key : stabilizerTag.getKeySet()){
            NBTTagCompound pos = stabilizerTag.getCompoundTag(key);
            this.stabilizers.add(new BlockPos(pos.getInteger("x"), pos.getInteger("y"), pos.getInteger("z")));
        }

        NBTTagCompound energyCellsTag = tag.getCompoundTag("energyCells");
        for(String key : energyCellsTag.getKeySet()){
            NBTTagCompound pos = energyCellsTag.getCompoundTag(key);
            this.energyCells.add(new BlockPos(pos.getInteger("x"), pos.getInteger("y"), pos.getInteger("z")));
        }

        NBTTagCompound targetCellsTag = tag.getCompoundTag("targetCells");
        for(String key : targetCellsTag.getKeySet()){
            NBTTagCompound pos = targetCellsTag.getCompoundTag(key);
            this.targetCells.add(new BlockPos(pos.getInteger("x"), pos.getInteger("y"), pos.getInteger("z")));
        }

        this.span = tag.getDouble("span");

        this.minCorner = new BlockPos(tag.getInteger("minCornerX"), tag.getInteger("minCornerY"), tag.getInteger("minCornerZ"));
        this.maxCorner = new BlockPos(tag.getInteger("maxCornerX"), tag.getInteger("maxCornerY"), tag.getInteger("maxCornerZ"));
    }

    public void createPortals(World level, EnumDyeColor color){
        if(color == null)
            color = EnumDyeColor.values()[new Random().nextInt(EnumDyeColor.values().length)];
        PortalBlock portalBlock = this.axis == EnumFacing.Axis.X ? Wormhole.portal_x : this.axis == EnumFacing.Axis.Y ? Wormhole.portal_y : Wormhole.portal_z;
        for(BlockPos pos : this.area){
            IBlockState state = level.getBlockState(pos);
            if(!(state.getBlock() instanceof PortalBlock) || state.getBlock() != portalBlock || state.getValue(PortalBlock.COLOR_PROPERTY) != color)
                level.setBlockState(pos, portalBlock.getDefaultState().withProperty(PortalBlock.COLOR_PROPERTY, color));
        }
    }

    public void destroyPortals(World world){
        for(BlockPos pos : this.area){
            IBlockState state = world.getBlockState(pos);
            if(state.getBlock() instanceof PortalBlock)
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    public boolean validateFrame(IBlockAccess world){
        for(BlockPos pos : this.frame){
            if(!(world.getBlockState(pos).getBlock() instanceof IPortalGroupEntity))
                return false;
        }
        return true;
    }

    public boolean validatePortal(IBlockAccess world){
        PortalBlock portalBlock = this.axis == EnumFacing.Axis.X ? Wormhole.portal_x : this.axis == EnumFacing.Axis.Y ? Wormhole.portal_y : Wormhole.portal_z;
        for(BlockPos pos : this.area){
            IBlockState state = world.getBlockState(pos);
            if(state.getBlock() != portalBlock && !state.getBlock().isAir(state, world, pos) && state.getBlock() != Blocks.WATER)
                return false;
        }
        return true;
    }

    public NBTTagCompound write(){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("axis", this.axis.name());

        NBTTagCompound areaTag = new NBTTagCompound();
        for(int i = 0; i < this.area.size(); i++){
            NBTTagCompound pos = new NBTTagCompound();
            pos.setInteger("x", this.area.get(i).getX());
            pos.setInteger("y", this.area.get(i).getY());
            pos.setInteger("z", this.area.get(i).getZ());
            areaTag.setTag("" + i, pos);
        }
        tag.setTag("area", areaTag);

        NBTTagCompound frameTag = new NBTTagCompound();
        for(int i = 0; i < this.frame.size(); i++){
            NBTTagCompound pos = new NBTTagCompound();
            pos.setInteger("x", this.frame.get(i).getX());
            pos.setInteger("y", this.frame.get(i).getY());
            pos.setInteger("z", this.frame.get(i).getZ());
            frameTag.setTag("" + i, pos);
        }
        tag.setTag("frame", frameTag);

        NBTTagCompound stabilizerTag = new NBTTagCompound();
        for(int i = 0; i < this.stabilizers.size(); i++){
            NBTTagCompound pos = new NBTTagCompound();
            pos.setInteger("x", this.stabilizers.get(i).getX());
            pos.setInteger("y", this.stabilizers.get(i).getY());
            pos.setInteger("z", this.stabilizers.get(i).getZ());
            stabilizerTag.setTag("" + i, pos);
        }
        tag.setTag("stabilizers", stabilizerTag);

        NBTTagCompound energyCellsTag = new NBTTagCompound();
        for(int i = 0; i < this.energyCells.size(); i++){
            NBTTagCompound pos = new NBTTagCompound();
            pos.setInteger("x", this.energyCells.get(i).getX());
            pos.setInteger("y", this.energyCells.get(i).getY());
            pos.setInteger("z", this.energyCells.get(i).getZ());
            energyCellsTag.setTag("" + i, pos);
        }
        tag.setTag("energyCells", energyCellsTag);

        NBTTagCompound targetCellsTag = new NBTTagCompound();
        for(int i = 0; i < this.targetCells.size(); i++){
            NBTTagCompound pos = new NBTTagCompound();
            pos.setInteger("x", this.targetCells.get(i).getX());
            pos.setInteger("y", this.targetCells.get(i).getY());
            pos.setInteger("z", this.targetCells.get(i).getZ());
            targetCellsTag.setTag("" + i, pos);
        }
        tag.setTag("targetCells", targetCellsTag);

        tag.setDouble("span", this.span);

        tag.setInteger("minCornerX", this.minCorner.getX());
        tag.setInteger("minCornerY", this.minCorner.getY());
        tag.setInteger("minCornerZ", this.minCorner.getZ());

        tag.setInteger("maxCornerX", this.maxCorner.getX());
        tag.setInteger("maxCornerY", this.maxCorner.getY());
        tag.setInteger("maxCornerZ", this.maxCorner.getZ());

        return tag;
    }

    public static PortalShape read(NBTTagCompound tag){
        return new PortalShape(tag);
    }
}
