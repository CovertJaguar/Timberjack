/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static mods.timberjack.TimberjackUtils.*;

/**
 * Created by CovertJaguar on 4/15/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
class FellingManager {
    private static final int MAX_LOGS = 1000;
    private Collection<Tree> fellQueue = new LinkedList<>();
    private final World world;

    FellingManager(World world) {
        this.world = world;
    }

    boolean isEmpty() {
        return fellQueue.isEmpty();
    }

    void tick() {
//            long worldTime = event.world.getTotalWorldTime();
//            if (worldTime % 4 != 0)
//                return;
        fellQueue.forEach(tree -> {
            Iterator<BlockPos> it = tree.logsToFell.iterator();
            for (int ii = 0; ii < 1 && it.hasNext(); ii++) {
                BlockPos log = it.next();
                tree.fellLog(log);
                it.remove();
            }
        });
        fellQueue.removeIf(tree -> tree.logsToFell.isEmpty());
    }

    void onChop(BlockPos pos) {
        Tree tree = new Tree(pos);
        tree.buildTree();
        tree.queueForFelling();
    }

    private class Tree {
        private Collection<Branch> branches = new LinkedList<>();
        private Set<BlockPos> logsToFell = new ConcurrentSkipListSet<>();
        private final BlockPos choppedBlock;

        Tree(BlockPos choppedBlock) {
            this.choppedBlock = choppedBlock;
            makeBranch(choppedBlock);
        }

        boolean contains(BlockPos pos) {
            return branches.stream().anyMatch(b -> b.logs.contains(pos));
        }

        Branch makeBranch(BlockPos pos) {
            Branch branch = new Branch(pos);
            branches.add(branch);
            return branch;
        }

        private void buildTree() {
            iterateBlocks(1, choppedBlock, targetPos -> {
                if (!contains(targetPos)) {
                    IBlockState targetState = world.getBlockState(targetPos);
                    if (isWood(targetState, world, targetPos)) {
                        addBranch(targetPos.toImmutable());
                    }
                }
            });
        }

        private void addBranch(BlockPos pos) {
            Branch branch = makeBranch(pos);
            branch.logs.add(choppedBlock);
            branch.expandLogs(pos);
            if (branch.hasLeaves && !branch.touchesGround)
                logsToFell.addAll(branch.logs);

        }

        private void queueForFelling() {
            if (!logsToFell.isEmpty())
                fellQueue.add(this);
        }

        private void fellLog(BlockPos pos) {
            spawnFalling(world, new BlockPos.MutableBlockPos(pos), world.getBlockState(pos), true);
            iterateBlocks(4, pos, targetPos -> {
                IBlockState targetState = world.getBlockState(targetPos);
                if (isLeaves(targetState, world, targetPos)) {
                    spawnFalling(world, targetPos, targetState, false);
                } else if (!contains(targetPos) && isWood(targetState, world, targetPos)) {
                    addBranch(targetPos.toImmutable());
                }
            });
        }
    }

    private class Branch {
        private Set<BlockPos> logs = new HashSet<>();
        private boolean hasLeaves;
        private boolean touchesGround;

        Branch(BlockPos start) {
            logs.add(start);
        }

        private void expandLogs(BlockPos last) {
            if (logs.size() > MAX_LOGS)
                return;
            if (!touchesGround) {
                BlockPos down = last.down();
                if (!logs.contains(down)) {
                    IBlockState targetState = world.getBlockState(down);
                    if (isDirt(targetState, world, down)) {
                        touchesGround = true;
                    }
                }
            }

            iterateBlocks(1, last, targetPos -> {
                if (!logs.contains(targetPos)) {
                    IBlockState targetState = world.getBlockState(targetPos);
                    if (isWood(targetState, world, targetPos)) {
                        BlockPos immutable = targetPos.toImmutable();
                        logs.add(immutable);
                        expandLogs(immutable);
                    } else if (!hasLeaves && isLeaves(targetState, world, targetPos))
                        hasLeaves = true;
                }
            });
        }
    }

}
