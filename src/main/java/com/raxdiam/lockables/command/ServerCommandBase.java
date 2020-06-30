package com.raxdiam.lockables.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.raxdiam.lockables.command.ICommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public abstract class ServerCommandBase implements ICommand<ServerCommandSource> {

}
