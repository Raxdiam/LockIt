# LockIt
Allows containers to be locked, unlocked, and shared using commands.

## Information
### How it works
- Locking or sharing a container block will assign ownership of the lock to the calling player
- Locking a container block will prevent it from breaking
- Sharing a container block will allow players that it is shared with, to access the container's inventory
- Players with shared access will NOT be able to lock or unlock the container
- Claiming/un-claiming a container block will assign/remove ownership of the lock to/from the calling player

### Commands
- `/li lock` - Lock the targeted container block
- `/li unlock`- Unlock the targeted container block
- `/li share player add <player>`- Add a specified player to the shared-with players for the targeted container block
- `/li share player remove <player>` - Remove a specified player from the shared-with players for the targeted container block
- `/li share player clear` (to-do) - Clear all shared-with players from the targeted container block
- `/li share player list` (to-do) - List all shared-with players for the targeted container block
- `/li share team add <team>` (to-do) - Add a specified team to the shared-with teams for the targeted container block
- `/li share team remove <team>` (to-do) - Remove a specified team from the shared-with teams for the targeted container block
- `/li share team clear` (to-do) - Clear all shared-with teams from the targeted container block
- `/li share team list` (to-do) - List all shared-with teams for the targeted container block
- `/li claim` (to-do) - Claim the targeted container block
- `/li unclaim` (to-do) - Un-claim the targeted container block

### To-Do
- [x] Lock container block
- [x] Unlock container block
- [x] Share container block with another player
- [x] Un-share container block from a shared-with player
- [x] Prevent hoppers from pulling/inserting items into/from a locked container
- [x] List a container block's shared-with players
- [x] Clear a container block's shared-with players
- [x] Share container block with teams
- [x] Un-share container block from a shared-with team
- [x] List a container block's shared-with teams
- [x] Clear a container block's shared-with teams
