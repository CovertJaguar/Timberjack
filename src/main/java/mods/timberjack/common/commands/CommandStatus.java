/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */
package mods.timberjack.common.commands;

import mods.timberjack.common.felling.FellingManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * Commands for testing, because it was too much effort to find another mod that did them.
 * Created by CovertJaguar on 3/12/2015.
 */
public class CommandStatus extends SubCommand {

    public CommandStatus() {
        super("status");
    }

    @Override
    public void executeSubCommand(MinecraftServer server, ICommandSender sender, String[] args) {
        World world = CommandHelpers.getWorld(sender);
        FellingManager fellingManager = FellingManager.fellingManagers.get(world);
        if (fellingManager == null) {
            sender.addChatMessage(new TextComponentString("No Felling Manager active, this is normal."));
        } else {
            sender.addChatMessage(new TextComponentString("Felling Manager active:"));
            sender.addChatMessage(new TextComponentString(String.format("- Trees queued: %d", fellingManager.treesQueuedToFell())));
            sender.addChatMessage(new TextComponentString(String.format("- Logs queued: %d", fellingManager.logsQueuedToFell())));
        }
    }

}
