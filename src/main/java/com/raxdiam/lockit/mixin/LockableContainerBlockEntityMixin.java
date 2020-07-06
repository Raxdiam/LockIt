package com.raxdiam.lockit.mixin;

import com.mojang.authlib.GameProfile;
import com.raxdiam.lockit.LockItLock;
import com.raxdiam.lockit.LockItLockResult;
import com.raxdiam.lockit.accessor.ILockableContainerBlockEntityAccessor;
import com.raxdiam.lockit.text.PrefixedText;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.raxdiam.lockit.LockItAction.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(value = LockableContainerBlockEntity.class)
public class LockableContainerBlockEntityMixin implements ILockableContainerBlockEntityAccessor {
    private LockItLock lockit = LockItLock.EMPTY;

    @Inject(method = "fromTag", at = @At("TAIL"))
    public void onFromTag(BlockState state, CompoundTag tag, CallbackInfo info) {
        this.lockit = LockItLock.fromTag(tag);
    }

    @Inject(method = "toTag", at = @At("TAIL"))
    public void onToTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> info) {
        this.lockit.toTag(tag);
    }

    @Inject(method = "checkUnlocked", at = @At("HEAD"), cancellable = true)
    public void onCheckUnlocked(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
        if (!player.isSpectator() && !this.canAccess((ServerPlayerEntity) player)) {
            player.sendMessage(PrefixedText.createLiteral("This container is locked.", Formatting.RED), false);
            player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            info.setReturnValue(false);
        }
    }

    @Override
    public boolean canAccess(ServerPlayerEntity player) {
        if (!this.lockit.getOwner().isEmpty()) {
            var owner = this.lockit.getOwner().get();
            if (this.lockit.isActive()) {
                if (owner.equals(player.getUuid())) return true;
                else if (this.lockit.getPlayersList().contains(player.getUuid())) return true;
                else {
                    var playerTeam = player.getScoreboardTeam();
                    if (playerTeam != null) {
                        if (this.lockit.getTeamsList().contains(playerTeam.getName()))
                            return true;
                    }
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean isOwner(ServerPlayerEntity player) {
        return this.hasOwner() && this.lockit.getOwner().get().equals(player.getUuid());
    }

    @Override
    public boolean hasOwner() {
        return !this.lockit.getOwner().isEmpty();
    }

    @Override
    public LockItLockResult lock(ServerPlayerEntity player) {
        if (!this.hasOwner()) {
            this.modifyLock(true, player.getUuid());
            return LockItLockResult.success(LOCK, "Container claimed and locked!");
        } else if (this.isOwner(player)) {
            if (this.lockit.isActive()) {
                return LockItLockResult.failSoft(LOCK, "Container is already locked.");
            } else {
                this.modifyLock(true);
                return LockItLockResult.success(LOCK, "Container locked!");
            }
        } else {
            return LockItLockResult.fail(LOCK, LockMessage.NOT_OWNER.get());
        }
    }

    @Override
    public LockItLockResult unlock(ServerPlayerEntity player) {
        if (!this.hasOwner()) {
            return LockItLockResult.failSoft(UNLOCK, LockMessage.NO_OWNER.get());
        } else if (this.isOwner(player)) {
            if (this.lockit.isActive()) {
                this.modifyLock(false);
                return LockItLockResult.success(UNLOCK, "Container unlocked!");
            } else {
                return LockItLockResult.failSoft(UNLOCK, "Container is already unlocked.");
            }
        } else {
            return LockItLockResult.fail(UNLOCK, LockMessage.NOT_OWNER.get());
        }
    }

    @Override
    public LockItLockResult claim(ServerPlayerEntity player) {
        if (!this.hasOwner()) {
            this.modifyLock(player.getUuid());
            return LockItLockResult.success(CLAIM, "Container claimed!");
        } else if (this.isOwner(player)) {
            return LockItLockResult.failSoft(CLAIM, "You already own this container.");
        } else {
            return LockItLockResult.fail(CLAIM, "This container has already been claimed by someone else.");
        }
    }

    @Override
    public LockItLockResult unclaim(ServerPlayerEntity player) {
        if (!this.hasOwner()) {
            return LockItLockResult.failSoft(UNCLAIM, LockMessage.NO_OWNER.get());
        } else if (this.isOwner(player)) {
            this.lockit = LockItLock.EMPTY;
            return LockItLockResult.success(UNCLAIM, "Ownership of this container removed!");
        } else {
            return LockItLockResult.fail(UNCLAIM, LockMessage.NOT_OWNER.get());
        }
    }

    @Override
    public LockItLockResult unclaim() {
        if (!this.hasOwner()) {
            return LockItLockResult.failSoft(UNCLAIM, LockMessage.NO_OWNER.get());
        } else {
            this.lockit = LockItLock.EMPTY;
            return LockItLockResult.success(UNCLAIM, "Ownership of this container removed!");
        }
    }

    @Override
    public LockItLockResult share(ServerPlayerEntity player, GameProfile target) {
        if (!this.hasOwner()) {
            this.modifyLock(player.getUuid(), new UUID[] {target.getId()});
            return LockItLockResult.success(SHAREPLAYER, "Container claimed and shared with " + target.getName() + "!");
        } else if (this.isOwner(player)) {
            var current = new ArrayList<>(this.lockit.getPlayersList());
            if (player.getUuid().equals(target.getId())) {
                return LockItLockResult.failSoft(SHAREPLAYER, "You cannot share a container you own with yourself.");
            } else if (current.contains(target.getId())) {
                return LockItLockResult.failSoft(SHAREPLAYER, "This container is already shared with " + target.getName() + ".");
            } else {
                current.add(target.getId());
                this.modifyLock(toUuidArray(current));
                return LockItLockResult.success(SHAREPLAYER, "Container shared with " + target.getName() + "!");
            }
        } else {
            return LockItLockResult.fail(SHAREPLAYER, LockMessage.NOT_OWNER.get());
        }
    }

    @Override
    public LockItLockResult share(ServerPlayerEntity player, Team team) {
        if (!this.hasOwner()) {
            return LockItLockResult.failSoft(SHARETEAM, LockMessage.NO_OWNER.get());
        } else if (this.isOwner(player)) {
            var current = new ArrayList<>(this.lockit.getTeamsList());
            if (current.contains(team.getName())) {
                return LockItLockResult.failSoft(SHARETEAM, "This container is already shared with team " + team.getName() + ".");
            } else {
                current.add(team.getName());
                this.modifyLock(toStringArray(current));
                return LockItLockResult.success(SHARETEAM, "This container is now shared with team " + team.getName() + "!");
            }
        } else {
            return LockItLockResult.fail(SHARETEAM, LockMessage.NOT_OWNER.get());
        }
    }

    @Override
    public LockItLockResult unshare(ServerPlayerEntity player, GameProfile target) {
        if (!this.hasOwner()) {
            return LockItLockResult.failSoft(UNSHAREPLAYER, LockMessage.NO_OWNER.get());
        } else if (this.isOwner(player)) {
            var current = new ArrayList<>(this.lockit.getPlayersList());
            if (current.contains(target.getId())) {
                current.remove(target.getId());
                this.modifyLock(toUuidArray(current));
                return LockItLockResult.success(UNSHAREPLAYER, "Container no loncger shared with " + target.getName() + "!");
            } else {
                return LockItLockResult.failSoft(UNSHAREPLAYER, "This container is not shared with " + target.getName() + ".");
            }
        } else {
            return LockItLockResult.fail(UNSHAREPLAYER, LockMessage.NOT_OWNER.get());
        }
    }

    @Override
    public LockItLockResult unshare(ServerPlayerEntity player, Team team) {
        if (!this.hasOwner()) {
            return LockItLockResult.failSoft(SHARETEAM, LockMessage.NO_OWNER.get());
        } else if (this.isOwner(player)) {
            var current = new ArrayList<>(this.lockit.getTeamsList());
            if (current.contains(team.getName())) {
                current.remove(team.getName());
                this.modifyLock(toStringArray(current));
                return LockItLockResult.success(SHARETEAM, "This container is no longer shared with team " + team.getName() + "!");
            } else {
                return LockItLockResult.failSoft(SHARETEAM, "This container is not shared with team " + team.getName() + ".");
            }
        } else {
            return LockItLockResult.fail(SHARETEAM, LockMessage.NOT_OWNER.get());
        }
    }

    @Override
    public LockItLockResult clearPlayers(ServerPlayerEntity player) {
        if (!this.hasOwner()) {
            return LockItLockResult.failSoft(CLEARPLAYERS, LockMessage.NO_OWNER.get());
        } else if (this.isOwner(player)) {
            this.modifyLock(new UUID[]{});
            return LockItLockResult.success(CLEARPLAYERS, "All shared-with players cleared from this container.");
        } else {
            return LockItLockResult.fail(CLEARPLAYERS, LockMessage.NOT_OWNER.get());
        }
    }

    @Override
    public LockItLockResult clearTeams(ServerPlayerEntity player) {
        if (!this.hasOwner()) {
            return LockItLockResult.failSoft(CLEARTEAMS, LockMessage.NO_OWNER.get());
        } else if (this.isOwner(player)) {
            this.modifyLock(new String[] {});
            return LockItLockResult.success(CLEARTEAMS, "All shared-with teams cleared from this container.");
        } else {
            return LockItLockResult.fail(CLEARTEAMS, LockMessage.NOT_OWNER.get());
        }
    }

    @Override
    public LockItLock getLockit() {
        return this.lockit;
    }

    private void modifyLock(boolean active, UUID owner, UUID[] players, String[] teams) {
        this.lockit = LockItLock.create(active, owner, players, teams);
    }

    private void modifyLock(boolean active, UUID owner) {
        this.modifyLock(active, owner, this.lockit.getPlayers(), this.lockit.getTeams());
    }

    private void modifyLock(UUID owner, UUID[] players, String[] teams) {
       this.modifyLock(this.lockit.isActive(), owner, players, teams);
    }

    private void modifyLock(UUID owner) {
        this.modifyLock(this.lockit.isActive(), owner);
    }

    private void modifyLock(boolean active, UUID[] players, String[] teams) {
        this.modifyLock(active, this.lockit.getOwner().get(), players, teams);
    }

    private void modifyLock(boolean active) {
        this.modifyLock(active, this.lockit.getOwner().get());
    }

    private void modifyLock(boolean active, UUID owner, UUID[] players) {
        this.modifyLock(active, owner, players, this.lockit.getTeams());
    }

    private void modifyLock(UUID owner, UUID[] players) {
        this.modifyLock(owner, players, this.lockit.getTeams());
    }

    private void modifyLock(boolean active, UUID[] players) {
        this.modifyLock(active, this.lockit.getOwner().get(), players);
    }

    private void modifyLock(UUID[] players) {
        this.modifyLock(this.lockit.isActive(), players);
    }

    private void modifyLock(boolean active, UUID owner, String[] teams) {
        this.modifyLock(active, owner, this.lockit.getPlayers(), teams);
    }

    private void modifyLock(UUID owner, String[] teams) {
        this.modifyLock(owner, this.lockit.getPlayers(), teams);
    }

    private void modifyLock(boolean active, String[] teams) {
        this.modifyLock(active, this.lockit.getOwner().get(), teams);
    }

    private void modifyLock(String[] teams) {
        this.modifyLock(this.lockit.isActive(), teams);
    }

    private static String[] toStringArray(List<String> list) {
        var arr = new String[list.size()];
        arr = list.toArray(arr);
        return arr;
    }

    private static UUID[] toUuidArray(List<UUID> list) {
        var arr = new UUID[list.size()];
        arr = list.toArray(arr);
        return arr;
    }
}
