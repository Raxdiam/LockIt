package com.raxdiam.lockit.util;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.raxdiam.lockit.accessor.ILockableContainerBlockEntityAccessor;
import com.raxdiam.lockit.text.PrefixedText;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;

public class LockItHelper {
    public static void lock(LockableContainerBlockEntity blockEntity, ServerPlayerEntity player) {
        access(blockEntity).lock(player);
    }

    public static void unlock(LockableContainerBlockEntity blockEntity, ServerPlayerEntity player) {
        access(blockEntity).unlock(player);
    }

    public static void shareWith(LockableContainerBlockEntity blockEntity, ServerPlayerEntity player, GameProfile target) {
        access(blockEntity).share(player, target);
    }

    public static void shareWith(LockableContainerBlockEntity blockEntity, ServerPlayerEntity player, Team team) {
        access(blockEntity).share(player, team);
    }

    public static void unshareWith(LockableContainerBlockEntity blockEntity, ServerPlayerEntity player, GameProfile target) {
        access(blockEntity).unshare(player, target);
    }

    public static void unshareWith(LockableContainerBlockEntity blockEntity, ServerPlayerEntity player, Team team) {
        access(blockEntity).unshare(player, team);
    }

    public static int setLockFromCommand(ServerCommandSource source, boolean locked) {
        var player = LockItHelper.getPlayerFromCommandSource(source);
        if (player == null) return 0;

        var blockEntity = LockItHelper.getTargetedBlockEntity(player);

        if (blockEntity instanceof LockableContainerBlockEntity) {
            if (locked) lock((LockableContainerBlockEntity) blockEntity, player);
            else unlock((LockableContainerBlockEntity) blockEntity, player);

            return 1;
        } else {
            source.sendFeedback(PrefixedText.createLiteral("You must be looking at a container block!", Formatting.RED), false);
        }

        return 0;
    }

    public static BlockEntity getTargetedBlockEntity(ServerPlayerEntity player) {
        var serverWorld = player.getServerWorld();
        var blockHitResult = (BlockHitResult) player.rayTrace(10.0D, 1.0F, false);
        var blockPos = blockHitResult.getBlockPos();
        var blockEntity = serverWorld.getBlockEntity(blockPos);
        return blockEntity;
    }

    public static ServerPlayerEntity getPlayerFromCommandSource(ServerCommandSource source) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayer();
        } catch (CommandSyntaxException e) {
            source.sendFeedback(PrefixedText.createLiteral("Cannot find player.", Formatting.RED), false);
            return null;
        }
        return player;
    }

    private static ILockableContainerBlockEntityAccessor access(LockableContainerBlockEntity blockEntity) {
        return (ILockableContainerBlockEntityAccessor) blockEntity;
    }
}
