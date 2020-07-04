package com.raxdiam.lockit.mixin;

import com.raxdiam.lockit.accessor.ILockableContainerBlockEntityAccessor;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Supplier;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    @Inject(method = "insertAndExtract", at = @At("HEAD"), cancellable = true)
    private void insertAndExtract(Supplier<Boolean> extractMethod, CallbackInfoReturnable<Boolean> info) {
        var me = (HopperBlockEntity) (Object) this;
        var world = me.getWorld();
        var outDir = me.getCachedState().get(HopperBlock.FACING);
        var outPos = me.getPos().offset(outDir);
        var outBlockEntity = world.getBlockEntity(outPos);

        var inPos = new BlockPos(me.getHopperX(), me.getHopperY() + 1.0D, me.getHopperZ());
        var inBlockEntity = world.getBlockEntity(inPos);

        if (outBlockEntity instanceof LockableContainerBlockEntity) {
            if (((ILockableContainerBlockEntityAccessor) outBlockEntity).getLockit().isActive()) {
                info.setReturnValue(false);
                info.cancel();
            }
        }

        if (inBlockEntity instanceof LockableContainerBlockEntity) {
            if (((ILockableContainerBlockEntityAccessor) inBlockEntity).getLockit().isActive()) {
                info.setReturnValue(false);
                info.cancel();
            }
        }
    }
}
