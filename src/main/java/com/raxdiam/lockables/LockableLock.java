package com.raxdiam.lockables;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LockableLock {
    public static final String KEY = "lockables";
    public static final LockableLock EMPTY = new LockableLock(false, "", new String[] {});

    private final boolean active;
    private final String owner;
    private final String[] shared;

    public LockableLock(boolean active, String owner, String... shared) {
        this.active = active;
        this.owner = owner;
        this.shared = shared;
    }

    public String getOwner() {
        return this.owner;
    }

    public List<String> getShared() {
        return Arrays.asList(this.shared);
    }

    public boolean isActive() {
        return this.active;
    }

    public void toTag(CompoundTag tag) {
        if (this.owner.isEmpty()) return;

        var lockedTag = new CompoundTag();
        lockedTag.putBoolean("active", this.active);
        lockedTag.putString("owner", owner);
        var sharedTag = new ListTag();
        for (var u : this.shared) {
            sharedTag.add(StringTag.of(u));
        }
        lockedTag.put("sharedWith", sharedTag);
        tag.put(KEY, lockedTag);
    }

    public static LockableLock fromTag(CompoundTag tag) {
        if (tag.contains(KEY)) {
            var lockedTag = tag.getCompound(KEY);
            var sharedTag = lockedTag.getList("sharedWith", 8);
            var uuids = new ArrayList<String>();
            for (var t : sharedTag) {
                uuids.add(t.asString());
            }
            var uuidArr = new String[uuids.size()];
            uuidArr = uuids.toArray(uuidArr);
            return new LockableLock(lockedTag.getBoolean("active"), lockedTag.getString("owner"), uuidArr);
        }
        return EMPTY;
    }
}
