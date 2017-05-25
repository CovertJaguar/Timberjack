/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack.common.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This is the command that will be registered with Minecraft, it will delegate execution to our sub-commands.
 *
 * Created by CovertJaguar on 3/12/2015.
 */
public class RootCommand extends CommandBase implements IModCommand {
    public static final String ROOT_COMMAND_NAME = "timberjack";
    private final SortedSet<SubCommand> children = new TreeSet<>(SubCommand::compareTo);

    public void addChildCommand(SubCommand child) {
        child.setParent(this);
        children.add(child);
    }

    @Override
    public SortedSet<SubCommand> getChildren() {
        return children;
    }

    @Override
    public String getCommandName() {
        return ROOT_COMMAND_NAME;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("timber");
        aliases.add("tj");
        return aliases;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName() + " help";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!CommandHelpers.executeStandardCommands(server, sender, this, args))
            CommandHelpers.throwWrongUsage(sender, this);
    }

    @Override
    public String getFullCommandString() {
        return getCommandName();
    }

    @Override
    public void printHelp(ICommandSender sender) {
        CommandHelpers.printHelp(sender, this);
    }
}