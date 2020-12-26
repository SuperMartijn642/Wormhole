package com.supermartijn642.wormhole.portal;

import com.google.common.collect.Lists;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.StabilizerTile;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.*;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
public class PortalShape {

    public static final Map<Direction.Axis,List<BlockPos>> DIRECT_OFFSETS = new EnumMap<>(Direction.Axis.class);
    private static final Map<Direction.Axis,List<BlockPos>> INDIRECT_OFFSETS = new EnumMap<>(Direction.Axis.class);
    private static final Map<Direction.Axis,List<BlockPos>> ALL_OFFSETS = new EnumMap<>(Direction.Axis.class);

    static{
        DIRECT_OFFSETS.put(Direction.Axis.X, Lists.newArrayList(BlockPos.ZERO.up(), BlockPos.ZERO.down(), BlockPos.ZERO.north(), BlockPos.ZERO.south()));
        DIRECT_OFFSETS.put(Direction.Axis.Y, Lists.newArrayList(BlockPos.ZERO.north(), BlockPos.ZERO.east(), BlockPos.ZERO.south(), BlockPos.ZERO.west()));
        DIRECT_OFFSETS.put(Direction.Axis.Z, Lists.newArrayList(BlockPos.ZERO.up(), BlockPos.ZERO.down(), BlockPos.ZERO.east(), BlockPos.ZERO.west()));

        INDIRECT_OFFSETS.put(Direction.Axis.X, Lists.newArrayList(BlockPos.ZERO.up().north(), BlockPos.ZERO.up().south(), BlockPos.ZERO.down().north(), BlockPos.ZERO.down().south()));
        INDIRECT_OFFSETS.put(Direction.Axis.Y, Lists.newArrayList(BlockPos.ZERO.north().east(), BlockPos.ZERO.north().west(), BlockPos.ZERO.south().east(), BlockPos.ZERO.south().west()));
        INDIRECT_OFFSETS.put(Direction.Axis.Z, Lists.newArrayList(BlockPos.ZERO.up().east(), BlockPos.ZERO.up().west(), BlockPos.ZERO.down().east(), BlockPos.ZERO.down().west()));

        for(Direction.Axis axis : Direction.Axis.values()){
            List<BlockPos> pos = new ArrayList<>();
            pos.addAll(DIRECT_OFFSETS.get(axis));
            pos.addAll(INDIRECT_OFFSETS.get(axis));
            ALL_OFFSETS.put(axis, pos);
        }
    }

    public static PortalShape find(IBlockReader world, BlockPos center){
        for(Direction.Axis axis : Direction.Axis.values()){
            PortalShape shape = find(world, center, axis);
            if(shape != null)
                return shape;
        }
        return null;
    }

    private static PortalShape find(IBlockReader world, BlockPos center, Direction.Axis axis){
        for(BlockPos offset : ALL_OFFSETS.get(axis)){
            if(world.getBlockState(center.add(offset)).getBlock() == Blocks.AIR){
                PortalShape shape = findArea(world, center.add(offset), axis);
                if(shape != null)
                    return shape;
            }
        }
        return null;
    }

