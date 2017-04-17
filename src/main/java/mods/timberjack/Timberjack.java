/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Timberjack.MOD_ID, version = Timberjack.VERSION)
public class Timberjack {
    static final String MOD_ID = "timberjack";
    static final String VERSION = "1.0";
//    @Mod.Instance(MOD_ID)
//    public static Timberjack instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        TimberjackConfig.load(event.getModConfigurationDirectory());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new TimberjackEventHandler());
    }
}
