package com.raxdiam.lockables.accessor;

import com.mojang.authlib.GameProfile;
import com.raxdiam.lockables.LockableLock;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ILockableContainerBlockEntityAccessor {
    boolean canAccess(ServerPlayerEntity player);
    void lock(ServerPlayerEntity player);
    void unlock(ServerPlayerEntity player);
    void shareWith(ServerPlayerEntity player, GameProfile target);
    void shareRemove(ServerPlayerEntity player, GameProfile target);
    LockableLock getLockable();
}
