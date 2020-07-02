package com.raxdiam.lockit.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.raxdiam.lockit.command.ICommand;
import com.raxdiam.lockit.util.LockItHelper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class UnlockCommand implements ICommand<ServerCommandSource> {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("unlock").executes(this::run));
    }

    public int run(CommandContext<ServerCommandSource> context) {
        return LockItHelper.setLockFromCommand(context.getSource(), false);
    }
}
