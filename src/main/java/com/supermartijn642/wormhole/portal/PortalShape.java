package com.supermartijn642.wormhole.portal;

import com.google.common.collect.Lists;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.StabilizerBlockEntity;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
public class PortalShape {

    public static final Map<Direction.Axis,List<BlockPos>> DIRECT_OFFSETS = new EnumMap<>(Direction.Axis.class);
    private static final Map<Direction.Axis,List<BlockPos>> INDIRECT_OFFSETS = new EnumMap<>(Direction.Axis.class);
    private static final Map<Direction.Axis,List<BlockPos>> ALL_OFFSETS = new EnumMap<>(Direction.Axis.class);

    static{
        DIRECT_OFFSETS.put(Direction.Axis.X, Lists.newArrayList(BlockPos.ZERO.above(), BlockPos.ZERO.below(), BlockPos.ZERO.north(), BlockPos.ZERO.south()));
        DIRECT_OFFSETS.put(Direction.Axis.Y, Lists.newArrayList(BlockPos.ZERO.north(), BlockPos.ZERO.east(), BlockPos.ZERO.south(), BlockPos.ZERO.west()));
        DIRECT_OFFSETS.put(Direction.Axis.Z, Lists.newArrayList(BlockPos.ZERO.above(), BlockPos.ZERO.below(), BlockPos.ZERO.east(), BlockPos.ZERO.west()));

        INDIRECT_OFFSETS.put(Direction.Axis.X, Lists.newArrayList(BlockPos.ZERO.above().north(), BlockPos.ZERO.above().south(), BlockPos.ZERO.below().north(), BlockPos.ZERO.below().south()));
        INDIRECT_OFFSETS.put(Direction.Axis.Y, Lists.newArrayList(BlockPos.ZERO.north().east(), BlockPos.ZERO.north().west(), BlockPos.ZERO.south().east(), BlockPos.ZERO.south().west()));
        INDIRECT_OFFSETS.put(Direction.Axis.Z, Lists.newArrayList(BlockPos.ZERO.above().east(), BlockPos.ZERO.above().west(), BlockPos.ZERO.below().east(), BlockPos.ZERO.below().west()));

        for(Direction.Axis axis : Direction.Axis.values()){
            List<BlockPos> pos = new ArrayList<>();
            pos.addAll(DIRECT_OFFSETS.get(axis));
            pos.addAll(INDIRECT_OFFSETS.get(axis));
            ALL_OFFSETS.put(axis, pos);
        }
    }

    public static PortalShape find(IBlockReader level, BlockPos center){
        for(Direction.Axis axis : Direction.Axis.values()){
            PortalShape shape = find(level, center, axis);
            if(shape != null)
                return shape;
        }
        return null;
    }

    private static PortalShape find(IBlockReader level, BlockPos center, Direction.Axis axis){
        for(BlockPos offset : ALL_OFFSETS.get(axis)){
            Block offsetBlock = level.getBlockState(center.offset(offset)).getBlock();
            if(offsetBlock.defaultBlockState().isAir() || offsetBlock == Blocks.WATER){
                PortalShape shape = findArea(level, center.offset(offset), axis);
                if(shape != null)
                    return shape;
            }
        }
        return null;
    }

