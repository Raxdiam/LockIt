package com.raxdiam.lockit.mixin;

import com.mojang.authlib.GameProfile;
import com.raxdiam.lockit.LockItAction;
import com.raxdiam.lockit.LockItLock;
import com.raxdiam.lockit.LockItLockResult;
import com.raxdiam.lockit.LockItMod;
import com.raxdiam.lockit.accessor.ILockableContainerBlockEntityAccessor;
import com.raxdiam.lockit.text.PrefixedText;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(value = LockableContainerBlockEntity.class)
public class LockableContainerBlockEntityMixin implements ILockableContainerBlockEntityAccessor {
    private LockItLock lockable = LockItLock.EMPTY;

    @Inject(method = "fromTag", at = @At("TAIL"))
    public void onFromTag(BlockState state, CompoundTag tag, CallbackInfo info) {
        this.lockable = LockItLock.fromTag(tag);
    }

    @Inject(method = "toTag", at = @At("TAIL"))
    public void onToTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> info) {
        this.lockable.toTag(tag);
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
        if (!this.lockable.getOwner().isEmpty()) {
            var owner = this.lockable.getOwner().get();
            if (this.lockable.isActive()) {
                if (owner.equals(player.getUuid())) return true;
                else if (this.lockable.getSharedList().contains(player.getUuid())) return true;
                else return false;
            }
        }

        return true;
    }

    @Override
    public LockItLockResult lock(ServerPlayerEntity player) {
        if (this.lockable.getOwner().isEmpty()) {
            //this.lockable = new LockableLock(true, player.getUuid().toString());
            this.modifyLock(true, player.getUuid());
            //player.sendMessage(PrefixedText.createLiteral("Container locked!", Formatting.GREEN), false);
            return LockItLockResult.success(LockItAction.LOCK, "Container locked!");
        } else {
            var ownerUuid = this.lockable.getOwner().get();
            var owner = player.getServerWorld().getServer().getPlayerManager().getPlayer(ownerUuid);
            if (!player.getUuid().equals(ownerUuid)) {
                //player.sendMessage(PrefixedText.createLiteral("Someone has already locked this container.", Formatting.RED), false);
                LockItMod.LOGGER.info(player.getDisplayName() + " tried to lock a container originally locked by " + owner.getDisplayName());
                return LockItLockResult.fail(LockItAction.LOCK, "Someone has already locked this container.");
            } else {
                if (!this.lockable.isActive()) {
                    //this.lockable = new LockableLock(true, this.lockable.getOwner(), toStringArray(this.lockable.getSharedList()));
                    this.modifyLock(true);
                    //player.sendMessage(PrefixedText.createLiteral("Container locked!", Formatting.GREEN), false);
                    return LockItLockResult.success(LockItAction.LOCK, "Container locked!");
                } else {
                    //player.sendMessage(PrefixedText.createLiteral("You have already locked this container.", Formatting.YELLOW), false);
                    return LockItLockResult.failSoft(LockItAction.LOCK, "You have already locked this container.");
                }
            }
        }
    }

    @Override
    public LockItLockResult unlock(ServerPlayerEntity player) {
        if (this.lockable.getOwner().isEmpty()) {
            //player.sendMessage(PrefixedText.createLiteral("This container was never locked.", Formatting.YELLOW), false);
            return LockItLockResult.failSoft(LockItAction.UNLOCK, "This container was never locked.");
        } else {
            var ownerUuid = this.lockable.getOwner().get();
            var owner = player.getServerWorld().getServer().getPlayerManager().getPlayer(ownerUuid);
            if (player.getUuid().equals(ownerUuid)) {
                if (this.lockable.isActive()) {
                    //this.lockable = new LockableLock(false, this.lockable.getOwner(), toStringArray(this.lockable.getSharedList()));
                    this.modifyLock(false);
                    //player.sendMessage(PrefixedText.createLiteral("Container unlocked!", Formatting.GREEN), false);
                    return LockItLockResult.success(LockItAction.UNLOCK, "Container unlocked!");
                } else {
                    //player.sendMessage(PrefixedText.createLiteral("This container is already unlocked.", Formatting.YELLOW), false);
                    return LockItLockResult.failSoft(LockItAction.UNLOCK, "This container is already unlocked.");
                }
            } else {
                //player.sendMessage(PrefixedText.createLiteral("You cannot unlock a container that isn't yours.", Formatting.RED), false);
                LockItMod.LOGGER.info(player.getDisplayName() + " tried to unlock a container owned by " + owner.getDisplayName());
                return LockItLockResult.fail(LockItAction.UNLOCK, "You cannot unlock a container that isn't yours.");
            }
        }
    }

    @Override
    public LockItLockResult claim(ServerPlayerEntity player) {
        return LockItLockResult.fail(LockItAction.CLAIM, "Method not yet implemented");
    }

    @Override
    public LockItLockResult unclaim(ServerPlayerEntity player) {
        return LockItLockResult.fail(LockItAction.UNCLAIM, "Method not yet implemented");
    }

    @Override
    public LockItLockResult share(ServerPlayerEntity player, GameProfile target) {
        if (this.lockable.getOwner().isEmpty()) {
            //this.lockable = new LockableLock(false, player.getUuid().toString(), new String[] {target.getId().toString()});
            this.modifyLock(false, player.getUuid(), new UUID[] {target.getId()});
            //player.sendMessage(PrefixedText.createLiteral("This container is now shared with " + target.getName() + "!", Formatting.GREEN), false);
            return LockItLockResult.success(LockItAction.SHAREPLAYER, "This container is now shared with " + target.getName() + "!");
        } else {
            var ownerUuid = this.lockable.getOwner().get();
            var owner = player.getServerWorld().getServer().getPlayerManager().getPlayer(ownerUuid);
            if (player.getUuid().equals(ownerUuid)) {
                var current = new ArrayList<>(this.lockable.getSharedList());

                if (player.getUuid().equals(target.getId())) {
                    //player.sendMessage(PrefixedText.createLiteral("You cannot share this container with yourself!", Formatting.YELLOW), false);
                    return LockItLockResult.failSoft(LockItAction.SHAREPLAYER, "You cannot share this container with yourself!");
                } else if (current.contains(target.getId())){
                    //player.sendMessage(PrefixedText.createLiteral("This container is already shared with " + target.getName() + "!", Formatting.YELLOW), false);
                    return LockItLockResult.failSoft(LockItAction.SHAREPLAYER, "This container is already shared with " + target.getName() + "!");
                } else {
                    current.add(target.getId());
                    //this.lockable = new LockableLock(this.lockable.isActive(), this.lockable.getOwner(), toStringArray(current));
                    this.modifyLock(toUuidArray(current));
                    //player.sendMessage(PrefixedText.createLiteral("This container is now shared with " + target.getName() + "!", Formatting.GREEN), false);
                    return LockItLockResult.success(LockItAction.SHAREPLAYER, "This container is now shared with " + target.getName() + "!");
                }
            } else {
                //player.sendMessage(PrefixedText.createLiteral("You cannot share a container you do not own.", Formatting.RED), false);
                LockItMod.LOGGER.info(player.getDisplayName() + " tried to share a chest owned by " + owner.getDisplayName());
                return LockItLockResult.fail(LockItAction.SHAREPLAYER, "You cannot share a container you do not own.");
            }
        }
    }

    @Override
    public LockItLockResult share(ServerPlayerEntity player, Team team) {
        return LockItLockResult.fail(LockItAction.SHARETEAM, "Method not yet implemented");
    }

    @Override
    public LockItLockResult unshare(ServerPlayerEntity player, GameProfile target) {
        if (this.lockable.getOwner().isEmpty()) {
            //player.sendMessage(PrefixedText.createLiteral("This container is not owned by anyone.", Formatting.YELLOW), false);
            return LockItLockResult.failSoft(LockItAction.UNSHAREPLAYER, "This container is not owned by anyone.");
        } else {
            var ownerUuid = this.lockable.getOwner().get();
            var owner = player.getServerWorld().getServer().getPlayerManager().getPlayer(ownerUuid);
            if (player.getUuid().equals(ownerUuid)) {
                var current = new ArrayList<>(this.lockable.getSharedList());
                if (current.contains(target.getId())) {
                    current.remove(target.getId());
                    //this.lockable = new LockableLock(this.lockable.isActive(), this.lockable.getOwner(), toStringArray(current));
                    this.modifyLock(toUuidArray(current));
                    //player.sendMessage(PrefixedText.createLiteral("This container is no longer shared with " + target.getName() + "!", Formatting.GREEN), false);
                    return LockItLockResult.success(LockItAction.UNSHAREPLAYER, "This container is no longer shared with " + target.getName() + "!");
                } else {
                    //player.sendMessage(PrefixedText.createLiteral("This container is not shared with " + target.getName() + ".", Formatting.YELLOW), false);
                    return LockItLockResult.failSoft(LockItAction.UNSHAREPLAYER, "This container is not shared with " + target.getName() + ".");
                }
            } else {
                //player.sendMessage(PrefixedText.createLiteral("You cannot un-share a container you do not own.", Formatting.RED), false);
                LockItMod.LOGGER.info(player.getDisplayName() + " tried to un-share a chest owned by " + owner.getDisplayName());
                return LockItLockResult.fail(LockItAction.UNSHAREPLAYER, "You cannot un-share a container you do not own.");
            }
        }
    }

    @Override
    public LockItLockResult unshare(ServerPlayerEntity player, Team team) {
        return LockItLockResult.fail(LockItAction.UNSHARETEAM, "Method not yet implemented");
    }

    @Override
    public LockItLock getLockable() {
        return this.lockable;
    }

    private void modifyLock(boolean active, UUID owner, UUID[] shared, String[] teams) {
        this.lockable = LockItLock.create(active, owner, shared, teams);
    }

    private void modifyLock(boolean active, UUID owner) {
        this.modifyLock(active, owner, this.lockable.getPlayers(), this.lockable.getTeams());
    }

    private void modifyLock(UUID owner, UUID[] shared, String[] teams) {
       this.modifyLock(this.lockable.isActive(), owner, shared, teams);
    }

    private void modifyLock(UUID owner) {
        this.modifyLock(this.lockable.isActive(), owner);
    }

    private void modifyLock(boolean active, UUID[] shared, String[] teams) {
        this.modifyLock(active, this.lockable.getOwner().get(), shared, teams);
    }

    private void modifyLock(boolean active) {
        this.modifyLock(active, this.lockable.getOwner().get());
    }

    private void modifyLock(boolean active, UUID owner, UUID[] shared) {
        this.modifyLock(active, owner, shared, this.lockable.getTeams());
    }

    private void modifyLock(UUID owner, UUID[] shared) {
        this.modifyLock(owner, shared, this.lockable.getTeams());
    }

    private void modifyLock(boolean active, UUID[] shared) {
        this.modifyLock(active, this.lockable.getOwner().get(), shared);
    }

    private void modifyLock(UUID[] shared) {
        this.modifyLock(this.lockable.isActive(), shared);
    }

    private void modifyLock(boolean active, UUID owner, String[] teams) {
        this.modifyLock(active, owner, this.lockable.getPlayers(), teams);
    }

    private void modifyLock(UUID owner, String[] teams) {
        this.modifyLock(owner, this.lockable.getPlayers(), teams);
    }

    private void modifyLock(boolean active, String[] teams) {
        this.modifyLock(active, this.lockable.getOwner().get(), teams);
    }

    private void modifyLock(String[] teams) {
        this.modifyLock(this.lockable.isActive(), teams);
    }

    private void onLock() {
        var entity = (LootableContainerBlockEntity) (Object) this;
        var state = entity.getCachedState().get(ChestBlock.CHEST_TYPE);

    }

    private void onUnlock() {
        
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
