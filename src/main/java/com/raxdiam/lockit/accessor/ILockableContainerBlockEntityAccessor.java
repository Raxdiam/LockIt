package com.raxdiam.lockit.accessor;

import com.mojang.authlib.GameProfile;
import com.raxdiam.lockit.LockItLock;
import com.raxdiam.lockit.LockItLockResult;
import com.raxdiam.lockit.commands.LockItCommand;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ILockableContainerBlockEntityAccessor {
    boolean canAccess(ServerPlayerEntity player);
    boolean isOwner(ServerPlayerEntity player);
    boolean hasOwner();
    LockItLockResult lock(ServerPlayerEntity player);
    LockItLockResult unlock(ServerPlayerEntity player);
    LockItLockResult claim(ServerPlayerEntity player);
    LockItLockResult unclaim(ServerPlayerEntity player);
    LockItLockResult unclaim();
    LockItLockResult share(ServerPlayerEntity player, GameProfile target);
    LockItLockResult share(ServerPlayerEntity player, Team team);
    LockItLockResult unshare(ServerPlayerEntity player, GameProfile target);
    LockItLockResult unshare(ServerPlayerEntity player, Team team);
    LockItLockResult clearPlayers(ServerPlayerEntity player);
    LockItLockResult clearTeams(ServerPlayerEntity player);
    LockItLock getLockit();

    enum LockMessage {
        NOT_OWNER("You do not own this container."),
        NO_OWNER("This container has no owner.");

        private String message;

        LockMessage(String message) {
            this.message = message;
        }

        public String get() {
            return this.message;
        }
    }
}
