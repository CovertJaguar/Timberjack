/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack;

import mods.timberjack.commands.CommandStatus;
import mods.timberjack.commands.RootCommand;
import net.minecraft.command.CommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Timberjack.MOD_ID, version = Timberjack.VERSION)
public class Timberjack {
    static final String MOD_ID = "timberjack";
    static final String VERSION = "@VERSION@";
//    @Mod.Instance(MOD_ID)
//    public static Timberjack instance;

    private static final RootCommand rootCommand = new RootCommand();

    static {
        rootCommand.addChildCommand(new CommandStatus());
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        TimberjackConfig.load(event.getModConfigurationDirectory());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new TimberjackEventHandler());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        CommandHandler commandManager = (CommandHandler) event.getServer().getCommandManager();
        commandManager.registerCommand(rootCommand);
    }

}
