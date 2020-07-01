package com.raxdiam.lockables.mixin;

import com.raxdiam.lockables.accessor.ILockableContainerBlockEntityAccessor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {
    @Inject(method = "getHardness", at = @At("HEAD"), cancellable = true)
    public void onGetHardness(BlockView world, BlockPos pos, CallbackInfoReturnable<Float> info) {
        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LockableContainerBlockEntity) {
            var access = (ILockableContainerBlockEntityAccessor) blockEntity;
            var lockable = access.getLockable();
            if (lockable.isActive()) info.setReturnValue(-1f);
        }
    }
}
