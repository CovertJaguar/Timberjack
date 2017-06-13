/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack.common;

import mods.timberjack.common.commands.CommandStatus;
import mods.timberjack.common.commands.RootCommand;
import mods.timberjack.common.entity.EntityTimber;
import mods.timberjack.common.felling.TimberjackEventHandler;
import net.minecraft.command.CommandHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

@Mod(modid = Timberjack.MOD_ID, version = Timberjack.VERSION)
public class Timberjack {
    static final String MOD_ID = "timberjack";
    static final String VERSION = "@VERSION@";
    @SidedProxy(clientSide = "mods.timberjack.client.ProxyClient", serverSide = "mods.timberjack.common.Proxy")
    public static Proxy proxy;
//    @Mod.Instance(MOD_ID)
//    public static Timberjack instance;

    private static final RootCommand rootCommand = new RootCommand();

    static {
        rootCommand.addChildCommand(new CommandStatus());
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        TimberjackConfig.load(event.getModConfigurationDirectory());

        EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "falling_block"), EntityTimber.class, "timber", 0, this, 160, 20, true);

        proxy.preInit();
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
