package mods.timberjack;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;

/**
 * Created by CovertJaguar on 4/12/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class TimberjackEventHandler {

    private static final int MAX_LOGS = 500;
    private static Map<World, Collection<TreeSet<BlockPos>>> chopQueue = new MapMaker().weakKeys().makeMap();

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent event) {
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.START) {
//            long worldTime = event.world.getTotalWorldTime();
//            if (worldTime % 4 != 0)
//                return;
            Collection<TreeSet<BlockPos>> choppedTrees = chopQueue.get(event.world);
            if (choppedTrees != null) {
                choppedTrees.forEach(tree -> {
                    Iterator<BlockPos> it = tree.iterator();
                    for (int counter = 0; counter < 1 && it.hasNext(); counter++) {
                        BlockPos log = it.next();
                        chop(event.world, log);
                        it.remove();
                    }
                });
                choppedTrees.removeIf(TreeSet::isEmpty);
            }
            chopQueue.values().removeIf(Collection::isEmpty);
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
        if (event.getState().getBlock().isWood(world, event.getPos())) {
            BlockPos base = event.getPos();
            Multimap<BlockPos, BlockPos> logSets = HashMultimap.create();
            logSets.put(base, base);
            BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
            for (int y = 0; y <= 1; ++y) {
                for (int x = -1; x <= 1; ++x) {
                    for (int z = -1; z <= 1; ++z) {
                        targetPos.setPos(base.getX() + x, base.getY() + y, base.getZ() + z);
                        if (logSets.containsValue(targetPos))
                            continue;
                        IBlockState targetState = world.getBlockState(targetPos);
                        if (targetState.getBlock().isWood(world, targetPos)) {
                            BlockPos immutable = targetPos.toImmutable();
                            logSets.put(immutable, base);
                            logSets.put(immutable, immutable);
                            Collection<BlockPos> logs = logSets.get(immutable);
                            if (expandLogsAndCanFell(world, logs, targetPos)) {
                                chopQueue.computeIfAbsent(world, w -> new LinkedList<>()).add(new TreeSet<>(logs));
                            }
                        }
                    }
                }
            }
        }
    }

    private void chop(World world, BlockPos pos) {
//        world.destroyBlock(pos, true);
        spawnFalling(world, pos, world.getBlockState(pos), true);
        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
        for (int y = -4; y <= 4; ++y) {
            for (int x = -4; x <= 4; ++x) {
                for (int z = -4; z <= 4; ++z) {
                    targetPos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    IBlockState targetState = world.getBlockState(targetPos);
                    if (targetState.getBlock().isLeaves(targetState, world, targetPos)) {
//                        world.destroyBlock(targetPos, false);
                        spawnFalling(world, targetPos, targetState, false);
                    }
                }
            }
        }
    }

    private void spawnFalling(World world, BlockPos pos, IBlockState state, boolean log) {
        EntityFallingBlock entity = new EntityFallingBlock(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, state);
        entity.motionX = (world.rand.nextFloat() - 0.5F) * 0.8F;
        entity.motionZ = (world.rand.nextFloat() - 0.5F) * 0.8F;
        entity.shouldDropItem = log;
        entity.setHurtEntities(log);
        world.spawnEntityInWorld(entity);
    }

    private boolean expandLogsAndCanFell(World world, Collection<BlockPos> logs, BlockPos last) {
        if (logs.size() > MAX_LOGS)
            return true;
        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
        targetPos.setPos(last.down());
        if (!logs.contains(targetPos)) {
            IBlockState targetState = world.getBlockState(targetPos);
            if (targetState.getBlock().canSustainPlant(targetState, world, targetPos, EnumFacing.UP, (BlockSapling) Blocks.SAPLING)) {
                return false;
            }
        }
        for (int y = -1; y <= 1; ++y) {
            for (int x = -1; x <= 1; ++x) {
                for (int z = -1; z <= 1; ++z) {
                    targetPos.setPos(last.getX() + x, last.getY() + y, last.getZ() + z);
                    if (logs.contains(targetPos))
                        continue;
                    IBlockState targetState = world.getBlockState(targetPos);
                    if (targetState.getBlock().isWood(world, targetPos)) {
                        BlockPos immutable = targetPos.toImmutable();
                        logs.add(immutable);
                        if (!expandLogsAndCanFell(world, logs, immutable))
                            return false;
                    }
                }
            }
        }
        return true;
    }
}