    private static PortalShape findArea(IBlockReader world, BlockPos start, Direction.Axis axis){
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
                    Block block = world.getBlockState(offPos).getBlock();
                    TileEntity tile = world.getTileEntity(offPos);
                    if(block == Blocks.AIR){
                        if(!done.contains(offPos) && !current.contains(offPos) && !next.contains(offPos))
                            next.add(offPos);
                    }else if(tile instanceof IPortalGroupTile && !((IPortalGroupTile)tile).hasGroup()){
                        if(!frame.contains(offPos)){
                            frame.add(offPos);
                            if(tile instanceof StabilizerTile)
                                stabilizers.add(offPos);
                            if(tile instanceof IEnergyCellTile)
                                energyCells.add(offPos);
                            if(tile instanceof ITargetCellTile)
                                targetCells.add(offPos);
                        }
                        frames++;
                    }else
                        return null;
                }
                if(frames >= 2)
                    corners.add(pos);
            }
            if(done.size() + current.size() + next.size() > WormholeConfig.INSTANCE.maxPortalSize.get())
                return null;
            done.addAll(current);
            current.clear();
            current.addAll(next);
            next.clear();
        }

        if(WormholeConfig.INSTANCE.requireCorners.get()){
            if(!validateCorners(world, done, frame, corners, stabilizers, axis))
                return null;
        }else
            collectCorners(world, done, frame, corners, stabilizers, axis);

        if(stabilizers.size() == 0)
            return null;

        return new PortalShape(axis, done, frame, stabilizers, energyCells, targetCells);
    }

    private static void collectCorners(IBlockReader world, List<BlockPos> area, List<BlockPos> frame, List<BlockPos> corners, List<BlockPos> stabilizers, Direction.Axis axis){
        BlockPos dir1pos = axis == Direction.Axis.Y ? BlockPos.ZERO.east() : BlockPos.ZERO.up();
        BlockPos dir1neg = axis == Direction.Axis.Y ? BlockPos.ZERO.west() : BlockPos.ZERO.down();
        BlockPos dir2pos = axis == Direction.Axis.Z ? BlockPos.ZERO.east() : BlockPos.ZERO.north();
        BlockPos dir2neg = axis == Direction.Axis.Z ? BlockPos.ZERO.west() : BlockPos.ZERO.south();
        for(BlockPos corner : corners){
            collectCorner(world, area, frame, stabilizers, corner, dir1pos, dir2pos);
            collectCorner(world, area, frame, stabilizers, corner, dir1pos, dir2neg);
            collectCorner(world, area, frame, stabilizers, corner, dir1neg, dir2pos);
            collectCorner(world, area, frame, stabilizers, corner, dir1neg, dir2neg);
        }
    }

    private static boolean validateCorners(IBlockReader world, List<BlockPos> area, List<BlockPos> frame, List<BlockPos> corners, List<BlockPos> stabilizers, Direction.Axis axis){
        BlockPos dir1pos = axis == Direction.Axis.Y ? BlockPos.ZERO.east() : BlockPos.ZERO.up();
        BlockPos dir1neg = axis == Direction.Axis.Y ? BlockPos.ZERO.west() : BlockPos.ZERO.down();
        BlockPos dir2pos = axis == Direction.Axis.Z ? BlockPos.ZERO.east() : BlockPos.ZERO.north();
        BlockPos dir2neg = axis == Direction.Axis.Z ? BlockPos.ZERO.west() : BlockPos.ZERO.south();
        for(BlockPos corner : corners){
            if(!collectCorner(world, area, frame, stabilizers, corner, dir1pos, dir2pos))
                return false;
            if(!collectCorner(world, area, frame, stabilizers, corner, dir1pos, dir2neg))
                return false;
            if(!collectCorner(world, area, frame, stabilizers, corner, dir1neg, dir2pos))
                return false;
            if(!collectCorner(world, area, frame, stabilizers, corner, dir1neg, dir2neg))
                return false;
        }
        return true;
    }

    private static boolean collectCorner(IBlockReader world, List<BlockPos> area, List<BlockPos> frame, List<BlockPos> stabilizers, BlockPos corner, BlockPos dir1, BlockPos dir2){
        if(frame.contains(corner.add(dir1)) && frame.contains(corner.add(dir2))){
            BlockPos pos = corner.add(dir1).add(dir2);
            TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof IPortalGroupTile ? ((IPortalGroupTile)tile).hasGroup() : !area.contains(pos))
                return false;
            else{
                if(tile instanceof StabilizerTile && !stabilizers.contains(pos))
                    stabilizers.add(pos);
                if(!frame.contains(pos))
                    frame.add(pos);
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
        this.span = Math.sqrt(span);
        this.minCorner = new BlockPos(minX, minY, minZ);
        this.maxCorner = new BlockPos(maxX, maxY, maxZ);
    }

    public PortalShape(CompoundNBT tag){
        this.axis = Enum.valueOf(Direction.Axis.class, tag.getString("axis"));

        CompoundNBT areaTag = tag.getCompound("area");
        for(String key : areaTag.keySet()){
            CompoundNBT pos = areaTag.getCompound(key);
            this.area.add(new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")));
        }

        CompoundNBT frameTag = tag.getCompound("frame");
        for(String key : frameTag.keySet()){
            CompoundNBT pos = frameTag.getCompound(key);
            this.frame.add(new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")));
        }

        CompoundNBT stabilizerTag = tag.getCompound("stabilizers");
        for(String key : stabilizerTag.keySet()){
            CompoundNBT pos = stabilizerTag.getCompound(key);
            this.stabilizers.add(new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")));
        }

        CompoundNBT energyCellsTag = tag.getCompound("energyCells");
        for(String key : energyCellsTag.keySet()){
            CompoundNBT pos = energyCellsTag.getCompound(key);
            this.energyCells.add(new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")));
        }

        CompoundNBT targetCellsTag = tag.getCompound("targetCells");
        for(String key : targetCellsTag.keySet()){
            CompoundNBT pos = targetCellsTag.getCompound(key);
            this.targetCells.add(new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")));
        }

        this.span = tag.getDouble("span");

        this.minCorner = new BlockPos(tag.getInt("minCornerX"), tag.getInt("minCornerY"), tag.getInt("minCornerZ"));
        this.maxCorner = new BlockPos(tag.getInt("maxCornerX"), tag.getInt("maxCornerY"), tag.getInt("maxCornerZ"));
    }

    public void createPortals(World world, DyeColor color){
        if(color == null)
            color = DyeColor.values()[new Random().nextInt(DyeColor.values().length)];
        for(BlockPos pos : this.area){
            if(!(world.getBlockState(pos).getBlock() instanceof PortalBlock) || world.getBlockState(pos).get(PortalBlock.AXIS_PROPERTY) != this.axis || world.getBlockState(pos).get(PortalBlock.COLOR_PROPERTY) != color)
                world.setBlockState(pos, Wormhole.portal.getDefaultState().with(PortalBlock.AXIS_PROPERTY, this.axis).with(PortalBlock.COLOR_PROPERTY, color));
        }
    }

    public void destroyPortals(World world){
        for(BlockPos pos : this.area){
            if(world.getBlockState(pos).getBlock() instanceof PortalBlock)
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    public boolean validateFrame(IBlockReader world){
        for(BlockPos pos : this.frame){
            if(!(world.getBlockState(pos).getBlock() instanceof IPortalGroupTile))
                return false;
        }
        return true;
    }

    public boolean validatePortal(IBlockReader world){
        for(BlockPos pos : this.area){
            BlockState state = world.getBlockState(pos);
            if(!(state.getBlock() instanceof PortalBlock && state.get(PortalBlock.AXIS_PROPERTY) == this.axis) && state.getBlock() != Blocks.AIR)
                return false;
        }
        return true;
    }

    public CompoundNBT write(){
        CompoundNBT tag = new CompoundNBT();
        tag.putString("axis", this.axis.name());

        CompoundNBT areaTag = new CompoundNBT();
        for(int i = 0; i < this.area.size(); i++){
            CompoundNBT pos = new CompoundNBT();
            pos.putInt("x", this.area.get(i).getX());
            pos.putInt("y", this.area.get(i).getY());
            pos.putInt("z", this.area.get(i).getZ());
            areaTag.put("" + i, pos);
        }
        tag.put("area", areaTag);

        CompoundNBT frameTag = new CompoundNBT();
        for(int i = 0; i < this.frame.size(); i++){
            CompoundNBT pos = new CompoundNBT();
            pos.putInt("x", this.frame.get(i).getX());
            pos.putInt("y", this.frame.get(i).getY());
            pos.putInt("z", this.frame.get(i).getZ());
            frameTag.put("" + i, pos);
        }
        tag.put("frame", frameTag);

        CompoundNBT stabilizerTag = new CompoundNBT();
        for(int i = 0; i < this.stabilizers.size(); i++){
            CompoundNBT pos = new CompoundNBT();
            pos.putInt("x", this.stabilizers.get(i).getX());
            pos.putInt("y", this.stabilizers.get(i).getY());
            pos.putInt("z", this.stabilizers.get(i).getZ());
            stabilizerTag.put("" + i, pos);
        }
        tag.put("stabilizers", stabilizerTag);

        CompoundNBT energyCellsTag = new CompoundNBT();
        for(int i = 0; i < this.energyCells.size(); i++){
            CompoundNBT pos = new CompoundNBT();
            pos.putInt("x", this.energyCells.get(i).getX());
            pos.putInt("y", this.energyCells.get(i).getY());
            pos.putInt("z", this.energyCells.get(i).getZ());
            energyCellsTag.put("" + i, pos);
        }
        tag.put("energyCells", energyCellsTag);

        CompoundNBT targetCellsTag = new CompoundNBT();
        for(int i = 0; i < this.targetCells.size(); i++){
            CompoundNBT pos = new CompoundNBT();
            pos.putInt("x", this.targetCells.get(i).getX());
            pos.putInt("y", this.targetCells.get(i).getY());
            pos.putInt("z", this.targetCells.get(i).getZ());
            targetCellsTag.put("" + i, pos);
        }
        tag.put("targetCells", targetCellsTag);

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
