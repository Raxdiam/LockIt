package com.raxdiam.lockables.util;

import com.mojang.brigadier.CommandDispatcher;
import com.raxdiam.lockables.command.ICommand;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandSource;

import java.util.LinkedList;

public class CommandHelper {
    private static LinkedList<ICommand> commands = new LinkedList<>();

    public static <S extends CommandSource> void register(ICommand<S> command) {
        CommandRegistrationCallback.EVENT.register((dispatcher, b) -> command.register((CommandDispatcher<S>) dispatcher));
        commands.add(command);
    }
}
