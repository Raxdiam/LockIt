package com.raxdiam.lockables.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;

public interface ICommand<S extends CommandSource> {
    void register(CommandDispatcher<S> dispatcher);
}
