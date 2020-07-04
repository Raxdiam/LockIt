package com.raxdiam.lockit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringTag;

import java.util.*;

public class LockItLock {
    public static final String KEY = "lockables";
    public static final LockItLock EMPTY = new LockItLock(false, Optional.empty(), new UUID[] {}, new String[] {});

    private final boolean active;
    private final Optional<UUID> owner;
    private final UUID[] players;
    private final String[] teams;

    public LockItLock(boolean active, Optional<UUID> owner, UUID[] players, String[] teams) {
        this.active = active;
        this.owner = owner;
        this.players = players;
        this.teams = teams;
    }

    public boolean isActive() {
        return this.active;
    }

    public Optional<UUID> getOwner() {
        return this.owner;
    }

    public UUID[] getPlayers() {
        return this.players;
    }

    public String[] getTeams() {
        return this.teams;
    }

    public List<UUID> getPlayersList() {
        return Arrays.asList(this.players);
    }

    public List<String> getTeamsList() {
        return Arrays.asList(this.teams);
    }

    public void toTag(CompoundTag tag) {
        if (this.owner.isEmpty()) return;

        var lockedTag = new CompoundTag();
        var sharedTag = new CompoundTag();

        sharedTag.put("players", toUuidListTag(this.players));
        sharedTag.put("teams", toStringListTag(this.teams));

        lockedTag.putBoolean("active", this.active);
        lockedTag.putUuid("owner", owner.get());
        lockedTag.put("shared", sharedTag);

        tag.put(KEY, lockedTag);
    }

    public static LockItLock fromTag(CompoundTag tag) {
        if (tag.contains(KEY)) {
            var lockedTag = tag.getCompound(KEY);
            var sharedTag = lockedTag.getCompound("shared");

            var playersTag = sharedTag.getList("players", 11);
            var teamsTag = sharedTag.getList("teams", 8);

            return create(lockedTag.getBoolean("active"), lockedTag.getUuid("owner"), toUuidArray(playersTag), toStringArray(teamsTag));
        }

        return EMPTY;
    }

    public static LockItLock create(boolean active, UUID owner, UUID[] shared, String[] teams) {
        return new LockItLock(active, Optional.of(owner), shared, teams);
    }

    public static LockItLock create(boolean active, UUID owner, UUID[] shared) {
        return create(active, owner, shared, EMPTY.teams);
    }

    public static LockItLock create(boolean active, UUID owner, String[] teams) {
        return create(active, owner, EMPTY.players, teams);
    }

    public static LockItLock create(boolean active, UUID owner) {
        return create(active, owner, EMPTY.players, EMPTY.teams);
    }

    public static LockItLock create(UUID owner, UUID[] shared, String[] teams) {
        return create(EMPTY.active, owner, shared, teams);
    }

    public static LockItLock create(UUID owner) {
        return create(owner, EMPTY.players, EMPTY.teams);
    }

    private static ListTag toStringListTag(String[] array) {
        var tag = new ListTag();
        for (var item : array)
            tag.add(StringTag.of(item));
        return tag;
    }

    private static ListTag toStringListTag(UUID[] array) {
        var tag = new ListTag();
        for (var item : array)
            tag.add(StringTag.of(item.toString()));
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
