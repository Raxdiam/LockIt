package com.raxdiam.lockit.accessor;

import com.mojang.authlib.GameProfile;
import com.raxdiam.lockit.LockItLock;
import com.raxdiam.lockit.LockItLockResult;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ILockableContainerBlockEntityAccessor {
    boolean canAccess(ServerPlayerEntity player);
    LockItLockResult lock(ServerPlayerEntity player);
    LockItLockResult unlock(ServerPlayerEntity player);
    LockItLockResult claim(ServerPlayerEntity player);
    LockItLockResult unclaim(ServerPlayerEntity player);
    LockItLockResult share(ServerPlayerEntity player, GameProfile target);
    LockItLockResult share(ServerPlayerEntity player, Team team);
    LockItLockResult unshare(ServerPlayerEntity player, GameProfile target);
    LockItLockResult unshare(ServerPlayerEntity player, Team team);
    LockItLock getLockable();
}
