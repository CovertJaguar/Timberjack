/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static mods.timberjack.TimberjackUtils.*;

/**
 * Created by CovertJaguar on 4/15/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
class FellingManager {
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
//        if(!fellQueue.isEmpty())
//            System.out.printf("Felling %d trees\n", fellQueue.size());
        fellQueue.forEach(tree -> {
            tree.prepForFelling();
            Iterator<BlockPos> it = tree.logsToFell.iterator();
            if (it.hasNext()) {
                BlockPos log = it.next();
                tree.fellLog(log);
                it.remove();
            }
        });
        fellQueue.removeIf(tree -> !tree.hasLogsToFell());
    }

    void onChop(BlockPos pos) {
        Tree tree = new Tree(pos);
        tree.buildTree();
        tree.queueForFelling();
    }

    private class Tree {
        private Collection<Branch> branches = new ConcurrentLinkedQueue<>();
        private Set<BlockPos> logs = new HashSet<>();
        private List<BlockPos> logsToFell = new LinkedList<>();
        private List<BlockPos> newLogsToFell = new LinkedList<>();
        private final BlockPos choppedBlock;
        private Vec3d centroid = Vec3d.ZERO;

        Tree(BlockPos choppedBlock) {
            this.choppedBlock = choppedBlock;
            makeBranch(choppedBlock);
        }

        boolean contains(BlockPos pos) {
            return logs.contains(pos);
        }

        int size() {
            return logs.size();
        }

        void addLogsToFell(Collection<BlockPos> logs) {
            newLogsToFell.addAll(logs);
        }

        void prepForFelling() {
            if (!newLogsToFell.isEmpty()) {
                logsToFell.addAll(newLogsToFell);
                updateCentroid();
                logsToFell.sort((o1, o2) -> {
                    int yCompare = Integer.compare(o1.getY(), o2.getY());
                    if (yCompare != 0)
                        return yCompare;
                    int distCompare = Double.compare(centroid.squareDistanceTo(o2.getX(), o2.getY(), o2.getZ()),
                            centroid.squareDistanceTo(o1.getX(), o1.getY(), o1.getZ()));
                    if (distCompare != 0)
                        return distCompare;
                    return o1.compareTo(o2);
                });
                newLogsToFell.clear();
            }
        }

        void updateCentroid() {
            double x = 0;
            double y = 0;
            double z = 0;
            for (BlockPos pos : logs) {
                x += pos.getX();
                y += pos.getY();
                z += pos.getZ();
            }
            int size = logs.size();
            x /= size;
            y /= size;
            z /= size;
            centroid = new Vec3d(x, y, z);
        }

        boolean hasLogsToFell() {
            return !logsToFell.isEmpty() || !newLogsToFell.isEmpty();
        }

        Branch makeBranch(BlockPos pos) {
            Branch branch = new Branch(this, pos);
            branches.add(branch);
            return branch;
        }

        private void buildTree() {
            iterateBlocks(1, choppedBlock, targetPos -> {
                if (!contains(targetPos)) {
                    IBlockState targetState = world.getBlockState(targetPos);
                    if (isWood(targetState, world, targetPos)) {
                        scanNewBranch(targetPos.toImmutable());
                    }
                }
            });
        }

        private void scanNewBranch(BlockPos pos) {
            Branch branch = makeBranch(pos);
            branch.scan();
        }

        private void queueForFelling() {
            if (hasLogsToFell())
                fellQueue.add(this);
        }

        private void fellLog(BlockPos logPos) {
            spawnFallingLog(world, logPos, centroid);
            iterateBlocks(4, logPos, targetPos -> {
                IBlockState targetState = world.getBlockState(targetPos);
                if (isLeaves(targetState, world, targetPos)) {
                    spawnFallingLeaves(world, targetPos, logPos, centroid, targetState);
                } else if (isWood(targetState, world, targetPos) && !contains(targetPos)) {
                    scanNewBranch(targetPos.toImmutable());
                }
            });
        }
    }

    private class Branch {
        private Set<BlockPos> logs = new HashSet<>();
        private final Tree tree;
        private final BlockPos start;
        private boolean hasLeaves;
        private boolean touchesGround;

        Branch(Tree tree, BlockPos start) {
            this.tree = tree;
            this.start = start;
            addLog(start);
        }

        private void scan() {
            expandLogs(start);
            if (hasLeaves && !touchesGround)
                tree.addLogsToFell(logs);
        }

        private void addLog(BlockPos pos) {
            logs.add(pos);
            tree.logs.add(pos);
        }

        private void expandLogs(BlockPos last) {
            if (tree.size() > TimberjackConfig.getMaxLogsProcessed())
                return;
            if (!touchesGround) {
                BlockPos down = last.down();
                if (!tree.contains(down)) {
                    IBlockState targetState = world.getBlockState(down);
                    if (isDirt(targetState, world, down)) {
                        touchesGround = true;
                    }
                }
            }

            iterateBlocks(1, last, targetPos -> {
                if (!tree.contains(targetPos)) {
                    IBlockState targetState = world.getBlockState(targetPos);
                    if (isWood(targetState, world, targetPos)) {
                        BlockPos immutable = targetPos.toImmutable();
                        addLog(immutable);
                        expandLogs(immutable);
                    } else if (!hasLeaves && isLeaves(targetState, world, targetPos))
                        hasLeaves = true;
                }
            });
        }
    }

}
