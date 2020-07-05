package com.raxdiam.lockit.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.raxdiam.lockit.LockItAction;
import com.raxdiam.lockit.LockItLockResult;
import com.raxdiam.lockit.LockItMod;
import com.raxdiam.lockit.accessor.ILockableContainerBlockEntityAccessor;
import com.raxdiam.lockit.command.ICommand;
import com.raxdiam.lockit.text.PrefixedText;
import com.raxdiam.lockit.text.TextBuilder;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.command.arguments.TeamArgumentType;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import org.apache.commons.lang3.tuple.Triple;

import static net.minecraft.server.command.CommandManager.*;
import static com.raxdiam.lockit.LockItAction.*;
import static net.minecraft.server.command.CommandSource.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LockItCommand implements ICommand<ServerCommandSource> {

    private final List<LiteralArgumentBuilder<ServerCommandSource>> subLiterals;

    public LockItCommand() {
        this.subLiterals = new ArrayList<>();
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var mainLiteral = literal("lockit");
        var aliasLiteral = literal("li");

        subLiterals.add(literal("lock").executes(context -> run(context.getSource(), LOCK, null)));
        subLiterals.add(literal("unlock").executes(context -> run(context.getSource(), UNLOCK, null)));
        subLiterals.add(literal("claim").executes(context -> run(context.getSource(), CLAIM, null)));
        subLiterals.add(literal("unclaim").executes(context -> run(context.getSource(), UNCLAIM, null)));

        var shareLiteral = literal("share");
        var sharePlayerLiteral = literal("player");
        var shareTeamLiteral = literal("team");

        sharePlayerLiteral.then(literal("add").then(argument("targets", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
            var playerManager = context.getSource().getMinecraftServer().getPlayerManager();
            return suggestMatching(playerManager.getPlayerList().stream().map((serverPlayerEntity) -> serverPlayerEntity.getGameProfile().getName()), builder);
        }).executes(context -> run(context.getSource(), SHAREPLAYER, GameProfileArgumentType.getProfileArgument(context, "targets")))));

        sharePlayerLiteral.then(literal("remove").then(argument("targets", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
            var pbResult = getPlayerAndBlock(context.getSource());
            if (!pbResult.getLeft()) return Suggestions.empty();
            var shared = pbResult.getRight().getLockit().getPlayersList();
            var userCache = context.getSource().getMinecraftServer().getUserCache();
            return suggestMatching(shared.stream().map(uuid -> userCache.getByUuid(uuid).getName()), builder);
        }).executes(context -> run(context.getSource(), UNSHAREPLAYER, GameProfileArgumentType.getProfileArgument(context, "targets")))));

        sharePlayerLiteral.then(literal("clear").executes(context -> run(context.getSource(), CLEARPLAYERS, null)));
        sharePlayerLiteral.then(literal("list").executes(context -> run(context.getSource(), LISTPLAYERS, null)));

        shareTeamLiteral.then(literal("add").then(argument("teamName", TeamArgumentType.team()).suggests((context, builder) -> {
            var scoreboard = context.getSource().getWorld().getScoreboard();
            return suggestMatching(scoreboard.getTeams().stream().map(team -> team.getName()), builder);
        }).executes(context -> run(context.getSource(), SHARETEAM, TeamArgumentType.getTeam(context, "teamName")))));

        shareTeamLiteral.then(literal("remove").then(argument("teamName", TeamArgumentType.team()).suggests((context, builder) -> {
            var pbResult = getPlayerAndBlock(context.getSource());
            if (!pbResult.getLeft()) return Suggestions.empty();
            var accessor = pbResult.getRight();
            var teams = accessor.getLockit().getTeamsList();

            var scoreboard = context.getSource().getWorld().getScoreboard();
            return suggestMatching(teams.stream().map(t -> scoreboard.getTeam(t).getName()), builder);
        }).executes(context -> run(context.getSource(), UNSHARETEAM, TeamArgumentType.getTeam(context, "teamName")))));

        shareTeamLiteral.then(literal("clear").executes(context -> run(context.getSource(), CLEARTEAMS, null)));
        shareTeamLiteral.then(literal("list").executes(context -> run(context.getSource(), LISTTEAMS, null)));

        shareLiteral.then(sharePlayerLiteral);
        shareLiteral.then(shareTeamLiteral);

        subLiterals.add(shareLiteral);

        for (var subLiteral : subLiterals) {
            mainLiteral.then(subLiteral);
            aliasLiteral.then(subLiteral);
        }

        dispatcher.register(mainLiteral);
        dispatcher.register(aliasLiteral);
    }

    public static <T> int run(ServerCommandSource source, LockItAction action, T extra) {
        var result = getPlayerAndBlock(source);
        if (!result.getLeft()) return 0;

        switch (action) {
            case LOCK:
                return processResult(source, result.getRight().lock(result.getMiddle()));
            case UNLOCK:
                return processResult(source, result.getRight().unlock(result.getMiddle()));
            case CLAIM:
                return processResult(source, result.getRight().claim(result.getMiddle()));
            case UNCLAIM:
                return processResult(source, result.getRight().unclaim(result.getMiddle()));
            case SHAREPLAYER:
                for (var profile : (Collection<GameProfile>) extra)
                    processResult(source, result.getRight().share(result.getMiddle(), profile));
                return 1;
            case SHARETEAM:
                return processResult(source, result.getRight().share(result.getMiddle(), (Team) extra));
            case UNSHAREPLAYER:
                for (var profile : (Collection<GameProfile>) extra)
                    processResult(source, result.getRight().unshare(result.getMiddle(), profile));
                return 1;
            case UNSHARETEAM:
                return processResult(source, result.getRight().unshare(result.getMiddle(), (Team) extra));
            case CLEARPLAYERS:
                return processResult(source, result.getRight().clearPlayers(result.getMiddle()));
            case CLEARTEAMS:
                return processResult(source, result.getRight().clearTeams(result.getMiddle()));
            case LISTPLAYERS: {
                if (!result.getRight().isOwner(result.getMiddle())) {
                    source.sendFeedback(PrefixedText.createLiteral("You cannot list the shared-with players of a container you do not own.", Formatting.RED), false);
                    return 0;
                }
                var userCache = source.getMinecraftServer().getUserCache();
                var players = result.getRight().getLockit().getPlayers();
                if (players.length == 0) {
                    source.sendFeedback(PrefixedText.createLiteral("This container is not shared with any players.", Formatting.YELLOW), false);
                    return 0;
                }

                var txtList = "";
                for (var uuid : players) {
                    var profile = userCache.getByUuid(uuid);
                    txtList += "- " + profile.getName() + "\n";
                }
                txtList = txtList.substring(0, txtList.length() - 1);

                var msg = TextBuilder.literal("[", Formatting.GRAY)
                        .append(TextBuilder.literal(LockItMod.LOGGER.getName(), Formatting.GREEN)
                                .append(TextBuilder.literal("]", Formatting.GRAY)
                                        .append(TextBuilder.literal(" Shared-with Players:\n", Formatting.WHITE)
                                                .append(TextBuilder.literal(txtList))))).build();
                source.sendFeedback(msg, false);
                return 1;
            }
            case LISTTEAMS: {
                if (!result.getRight().isOwner(result.getMiddle())) {
                    source.sendFeedback(PrefixedText.createLiteral("You cannot list the shared-with teams of a container you do not own.", Formatting.RED), false);
                    return 0;
                }
                var teams = result.getRight().getLockit().getTeams();
                if (teams.length == 0) {
                    source.sendFeedback(PrefixedText.createLiteral("This container is not shared with any teams.", Formatting.YELLOW), false);
                    return 0;
                }

                var txtList = "";
                for (var name : teams) {
                    txtList += "- " + name + "\n";
                }
                txtList = txtList.substring(0, txtList.length() - 1);

                var msg = TextBuilder.literal("[", Formatting.GRAY)
                        .append(TextBuilder.literal(LockItMod.LOGGER.getName(), Formatting.GREEN)
                                .append(TextBuilder.literal("]", Formatting.GRAY)
                                        .append(TextBuilder.literal(" Shared-with Teams:\n", Formatting.WHITE)
                                                .append(TextBuilder.literal(txtList))))).build();
                source.sendFeedback(msg, false);
                return 1;
            }
        }

        return 0;
    }

    private static int processResult(ServerCommandSource source, LockItLockResult result) {
        if (result.isSuccess()) {
            source.sendFeedback(result.getMessage(), false);
            return 1;
        }

        source.sendFeedback(result.getMessage(), false);
        return 0;
    }

    private static Triple<Boolean, ServerPlayerEntity, ILockableContainerBlockEntityAccessor> getPlayerAndBlock(ServerCommandSource source) {
        var success = false;
        var player = getPlayer(source);
        var accessor = player == null ? null : getTargetedLockable(source, player);

        if (player != null && accessor != null) success = true;
        return Triple.of(success, player, accessor);
    }

    private static ILockableContainerBlockEntityAccessor getTargetedLockable(ServerCommandSource source, ServerPlayerEntity player) {
        var serverWorld = player.getServerWorld();
        var blockHitResult = (BlockHitResult) player.rayTrace(10.0D, 1.0F, false);
        var blockPos = blockHitResult.getBlockPos();
        var blockEntity = serverWorld.getBlockEntity(blockPos);
        var lockableBlockEntity = blockEntity instanceof LockableContainerBlockEntity ?
                (LockableContainerBlockEntity) blockEntity :
                null;
        if (lockableBlockEntity == null)
            source.sendFeedback(PrefixedText.createLiteral("You must be looking at a container block!", Formatting.RED), false);
        return (ILockableContainerBlockEntityAccessor) lockableBlockEntity;
    }

    private static ServerPlayerEntity getPlayer(ServerCommandSource source) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayer();
        } catch (CommandSyntaxException e) {
            source.sendFeedback(PrefixedText.createLiteral("Cannot find player.", Formatting.RED), false);
            return null;
        }
        return player;
    }
}
