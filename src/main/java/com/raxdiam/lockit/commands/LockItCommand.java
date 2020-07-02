package com.raxdiam.lockit.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.raxdiam.lockit.LockItAction;
import com.raxdiam.lockit.LockItLockResult;
import com.raxdiam.lockit.accessor.ILockableContainerBlockEntityAccessor;
import com.raxdiam.lockit.command.ICommand;
import com.raxdiam.lockit.text.PrefixedText;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Collection;

public class LockItCommand implements ICommand<ServerCommandSource> {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var mainLiteral = CommandManager.literal("lockit");
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
