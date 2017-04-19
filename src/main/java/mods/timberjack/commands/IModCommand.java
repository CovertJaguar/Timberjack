/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack.commands;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

import java.util.SortedSet;

/**
 * Our commands will have a few more methods.
 *
 * Created by CovertJaguar on 3/12/2015.
 */
public interface IModCommand extends ICommand {

    String getFullCommandString();

    int getRequiredPermissionLevel();

    SortedSet<SubCommand> getChildren();

    void printHelp(ICommandSender sender);
}