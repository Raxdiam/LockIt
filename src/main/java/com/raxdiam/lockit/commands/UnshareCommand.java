package com.raxdiam.lockit.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.raxdiam.lockit.accessor.ILockableContainerBlockEntityAccessor;
import com.raxdiam.lockit.command.ICommand;
import com.raxdiam.lockit.util.LockItHelper;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

public class UnshareCommand implements ICommand<ServerCommandSource> {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("unshare")
                .then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((commandContext, suggestionsBuilder) -> {
                    var source = commandContext.getSource();
                    var player = LockItHelper.getPlayerFromCommandSource(source);
                    var blockEntity = LockItHelper.getTargetedBlockEntity(player);
                    if (!(blockEntity instanceof LockableContainerBlockEntity)) {
                        return CommandSource.suggestMatching(new String[]{}, suggestionsBuilder);
                    }

                    var accessor = (ILockableContainerBlockEntityAccessor) blockEntity;
                    var shared = accessor.getLockable().getSharedList();

                    PlayerManager playerManager = ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getPlayerManager();
                    return CommandSource.suggestMatching(shared.stream().map(s -> {
                        return playerManager.getPlayer(s).getGameProfile().getName();
                    }), suggestionsBuilder);
                }).executes(context -> run(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets")))));
    }

    public int run(ServerCommandSource source, Collection<GameProfile> targets) {
        var player = LockItHelper.getPlayerFromCommandSource(source);
        if (player == null) return 0;

        var blockEntity = LockItHelper.getTargetedBlockEntity(player);
        if (blockEntity instanceof LockableContainerBlockEntity) {
            for (var target : targets) {
                LockItHelper.unshareWith((LockableContainerBlockEntity) blockEntity, player, target);
            }
            return 1;
        }

        return 0;
    }
}
