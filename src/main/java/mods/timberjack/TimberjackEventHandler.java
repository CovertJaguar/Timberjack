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
    private static Map<World, Collection<Tree>> chopQueue = new MapMaker().weakKeys().makeMap();

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent event) {
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.START) {
//            long worldTime = event.world.getTotalWorldTime();
//            if (worldTime % 4 != 0)
//                return;
            Collection<Tree> choppedTrees = chopQueue.get(event.world);
            if (choppedTrees != null) {
                choppedTrees.forEach(tree -> {
                    Iterator<BlockPos> it = tree.choppable.iterator();
                    for (int counter = 0; counter < 1 && it.hasNext(); counter++) {
                        BlockPos log = it.next();
                        chop(event.world, tree, log);
                        it.remove();
                    }
                });
                choppedTrees.removeIf(tree -> tree.choppable.isEmpty());
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
            Tree tree = new Tree();
            tree.logSets.put(base, base);
            BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
            for (int y = 0; y <= 1; ++y) {
                for (int x = -1; x <= 1; ++x) {
                    for (int z = -1; z <= 1; ++z) {
                        targetPos.setPos(base.getX() + x, base.getY() + y, base.getZ() + z);
                        if (tree.logSets.containsValue(targetPos))
                            continue;
                        IBlockState targetState = world.getBlockState(targetPos);
                        if (targetState.getBlock().isWood(world, targetPos)) {
                            BlockPos immutable = targetPos.toImmutable();
                            tree.logSets.put(immutable, base);
                            tree.logSets.put(immutable, immutable);
                            Collection<BlockPos> logs = tree.logSets.get(immutable);
                            if (expandLogsAndCanFell(world, tree, logs, targetPos)) {
                                if (tree.foundLeaves)
                                    tree.choppable.addAll(logs);
                            }
                        }
                    }
                }
            }
            if (!tree.choppable.isEmpty())
                chopQueue.computeIfAbsent(world, w -> new LinkedList<>()).add(tree);
        }
    }

    private static class Tree {
        private Multimap<BlockPos, BlockPos> logSets = HashMultimap.create();
        private Set<BlockPos> choppable = new TreeSet<>();
        private boolean foundLeaves;
    }

    private void chop(World world, Tree tree, BlockPos pos) {
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
        for (int y = -2; y <= 2; ++y) {
            for (int x = -2; x <= 2; ++x) {
                for (int z = -2; z <= 2; ++z) {
                    targetPos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    IBlockState targetState = world.getBlockState(targetPos);
                    if (!tree.logSets.containsValue(targetPos) && targetState.getBlock().isWood(world, targetPos))
                        spawnFalling(world, targetPos, targetState, true);
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

    private boolean expandLogsAndCanFell(World world, Tree tree, Collection<BlockPos> logs, BlockPos last) {
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
                        if (!expandLogsAndCanFell(world, tree, logs, immutable))
                            return false;
                    } else if (!tree.foundLeaves && targetState.getBlock().isLeaves(targetState, world, targetPos))
                        tree.foundLeaves = true;
                }
            }
        }
        return true;
    }
}