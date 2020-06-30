package com.raxdiam.lockables.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.raxdiam.lockables.command.ICommand;
import com.raxdiam.lockables.util.LockableHelper;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

public class ShareCommand implements ICommand<ServerCommandSource> {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("share")
                .then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((commandContext, suggestionsBuilder) -> {
                    PlayerManager playerManager = ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getPlayerManager();
                    return CommandSource.suggestMatching(playerManager.getPlayerList().stream().map((serverPlayerEntity) -> {
                        return serverPlayerEntity.getGameProfile().getName();
                    }), suggestionsBuilder);
                }).executes(context -> run(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets")))));
    }

    public int run(ServerCommandSource source, Collection<GameProfile> targets) {
        var player = LockableHelper.getPlayerFromCommandSource(source);
        if (player == null) return 0;

        var blockEntity = LockableHelper.getTargetedBlockEntity(player);
        if (blockEntity instanceof LockableContainerBlockEntity) {
            for (var target : targets) {
                LockableHelper.shareWith((LockableContainerBlockEntity) blockEntity, player, target);
            }
            return 1;
        }

        return 0;
    }
}
