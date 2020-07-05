package com.raxdiam.lockit.mixin;

import com.raxdiam.lockit.accessor.ILockableContainerBlockEntityAccessor;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "isBlockBreakingRestricted", at = @At("HEAD"), cancellable = true)
    public void onIsBlockBreakingRestricted(World world, BlockPos pos, GameMode gameMode, CallbackInfoReturnable<Boolean> info) {
        var blockEntity = world.getBlockEntity(pos);
        var me = (PlayerEntity) (Object) this;
        if (!me.isCreativeLevelTwoOp() && blockEntity instanceof LockableContainerBlockEntity && ((ILockableContainerBlockEntityAccessor) blockEntity).getLockit().isActive()) {
            info.setReturnValue(true);
            info.cancel();
        }
    }
}
