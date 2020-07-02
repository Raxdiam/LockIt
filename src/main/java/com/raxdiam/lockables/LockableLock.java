package com.raxdiam.lockables;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringTag;
import net.minecraft.tag.Tag;

import javax.swing.border.EmptyBorder;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

public class LockableLock {
    public static final String KEY = "lockables";
    public static final LockableLock EMPTY = new LockableLock(false, Optional.empty(), new UUID[] {}, new String[] {});

    private final boolean active;
    private final Optional<UUID> owner;
    private final UUID[] shared;
    private final String[] teams;

    public LockableLock(boolean active, Optional<UUID> owner, UUID[] shared, String[] teams) {
        this.active = active;
        this.owner = owner;
        this.shared = shared;
        this.teams = teams;
    }

    public boolean isActive() {
        return this.active;
    }

    public Optional<UUID> getOwner() {
        return this.owner;
    }

    public UUID[] getShared() {
        return this.shared;
    }

    public String[] getTeams() {
        return this.teams;
    }

    public List<UUID> getSharedList() {
        return Arrays.asList(this.shared);
    }

    public List<String> getTeamsList() {
        return Arrays.asList(this.teams);
    }

    public void toTag(CompoundTag tag) {
        if (this.owner.isEmpty()) return;

        var lockedTag = new CompoundTag();

        var sharedTag = toUuidListTag(this.shared);
        var teamsTag = toStringListTag(this.teams);

        lockedTag.putBoolean("active", this.active);
        lockedTag.putUuid("owner", owner.get());
        lockedTag.put("sharedWith", sharedTag);
        lockedTag.put("teams", teamsTag);
        tag.put(KEY, lockedTag);
    }

    public static LockableLock create(boolean active, UUID owner, UUID[] shared, String[] teams) {
        return new LockableLock(active, Optional.of(owner), shared, teams);
    }

    public static LockableLock create(boolean active, UUID owner, UUID[] shared) {
        return create(active, owner, shared, EMPTY.teams);
    }

    public static LockableLock create(boolean active, UUID owner, String[] teams) {
        return create(active, owner, EMPTY.shared, teams);
    }

    public static LockableLock create(boolean active, UUID owner) {
        return create(active, owner, EMPTY.shared, EMPTY.teams);
    }

    public static LockableLock create(UUID owner, UUID[] shared, String[] teams) {
        return create(EMPTY.active, owner, shared, teams);
    }

    public static LockableLock create(UUID owner) {
        return create(owner, EMPTY.shared, EMPTY.teams);
    }

    public static LockableLock fromTag(CompoundTag tag) {
        if (tag.contains(KEY)) {
            var lockedTag = tag.getCompound(KEY);

            var sharedTag = lockedTag.getList("sharedWith", 8);
            var teamsTag = lockedTag.getList("teams", 8);

            var sharedArray = toUuidArray(sharedTag);
            var teamsArray = toStringArray(teamsTag);

            return create(lockedTag.getBoolean("active"), lockedTag.getUuid("owner"), sharedArray, teamsArray);
        }

        return EMPTY;
    }

    private static ListTag toStringListTag(String[] array) {
        var tag = new ListTag();
        for (var item : array)
            tag.add(StringTag.of(item));
        return tag;
    }

    private static ListTag toUuidListTag(UUID[] array) {
        var tag = new ListTag();
        for (var item : array)
            tag.add(NbtHelper.fromUuid(item));
        return tag;
    }

    private static String[] toStringArray(ListTag listTag) {
        var array = new String[listTag.size()];
        for (int i = 0; i < listTag.size(); i++)
            array[i] = listTag.getString(i);
        return array;
    }

    private static UUID[] toUuidArray(ListTag listTag) {
        var array = new UUID[listTag.size()];
        for (int i = 0; i < listTag.size(); i++)
            array[i] = NbtHelper.toUuid(listTag.get(i));
        return array;
    }
}
