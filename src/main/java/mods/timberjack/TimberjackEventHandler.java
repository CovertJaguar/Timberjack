/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack;

import com.google.common.collect.MapMaker;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

import static mods.timberjack.TimberjackUtils.isWood;

/**
 * Created by CovertJaguar on 4/12/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class TimberjackEventHandler {

    private static Map<World, FellingManager> fellingManagers = new MapMaker().weakKeys().makeMap();

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent event) {
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.START) {
            FellingManager fellingManager = fellingManagers.get(event.world);
            if (fellingManager != null) {
                fellingManager.tick();
                if (fellingManager.isEmpty())
                    fellingManagers.remove(event.world);
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
        if (isWood(event.getState(), world, event.getPos())) {
            fellingManagers.computeIfAbsent(world, FellingManager::new).onChop(event.getPos());
        }
    }
}