    private static PortalShape findArea(IBlockReader level, BlockPos start, Direction.Axis axis){
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
                    BlockPos offPos = pos.offset(offset);
                    BlockState state = level.getBlockState(offPos);
                    TileEntity entity = level.getBlockEntity(offPos);
                    if(state.isAir() || state.getBlock() == Blocks.WATER){
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

    private static void collectCorners(IBlockReader level, List<BlockPos> area, List<BlockPos> frame, List<BlockPos> corners, List<BlockPos> stabilizers, List<BlockPos> energyCells, List<BlockPos> targetCells, Direction.Axis axis){
        BlockPos dir1pos = axis == Direction.Axis.Y ? BlockPos.ZERO.east() : BlockPos.ZERO.above();
        BlockPos dir1neg = axis == Direction.Axis.Y ? BlockPos.ZERO.west() : BlockPos.ZERO.below();
        BlockPos dir2pos = axis == Direction.Axis.Z ? BlockPos.ZERO.east() : BlockPos.ZERO.north();
        BlockPos dir2neg = axis == Direction.Axis.Z ? BlockPos.ZERO.west() : BlockPos.ZERO.south();
        for(BlockPos corner : corners){
            collectCorner(level, area, frame, stabilizers, energyCells, targetCells, corner, dir1pos, dir2pos);
            collectCorner(level, area, frame, stabilizers, energyCells, targetCells, corner, dir1pos, dir2neg);
            collectCorner(level, area, frame, stabilizers, energyCells, targetCells, corner, dir1neg, dir2pos);
            collectCorner(level, area, frame, stabilizers, energyCells, targetCells, corner, dir1neg, dir2neg);
        }
    }

    private static boolean validateCorners(IBlockReader level, List<BlockPos> area, List<BlockPos> frame, List<BlockPos> corners, List<BlockPos> stabilizers, List<BlockPos> energyCells, List<BlockPos> targetCells, Direction.Axis axis){
        BlockPos dir1pos = axis == Direction.Axis.Y ? BlockPos.ZERO.east() : BlockPos.ZERO.above();
        BlockPos dir1neg = axis == Direction.Axis.Y ? BlockPos.ZERO.west() : BlockPos.ZERO.below();
        BlockPos dir2pos = axis == Direction.Axis.Z ? BlockPos.ZERO.east() : BlockPos.ZERO.north();
        BlockPos dir2neg = axis == Direction.Axis.Z ? BlockPos.ZERO.west() : BlockPos.ZERO.south();
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

    private static boolean collectCorner(IBlockReader level, List<BlockPos> area, List<BlockPos> frame, List<BlockPos> stabilizers, List<BlockPos> energyCells, List<BlockPos> targetCells, BlockPos corner, BlockPos dir1, BlockPos dir2){
        if(frame.contains(corner.offset(dir1)) && frame.contains(corner.offset(dir2))){
            BlockPos pos = corner.offset(dir1).offset(dir2);
            TileEntity entity = level.getBlockEntity(pos);
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

    public final Direction.Axis axis;

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

    public PortalShape(Direction.Axis axis, List<BlockPos> area, List<BlockPos> frame, List<BlockPos> stabilizers, List<BlockPos> energyCells, List<BlockPos> targetCells){
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
                double distance = pos1.distSqr(pos2);
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

    public PortalShape(CompoundNBT tag){
        this.axis = Enum.valueOf(Direction.Axis.class, tag.getString("axis"));

        if(tag.contains("area", Constants.NBT.TAG_COMPOUND)){
            // Use old behaviour to load the data
            CompoundNBT areaTag = tag.getCompound("area");
            for(int i = 0; i < areaTag.size(); i++){
                CompoundNBT pos = areaTag.getCompound("" + i);
                this.area.add(new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")));
            }
            CompoundNBT frameTag = tag.getCompound("frame");
            for(int i = 0; i < areaTag.size(); i++){
                CompoundNBT pos = frameTag.getCompound("" + i);
                this.frame.add(new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")));
            }
            CompoundNBT stabilizerTag = tag.getCompound("stabilizers");
            for(int i = 0; i < areaTag.size(); i++){
                CompoundNBT pos = stabilizerTag.getCompound("" + i);
                this.stabilizers.add(new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")));
            }
            CompoundNBT energyCellsTag = tag.getCompound("energyCells");
            for(int i = 0; i < areaTag.size(); i++){
                CompoundNBT pos = energyCellsTag.getCompound("" + i);
                this.energyCells.add(new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")));
            }
            CompoundNBT targetCellsTag = tag.getCompound("targetCells");
            for(int i = 0; i < areaTag.size(); i++){
                CompoundNBT pos = targetCellsTag.getCompound("" + i);
                this.targetCells.add(new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")));
            }
        }else{
            // Load blocks
            int[] area = tag.getIntArray("area");
            for(int i = 0; i < area.length / 3; i++)
                this.area.add(new BlockPos(area[i * 3], area[i * 3 + 1], area[i * 3 + 2]));

            int[] frame = tag.getIntArray("frame");
            for(int i = 0; i < frame.length / 3; i++)
                this.frame.add(new BlockPos(frame[i * 3], frame[i * 3 + 1], frame[i * 3 + 2]));

            int[] stabilizers = tag.getIntArray("stabilizers");
            for(int i = 0; i < stabilizers.length / 3; i++)
                this.stabilizers.add(new BlockPos(stabilizers[i * 3], stabilizers[i * 3 + 1], stabilizers[i * 3 + 2]));

            int[] energyCells = tag.getIntArray("energyCells");
            for(int i = 0; i < energyCells.length / 3; i++)
                this.energyCells.add(new BlockPos(energyCells[i * 3], energyCells[i * 3 + 1], energyCells[i * 3 + 2]));

            int[] targetCells = tag.getIntArray("targetCells");
            for(int i = 0; i < targetCells.length / 3; i++)
                this.targetCells.add(new BlockPos(targetCells[i * 3], targetCells[i * 3 + 1], targetCells[i * 3 + 2]));
        }

        this.span = tag.getDouble("span");

        this.minCorner = new BlockPos(tag.getInt("minCornerX"), tag.getInt("minCornerY"), tag.getInt("minCornerZ"));
        this.maxCorner = new BlockPos(tag.getInt("maxCornerX"), tag.getInt("maxCornerY"), tag.getInt("maxCornerZ"));
    }

    public void createPortals(World level, DyeColor color){
        if(color == null)
            color = DyeColor.values()[new Random().nextInt(DyeColor.values().length)];
        for(BlockPos pos : this.area){
            BlockState state = level.getBlockState(pos);
            if(!(state.getBlock() instanceof PortalBlock) || state.getValue(PortalBlock.AXIS_PROPERTY) != this.axis || state.getValue(PortalBlock.COLOR_PROPERTY) != color){
                boolean waterlogged = level.getFluidState(pos).getType() == Fluids.WATER;
                level.setBlockAndUpdate(pos, Wormhole.portal.defaultBlockState().setValue(PortalBlock.AXIS_PROPERTY, this.axis).setValue(PortalBlock.COLOR_PROPERTY, color).setValue(PortalBlock.WATERLOGGED, waterlogged));
            }
        }
    }

    public void destroyPortals(World world){
        for(BlockPos pos : this.area){
            BlockState state = world.getBlockState(pos);
            if(state.getBlock() instanceof PortalBlock){
                if(state.getValue(PortalBlock.WATERLOGGED))
                    world.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
                else
                    world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
    }

    public boolean validateFrame(IBlockReader world){
        for(BlockPos pos : this.frame){
            if(!(world.getBlockState(pos).getBlock() instanceof IPortalGroupEntity))
                return false;
        }
        return true;
    }

    public boolean validatePortal(IBlockReader world){
        for(BlockPos pos : this.area){
            BlockState state = world.getBlockState(pos);
            if(!(state.getBlock() instanceof PortalBlock && state.getValue(PortalBlock.AXIS_PROPERTY) == this.axis) && !state.isAir() && state.getBlock() != Blocks.WATER)
                return false;
        }
        return true;
    }

    public CompoundNBT write(){
        CompoundNBT tag = new CompoundNBT();
        tag.putString("axis", this.axis.name());

        int[] areaData = new int[this.area.size() * 3];
        for(int i = 0; i < this.area.size(); i++){
            areaData[i * 3] = this.area.get(i).getX();
            areaData[i * 3 + 1] = this.area.get(i).getY();
            areaData[i * 3 + 2] = this.area.get(i).getZ();
        }
        tag.putIntArray("area", areaData);

        int[] frameData = new int[this.frame.size() * 3];
        for(int i = 0; i < this.frame.size(); i++){
            frameData[i * 3] = this.frame.get(i).getX();
            frameData[i * 3 + 1] = this.frame.get(i).getY();
            frameData[i * 3 + 2] = this.frame.get(i).getZ();
        }
        tag.putIntArray("frame", frameData);

        int[] stabilizersData = new int[this.stabilizers.size() * 3];
        for(int i = 0; i < this.stabilizers.size(); i++){
            stabilizersData[i * 3] = this.stabilizers.get(i).getX();
            stabilizersData[i * 3 + 1] = this.stabilizers.get(i).getY();
            stabilizersData[i * 3 + 2] = this.stabilizers.get(i).getZ();
        }
        tag.putIntArray("stabilizers", stabilizersData);

        int[] energyCellsData = new int[this.energyCells.size() * 3];
        for(int i = 0; i < this.energyCells.size(); i++){
            energyCellsData[i * 3] = this.energyCells.get(i).getX();
            energyCellsData[i * 3 + 1] = this.energyCells.get(i).getY();
            energyCellsData[i * 3 + 2] = this.energyCells.get(i).getZ();
        }
        tag.putIntArray("energyCells", energyCellsData);

        int[] targetCellsData = new int[this.targetCells.size() * 3];
        for(int i = 0; i < this.targetCells.size(); i++){
            targetCellsData[i * 3] = this.targetCells.get(i).getX();
            targetCellsData[i * 3 + 1] = this.targetCells.get(i).getY();
            targetCellsData[i * 3 + 2] = this.targetCells.get(i).getZ();
        }
        tag.putIntArray("targetCells", targetCellsData);

        tag.putDouble("span", this.span);

        tag.putInt("minCornerX", this.minCorner.getX());
        tag.putInt("minCornerY", this.minCorner.getY());
        tag.putInt("minCornerZ", this.minCorner.getZ());

        tag.putInt("maxCornerX", this.maxCorner.getX());
        tag.putInt("maxCornerY", this.maxCorner.getY());
        tag.putInt("maxCornerZ", this.maxCorner.getZ());

        return tag;
    }

    public static PortalShape read(CompoundNBT tag){
        return new PortalShape(tag);
    }
}
