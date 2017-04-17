/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack;

import net.minecraft.block.BlockSapling;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.function.Consumer;

/**
 * Created by CovertJaguar on 4/13/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
class TimberjackUtils {

    static void iterateBlocks(int range, BlockPos center, Consumer<BlockPos.MutableBlockPos> action) {
        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
        for (int y = -range; y <= range; ++y) {
            for (int x = -range; x <= range; ++x) {
                for (int z = -range; z <= range; ++z) {
                    targetPos.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
                    action.accept(targetPos);
                }
            }
        }
    }

    static boolean isWood(IBlockState state, World world, BlockPos pos) {
        return state.getBlock().isWood(world, pos);
    }

    static boolean isLeaves(IBlockState state, World world, BlockPos pos) {
        return state.getBlock().isLeaves(state, world, pos);
    }

    static boolean isDirt(IBlockState state, World world, BlockPos pos) {
        if (state.getBlock().canSustainPlant(state, world, pos, EnumFacing.UP, (BlockSapling) Blocks.SAPLING))
            return true;
        Biome biome = world.getBiome(pos);
        return biome.topBlock != null && biome.topBlock.getBlock() == state.getBlock();
    }

    static void spawnFallingLog(World world, BlockPos logPos, Vec3d centroid) {
        spawnFalling(world, logPos, centroid, world.getBlockState(logPos), true);
    }

    static void spawnFallingLeaves(World world, BlockPos.MutableBlockPos pos, BlockPos logPos, Vec3d centroid, IBlockState state) {
        pos.move(EnumFacing.DOWN);
        IBlockState belowState = world.getBlockState(pos);
        boolean canFall = belowState.getBlock().isAir(belowState, world, pos)
                || belowState.getBlock() instanceof IGrowable
                || belowState.getMaterial().isReplaceable()
                || logPos.equals(pos);
        pos.move(EnumFacing.UP);

        if (canFall)
            spawnFalling(world, pos, centroid, state, false);
    }

    private static void spawnFalling(World world, BlockPos pos, Vec3d centroid, IBlockState state, boolean log) {
        EntityFallingBlock entity = new EntityFallingBlock(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, state);
        Vec3d vector = new Vec3d(pos);
        vector = vector.subtract(centroid);
        vector = vector.normalize();
        entity.motionX = vector.xCoord * 0.4 + (world.rand.nextFloat() - 0.5) * 0.4;
        entity.motionZ = vector.zCoord * 0.4 + (world.rand.nextFloat() - 0.5) * 0.4;
        entity.shouldDropItem = log;
        entity.setHurtEntities(log);
        world.spawnEntityInWorld(entity);
    }
}
