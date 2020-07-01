package com.raxdiam.lockables.mixin;

import com.raxdiam.lockables.accessor.IAbstractBlockAccssor;
import com.raxdiam.lockables.accessor.ILockableContainerBlockEntityAccessor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin implements IAbstractBlockAccssor {
    @Shadow @Final
    protected AbstractBlock.Settings settings;

    @Override
    public AbstractBlock.Settings getSettings() {
        return settings;
    }

    @Inject(method = "calcBlockBreakingDelta", at = @At("HEAD"), cancellable = true)
    public void calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> info) {
        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LockableContainerBlockEntity) {
            var access = (ILockableContainerBlockEntityAccessor) blockEntity;
            var lockable = access.getLockable();
            if (lockable.isActive()) {
                info.setReturnValue(-1f);
                info.cancel();
            }
        }
    }
}
