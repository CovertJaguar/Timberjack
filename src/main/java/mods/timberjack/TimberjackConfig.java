/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Created by CovertJaguar on 4/17/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class TimberjackConfig {
    private static int maxLogsProcessed = 2000;
    private static boolean canFellLargeTrees;

    public static int getMaxLogsProcessed() {
        return maxLogsProcessed;
    }

    public static boolean canFellLargeTrees() {
        return canFellLargeTrees;
    }

    public static void load(File configDir) {
        Configuration config = new Configuration(new File(configDir, Timberjack.MOD_ID + ".cfg"));
        config.load();

        maxLogsProcessed = config.get(Configuration.CATEGORY_GENERAL, "maxLogsProcessed", maxLogsProcessed).getInt(maxLogsProcessed);
        canFellLargeTrees = config.get(Configuration.CATEGORY_GENERAL, "canFellLargeTrees", canFellLargeTrees).getBoolean(canFellLargeTrees);

        if (config.hasChanged())
            config.save();
    }
}
