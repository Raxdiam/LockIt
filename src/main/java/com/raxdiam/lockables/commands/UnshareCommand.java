package com.raxdiam.lockables.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.raxdiam.lockables.accessor.ILockableContainerBlockEntityAccessor;
import com.raxdiam.lockables.command.ICommand;
import com.raxdiam.lockables.util.LockableHelper;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

public class UnshareCommand implements ICommand<ServerCommandSource> {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("unshare")
                .then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((commandContext, suggestionsBuilder) -> {
                    var source = commandContext.getSource();
                    var player = LockableHelper.getPlayerFromCommandSource(source);
                    var blockEntity = LockableHelper.getTargetedBlockEntity(player);
                    if (!(blockEntity instanceof LockableContainerBlockEntity)) {
                        return CommandSource.suggestMatching(new String[]{}, suggestionsBuilder);
                    }

                    var accessor = (ILockableContainerBlockEntityAccessor) blockEntity;
                    var shared = accessor.getLockable().getShared();

                    PlayerManager playerManager = ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getPlayerManager();
                    return CommandSource.suggestMatching(shared.stream().map(s -> {
                        return playerManager.getPlayer(UUID.fromString(s)).getGameProfile().getName();
                    }), suggestionsBuilder);
                }).executes(context -> run(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets")))));
    }

    public int run(ServerCommandSource source, Collection<GameProfile> targets) {
        var player = LockableHelper.getPlayerFromCommandSource(source);
        if (player == null) return 0;

        var blockEntity = LockableHelper.getTargetedBlockEntity(player);
        if (blockEntity instanceof LockableContainerBlockEntity) {
            for (var target : targets) {
                LockableHelper.unshareWith((LockableContainerBlockEntity) blockEntity, player, target);
            }
            return 1;
        }

        return 0;
    }
}
