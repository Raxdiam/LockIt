# LockIt
Allows containers to be locked, unlocked, and shared using commands.

## Information
### How it works
- Locking or sharing a container block will assign ownership of the lock to the calling player
- Locking a container block will prevent it from breaking
- Sharing a container block will allow players that it is shared with, to access the container's inventory
- Players with shared access will NOT be able to lock or unlock the container
- Claiming/un-claiming a container block will assign/remove ownership of the lock to/from the calling player
- Using the uninstall command will remove any LockIt data from all claimed containers

### Commands
- `/li lock` - Lock the targeted container block
- `/li unlock`- Unlock the targeted container block
- `/li share player add <player>`- Add a specified player to the shared-with players for the targeted container block
- `/li share player remove <player>` - Remove a specified player from the shared-with players for the targeted container block
- `/li share player clear` - Clear all shared-with players from the targeted container block
- `/li share player list` - List all shared-with players for the targeted container block
- `/li share team add <team>` - Add a specified team to the shared-with teams for the targeted container block
- `/li share team remove <team>` - Remove a specified team from the shared-with teams for the targeted container block
- `/li share team clear` - Clear all shared-with teams from the targeted container block
- `/li share team list` - List all shared-with teams for the targeted container block
- `/li claim` - Claim the targeted container block
- `/li unclaim` - Un-claim the targeted container block
- `/li uninstall confirm` - Prepare for uninstallation of mod
#
<br/>
<sub><sup>Thanks to my good friend MrVelocity for providing a name for this project.</sup></sub>