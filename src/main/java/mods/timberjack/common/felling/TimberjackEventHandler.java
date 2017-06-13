/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack.common.felling;

import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Random;

/**
 * Created by CovertJaguar on 4/12/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class TimberjackEventHandler {

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent event) {
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.START) {
            FellingManager fellingManager = FellingManager.fellingManagers.get(event.world);
            if (fellingManager != null) {
                fellingManager.tick();
                if (fellingManager.isEmpty())
                    FellingManager.fellingManagers.remove(event.world);
            }
        }
    }

    @SubscribeEvent
    public void entityJoin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityFallingBlock) {
            EntityFallingBlock falling = (EntityFallingBlock) event.getEntity();
            if (falling.getBlock() != null) {
                if (falling.getBlock().getBlock() instanceof BlockLog || falling.getBlock().getMaterial() == Material.LEAVES) {
                    if (falling.fallTime > 600)
                        falling.setDead();
                }
            }
        }
    }

//    @SubscribeEvent
//    public void breakSpeedEvent(PlayerEvent.BreakSpeed event) {
//        World world = event.getEntity().worldObj;
//        IBlockState state = event.getState();
//        if (state.getBlock().isWood(world, event.getPos())) {
//            BlockPos down = event.getPos().down();
//            IBlockState downState = world.getBlockState(down);
//            if (downState.getBlock().canSustainPlant(downState, world, down, EnumFacing.UP, (BlockSapling) Blocks.SAPLING))
//                event.setNewSpeed(event.getOriginalSpeed() / 10F);
//        }
//    }

    @SubscribeEvent
    public void chopEvent(BlockEvent.BreakEvent event) {
        World world = event.getWorld();
        if (TimberjackUtils.isWood(event.getState(), world, event.getPos())) {
            EnumFacing fellingDirection;
            if (world.rand.nextFloat() < 0.1) {
                fellingDirection = EnumFacing.HORIZONTALS[new Random().nextInt(EnumFacing.HORIZONTALS.length)];
            } else {
                fellingDirection = BlockPistonBase.getFacingFromEntity(event.getPos(), event.getPlayer()).getOpposite();
            }
            FellingManager.fellingManagers.computeIfAbsent(world, FellingManager::new).onChop(event.getPos(), fellingDirection);
        }
    }
}