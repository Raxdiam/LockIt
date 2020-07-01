package com.raxdiam.lockables.mixin;

import com.mojang.authlib.GameProfile;
import com.raxdiam.lockables.LockableLock;
import com.raxdiam.lockables.LockablesMod;
import com.raxdiam.lockables.accessor.ILockableContainerBlockEntityAccessor;
import com.raxdiam.lockables.text.PrefixedText;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
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
public abstract class LockableContainerBlockEntityMixin implements ILockableContainerBlockEntityAccessor {
    private LockableLock lockable = LockableLock.EMPTY;

    @Inject(method = "fromTag", at = @At("TAIL"))
    public void onFromTag(BlockState state, CompoundTag tag, CallbackInfo info) {
        this.lockable = LockableLock.fromTag(tag);
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
            var owner = UUID.fromString(this.lockable.getOwner());
            if (this.lockable.isActive()) {
                if (owner.equals(player.getUuid())) return true;
                else if (this.lockable.getShared().contains(player.getUuid().toString())) return true;
                else return false;
            }
        }

        return true;
    }

    @Override
    public void lock(ServerPlayerEntity player) {
        if (this.lockable.getOwner().isEmpty()) {
            this.lockable = new LockableLock(true, player.getUuid().toString());
            player.sendMessage(PrefixedText.createLiteral("Container locked!", Formatting.GREEN), false);
        } else {
            var ownerUuid = UUID.fromString(this.lockable.getOwner());
            var owner = player.getServerWorld().getServer().getPlayerManager().getPlayer(ownerUuid);
            if (!player.getUuid().equals(ownerUuid)) {
                player.sendMessage(PrefixedText.createLiteral("Someone has already locked this container.", Formatting.RED), false);
                LockablesMod.LOGGER.info(player.getDisplayName() + " tried to lock a container originally locked by " + owner.getDisplayName());
            } else {
                if (!this.lockable.isActive()) {
                    this.lockable = new LockableLock(true, this.lockable.getOwner(), toStringArray(this.lockable.getShared()));
                    player.sendMessage(PrefixedText.createLiteral("Container locked!", Formatting.GREEN), false);
                } else {
                    player.sendMessage(PrefixedText.createLiteral("You have already locked this container.", Formatting.YELLOW), false);
                }
            }
        }
    }

    @Override
    public void unlock(ServerPlayerEntity player) {
        if (this.lockable.getOwner().isEmpty()) {
            player.sendMessage(PrefixedText.createLiteral("This container was never locked.", Formatting.YELLOW), false);
        } else {
            var ownerUuid = UUID.fromString(this.lockable.getOwner());
            var owner = player.getServerWorld().getServer().getPlayerManager().getPlayer(ownerUuid);
            if (player.getUuid().equals(ownerUuid)) {
                if (this.lockable.isActive()) {
                    this.lockable = new LockableLock(false, this.lockable.getOwner(), toStringArray(this.lockable.getShared()));
                    player.sendMessage(PrefixedText.createLiteral("Container unlocked!", Formatting.GREEN), false);
                } else {
                    player.sendMessage(PrefixedText.createLiteral("This container is already unlocked.", Formatting.YELLOW), false);
                }
            } else {
                player.sendMessage(PrefixedText.createLiteral("You cannot unlock a container that isn't yours.", Formatting.RED), false);
                LockablesMod.LOGGER.info(player.getDisplayName() + " tried to unlock a container owned by " + owner.getDisplayName());
            }
        }
    }

    @Override
    public void share(ServerPlayerEntity player, GameProfile target) {
        if (this.lockable.getOwner().isEmpty()) {
            this.lockable = new LockableLock(false, player.getUuid().toString(), new String[] {target.getId().toString()});
            player.sendMessage(PrefixedText.createLiteral("This container is now shared with " + target.getName() + "!", Formatting.GREEN), false);
        } else {
            var ownerUuid = UUID.fromString(this.lockable.getOwner());
            var owner = player.getServerWorld().getServer().getPlayerManager().getPlayer(ownerUuid);
            if (player.getUuid().equals(ownerUuid)) {
                var current = new ArrayList<>(this.lockable.getShared());

                if (player.getUuid().equals(target.getId())) {
                    player.sendMessage(PrefixedText.createLiteral("You cannot share this container with yourself!", Formatting.YELLOW), false);
                } else if (current.contains(target.getId().toString())){
                    player.sendMessage(PrefixedText.createLiteral("This container is already shared with " + target.getName() + "!", Formatting.YELLOW), false);
                } else {
                    current.add(target.getId().toString());
                    this.lockable = new LockableLock(this.lockable.isActive(), this.lockable.getOwner(), toStringArray(current));
                    player.sendMessage(PrefixedText.createLiteral("This container is now shared with " + target.getName() + "!", Formatting.GREEN), false);
                }
            } else {
                player.sendMessage(PrefixedText.createLiteral("You cannot share a container you do not own.", Formatting.RED), false);
                LockablesMod.LOGGER.info(player.getDisplayName() + " tried to share a chest owned by " + owner.getDisplayName());
            }
        }
    }

    @Override
    public void unshare(ServerPlayerEntity player, GameProfile target) {
        if (this.lockable.getOwner().isEmpty()) {
            player.sendMessage(PrefixedText.createLiteral("This container is not owned by anyone.", Formatting.YELLOW), false);
        } else {
            var ownerUuid = UUID.fromString(this.lockable.getOwner());
            var owner = player.getServerWorld().getServer().getPlayerManager().getPlayer(ownerUuid);
            if (player.getUuid().equals(ownerUuid)) {
                var current = new ArrayList<>(this.lockable.getShared());
                if (current.contains(target.getId().toString())) {
                    current.remove(target.getId().toString());
                    this.lockable = new LockableLock(this.lockable.isActive(), this.lockable.getOwner(), toStringArray(current));
                    player.sendMessage(PrefixedText.createLiteral("This container is no longer shared with " + target.getName() + "!", Formatting.GREEN), false);
                } else {
                    player.sendMessage(PrefixedText.createLiteral("This container is not shared with " + target.getName() + ".", Formatting.YELLOW), false);
                }
            } else {
                player.sendMessage(PrefixedText.createLiteral("You cannot un-share a container you do not own.", Formatting.RED), false);
                LockablesMod.LOGGER.info(player.getDisplayName() + " tried to un-share a chest owned by " + owner.getDisplayName());
            }
        }
    }

    @Override
    public LockableLock getLockable() {
        return this.lockable;
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
}
