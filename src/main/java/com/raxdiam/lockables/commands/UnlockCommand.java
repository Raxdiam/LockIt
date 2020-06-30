package com.raxdiam.lockables.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.raxdiam.lockables.command.ICommand;
import com.raxdiam.lockables.text.PrefixedText;
import com.raxdiam.lockables.util.LockableHelper;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

public class UnlockCommand implements ICommand<ServerCommandSource> {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("unlock").executes(this::run));
    }

    public int run(CommandContext<ServerCommandSource> context) {
        return LockableHelper.setLockFromCommand(context.getSource(), false);
    }
}
