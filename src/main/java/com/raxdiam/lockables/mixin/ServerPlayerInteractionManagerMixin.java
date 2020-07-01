package com.raxdiam.lockables.mixin;

import com.raxdiam.lockables.accessor.ILockableContainerBlockEntityAccessor;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow public ServerWorld world;

    /*@Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    public void onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        var blockEntity = this.world.getBlockEntity(pos);
        if (blockEntity instanceof LockableContainerBlockEntity) {
            var access = (ILockableContainerBlockEntityAccessor) blockEntity;
            var lockable = access.getLockable();
            if (lockable.isActive()) info.setReturnValue(false);
        }
    }*/

    @Inject(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;canPlayerModifyAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;)Z"), cancellable = true)
    public void onProcessBlockBreakingAction(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo info) {
        var blockEntity = this.world.getBlockEntity(pos);
        if (blockEntity instanceof LockableContainerBlockEntity) {
            var access = (ILockableContainerBlockEntityAccessor) blockEntity;
            var lockable = access.getLockable();
            if (lockable.isActive()) info.cancel();
        }
    }
}
