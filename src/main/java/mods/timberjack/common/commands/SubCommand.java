/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack.common.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Sub-Command
 *
 * Created by CovertJaguar on 3/12/2015.
 */
public abstract class SubCommand implements IModCommand {

    public enum PermLevel {

        EVERYONE(0), ADMIN(2);
        int permLevel;

        PermLevel(int permLevel) {
            this.permLevel = permLevel;
        }

    }

    private final String name;
    private final List<String> aliases = new ArrayList<String>();
    private PermLevel permLevel = PermLevel.EVERYONE;
    private IModCommand parent;
    private final SortedSet<SubCommand> children = new TreeSet<>(SubCommand::compareTo);

    protected SubCommand(String name) {
        this.name = name;
    }

    @Override
    public final String getCommandName() {
        return name;
    }

    public SubCommand addChildCommand(SubCommand child) {
        child.setParent(this);
        children.add(child);
        return this;
    }

    void setParent(IModCommand parent) {
        this.parent = parent;
    }

    @Override
    public SortedSet<SubCommand> getChildren() {
        return children;
    }

    public void addAlias(String alias) {
        aliases.add(alias);
    }

    @Override
    public List<String> getCommandAliases() {
        return aliases;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!CommandHelpers.executeStandardCommands(server, sender, this, args))
            executeSubCommand(server, sender, args);
    }

    public void executeSubCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        CommandHelpers.throwWrongUsage(sender, this);
    }

    public SubCommand setPermLevel(PermLevel permLevel) {
        this.permLevel = permLevel;
        return this;
    }

    @Override
    public final int getRequiredPermissionLevel() {
        return permLevel.permLevel;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canCommandSenderUseCommand(getRequiredPermissionLevel(), getCommandName());
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getFullCommandString() + " help";
    }

    @Override
    public void printHelp(ICommandSender sender) {
        CommandHelpers.printHelp(sender, this);
    }

    @Override
    public String getFullCommandString() {
        return parent.getFullCommandString() + " " + getCommandName();
    }

    @Override
    public int compareTo(ICommand command) {
        return getCommandName().compareTo(command.getCommandName());
    }

}