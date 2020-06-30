package com.raxdiam.lockables.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.raxdiam.lockables.text.PrefixedText;
import com.raxdiam.lockables.command.ICommand;
import com.raxdiam.lockables.util.LockableHelper;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

public class LockCommand implements ICommand<ServerCommandSource> {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("lock").executes(this::run));
    }

    public int run(CommandContext<ServerCommandSource> context) {
        return LockableHelper.setLockFromCommand(context.getSource(), true);
        /*var source = context.getSource();
        var player = LockableHelper.getPlayerFromCommandSource(source);
        if (player == null) return 0;

        *//*var blockHitResult = (BlockHitResult) player.rayTrace(10.0D, 1.0F, false);
        if (blockHitResult == null) {
            source.sendFeedback(PrefixedText.createLiteral("You must be looking at a container block!", Formatting.RED), false);
            return 0;
        }

        var blockPos = blockHitResult.getBlockPos();
        var serverWorld = source.getWorld();
        var blockState = serverWorld.getBlockState(blockPos);
        var blockEntity = serverWorld.getBlockEntity(blockPos);
        var block = blockState.getBlock();*//*

        var blockEntity = LockableHelper.getTargetedBlockEntity(player);

        if (blockEntity instanceof LockableContainerBlockEntity) {
            //LOGGER.info("Found a chest under crosshair at " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ());

            LockableHelper.lock((LockableContainerBlockEntity) blockEntity, player);

            //source.sendFeedback(PrefixedText.createLiteral("Container locked!", Formatting.GREEN), false);
            return 1;
        }
        else {
            source.sendFeedback(PrefixedText.createLiteral("You must be looking at a container block!", Formatting.RED), false);
        }

        return 0;*/
    }
}
