package com.raxdiam.lockables.mixin;

import com.raxdiam.lockables.LockablesMod;
import com.raxdiam.lockables.accessor.ILockableContainerBlockEntityAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {

    /*@Inject(method = "getBlastResistance", at = @At("HEAD"), cancellable = true)
    public void onGetBlastResistance(CallbackInfoReturnable<Float> info) {

        *//*LockablesMod.SERVER.getWorld()
        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LockableContainerBlockEntity) {
            var access = (ILockableContainerBlockEntityAccessor) blockEntity;
            var lockable = access.getLockable();
            if (lockable.isActive()) info.setReturnValue(-1f);
        }*//*
    }*/


}
