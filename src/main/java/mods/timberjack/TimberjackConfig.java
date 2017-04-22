/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by CovertJaguar on 4/17/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class TimberjackConfig {
    private static int maxLogsProcessed = 2000;
    private static boolean canFellLargeTrees;
    private static String[] logBlacklistArray = {"natura:redwood_logs"};
    private static Set<String> logBlacklist = Collections.emptySet();

    public static int getMaxLogsProcessed() {
        return maxLogsProcessed;
    }

    public static boolean canFellLargeTrees() {
        return canFellLargeTrees;
    }

    public static boolean canFellLog(IBlockState state) {
        String name = state.getBlock().getRegistryName().toString();
        if (logBlacklist.contains(name))
            return false;
        name += "#" + state.getBlock().getMetaFromState(state);
        return !logBlacklist.contains(name);
    }

    public static void load(File configDir) {
        Configuration config = new Configuration(new File(configDir, Timberjack.MOD_ID + ".cfg"));
        config.load();

        maxLogsProcessed = config.getInt("maxLogsProcessed", Configuration.CATEGORY_GENERAL, maxLogsProcessed, 0, 10_0000, "How many logs the tree scanning algorithm should look at before giving up");
        canFellLargeTrees = config.getBoolean("canFellLargeTrees", Configuration.CATEGORY_GENERAL, canFellLargeTrees, "What should happen when maxLogsProcessed is hit?");
        logBlacklistArray = config.getStringList("logBlacklist", Configuration.CATEGORY_GENERAL, logBlacklistArray, "Log types that should never be felled. Format: <resourceId/modId>:<blockName>[#<meta>]");

        logBlacklist = Arrays.stream(logBlacklistArray).collect(Collectors.toSet());

        if (config.hasChanged())
            config.save();
    }
}
