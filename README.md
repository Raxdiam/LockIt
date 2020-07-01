# Lockables
Allows containers to be locked, unlocked, and shared using commands.

## Information
### How it works
- Locking or sharing a container block will assign ownership of the lock to the calling player
- Locking a container block will prevent it from breaking
- Sharing a container block will allow players that it is shared with, to access the container's inventory
- Players with shared access will NOT be able to lock or unlock the container

### Commands
- `/lock` - Lock the targeted container block
- `/unlock` - Unlock the targeted container block
- `/share <player>` - Share the targeted container block with another player (even if they are offline)
- `/unshare <player>` - Un-share the targeted container block with a previously shared-with player

### Planned
- [x] Lock container block
- [x] Unlock container block
- [x] Share container block with another player
- [x] Un-share container block from a shared-with player
- [x] Prevent hoppers from pulling/inserting items into/from a locked container
- [ ] Share container block with teams
- [ ] Un-share container block from a shared-with team
