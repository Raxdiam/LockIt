package com.raxdiam.lockables.util;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.raxdiam.lockables.accessor.ILockableContainerBlockEntityAccessor;
import com.raxdiam.lockables.text.PrefixedText;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.concurrent.locks.Lock;

/*
* I know that this is probably not the best way to go about organizing things but
* my highest priority is for everything to just work right now with minimal repeated code and easy access.
*
* If you're reading this, you can let me know why this is wrong and what I should do to fix it.
*/
public class LockableHelper {
    public static void lock(LockableContainerBlockEntity blockEntity, ServerPlayerEntity player) {
        access(blockEntity).lock(player);
    }

    public static void unlock(LockableContainerBlockEntity blockEntity, ServerPlayerEntity player) {
        access(blockEntity).unlock(player);
    }

    public static void shareWith(LockableContainerBlockEntity blockEntity, ServerPlayerEntity player, GameProfile target) {
        access(blockEntity).shareWith(player, target);
    }

    public static void unshareWith(LockableContainerBlockEntity blockEntity, ServerPlayerEntity player, GameProfile target) {
        access(blockEntity).shareRemove(player, target);
    }

    public static int setLockFromCommand(ServerCommandSource source, boolean locked) {
        var player = LockableHelper.getPlayerFromCommandSource(source);
        if (player == null) return 0;

        var blockEntity = LockableHelper.getTargetedBlockEntity(player);

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
