/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack.common;

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
    private static boolean aggressiveHouseProtection = true;
    private static String[] logBlacklistArray = {
            "natura:redwood_logs",
            "biomesoplenty:log_0#4",
            "forestry:logs.6#0",
            "forestry:logs.fireproof.6#0",
    };
    private static Set<String> logBlacklist = Collections.emptySet();

    public static int getMaxLogsProcessed() {
        return maxLogsProcessed;
    }

    public static boolean canFellLargeTrees() {
        return canFellLargeTrees;
    }

    public static boolean aggressiveHouseProtection() {
        return aggressiveHouseProtection;
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
        aggressiveHouseProtection = config.getBoolean("aggressiveHouseProtection", Configuration.CATEGORY_GENERAL, aggressiveHouseProtection, "If doors, glass, bed, workbench, signs, furnaces, carpets, etc are detected near the tree it will terminate the felling, protecting the house");
        logBlacklistArray = config.getStringList("logBlacklist", Configuration.CATEGORY_GENERAL, logBlacklistArray, "Log types that should never be felled. Format: <resourceId/modId>:<blockName>[#<meta>]");

        logBlacklist = Arrays.stream(logBlacklistArray).collect(Collectors.toSet());

        if (config.hasChanged())
            config.save();
    }
}
