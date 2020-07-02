package com.raxdiam.lockit.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.raxdiam.lockit.command.ICommand;
import com.raxdiam.lockit.util.LockItHelper;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.command.arguments.TeamArgumentType;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

public class ShareCommand implements ICommand<ServerCommandSource> {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        /*dispatcher.register(CommandManager.literal("share")
                .then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((commandContext, suggestionsBuilder) -> {
                    PlayerManager playerManager = ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getPlayerManager();
                    return CommandSource.suggestMatching(playerManager.getPlayerList().stream().map((serverPlayerEntity) -> {
                        return serverPlayerEntity.getGameProfile().getName();
                    }), suggestionsBuilder);
                }).executes(context -> run(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets")))));*/
        var main = CommandManager.literal("share");



        main.then(CommandManager.literal("player")
                .then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
                    var playerManager = context.getSource().getMinecraftServer().getPlayerManager();
                    return CommandSource.suggestMatching(playerManager.getPlayerList().stream().map((serverPlayerEntity) -> {
                        return serverPlayerEntity.getGameProfile().getName();
                    }), builder);
                }).executes(context -> runPlayer(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets")))));

        main.then(CommandManager.literal("team")
                .then(CommandManager.argument("teamName", TeamArgumentType.team()).suggests((context, builder) -> {
                    var scoreboard = context.getSource().getWorld().getScoreboard();
                    return CommandSource.suggestMatching(scoreboard.getTeams().stream().map(team -> {
                        return team.getName();
                    }), builder);
                }).executes(context -> runTeam(context.getSource(), TeamArgumentType.getTeam(context, "teamName")))));

        dispatcher.register(main);
    }

    public int runPlayer(ServerCommandSource source, Collection<GameProfile> targets) {
        var player = LockItHelper.getPlayerFromCommandSource(source);
        if (player == null) return 0;

        var blockEntity = LockItHelper.getTargetedBlockEntity(player);
        if (blockEntity instanceof LockableContainerBlockEntity) {
            for (var target : targets) {
                LockItHelper.shareWith((LockableContainerBlockEntity) blockEntity, player, target);
            }
            return 1;
        }

        return 0;
    }

    public int runTeam(ServerCommandSource source, Team team) {
        var player = LockItHelper.getPlayerFromCommandSource(source);
        if (player == null) return 0;

        var blockEntity = LockItHelper.getTargetedBlockEntity(player);
        if (blockEntity instanceof LockableContainerBlockEntity) {
            LockItHelper.shareWith((LockableContainerBlockEntity) blockEntity, player, team);
            return 1;
        }

        return 0;
    }
}
