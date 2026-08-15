VersePlus is a lightweight Fabric MC mod for vanilla enhancement

*Current supported MC version:* **26.2**

*Release status:* **0.10.0-beta**

## Current Features
- Increased up to 6x player's drop lifetime (30 minutes)
- Loom pattern limit increased to 16
- Reduced leaf litter in forest biome
- Chained respawn points (buffered 32 spawnpoints)
- Rebalanced lead craft
- Frosted Ice Block
- Sponge piles in warm oceans
- Suspicious sand and gravel in cold ocean floors
- Some sniffer eggs may be empty (*10% chance*)
- Tamed wolves and cats can recognize up to five additional owners after repeated feeding near their primary owner
- Wolves can learn a goat horn sound and be recalled by that sound, even after their chunk unloads
- Porting Bedrock (before 1.19.40) windswept savanna grass color
- Player's drop NBT log (in world folder, `data/players_drop_graveyard.dat`)
- Throwable fireball item
- Warden Staff
  - crafted from three echo shards and a Warden Rod dropped when a player kills a Warden
  - charges for 1.7 seconds before firing a through-wall sonic boom
  - has 64 durability and a 5-second cooldown after every completed shot
- Obsidian Boats
  - rarely found in Nether Fortress chests
  - ride just above lava, travel more slowly than normal boats, and sink after 2500 blocks of lava travel
  - sink in water and cannot be used there
  - sit progressively lower in lava as their remaining travel resource decreases
- Container Locks
  - rare copper, iron, and golden locks appear in shipwreck, village, abandoned-mineshaft, and ancient-city chests
  - every lock contains a matching unique key; applying it to a chest or barrel installs the lock and returns that key
  - craft any shulker box with a lock to preserve its contents and install the lock; closed shulkers hide their contents in tooltips
  - apply a lock to any button, or craft them together, to install a permanent open lock; its matching key toggles access without pressing the button
  - closed button locks block hand, arrow, and explosion activation; broken buttons retain their lock and current state
  - use the matching key to close or open the container; clone a key with an ingot of its material
  - broken chests and barrels drop their contents and installed lock, while shulker boxes keep their vanilla contents-preserving behavior
- Mannequin persistent nickname visibility
- Players can be renamed with name tags
- Player waypoint editing
  - create, remove, and style named points at the command executor's current position; coordinate arguments are not accepted
  - `/waypoint list` is available to every player and also works for connection-local points on servers without VersePlus
  - retain the complete vanilla entity `modify` command; personal points use the unambiguous `modify personal <name>` branch
  - optionally choose a named color directly after `add`, or use `hex <RRGGBB>`
  - everyone may use their own points, while operators can target players with selectors such as `@s` and `@a`
  - on servers without VersePlus, named points remain local to the current connection
- Operator-only `/inhabited` scans saved region files in the background and reports clusters of long-inhabited areas without loading or generating chunks
- Collection challenges for every ore block, every non-player mob head, and every music disc
- Baby spiders natural in adult spiders
- Spiders can naturally spawn with Weaving at night (*7% chance*)
- Mobs naturally spawned in swamps under full moon get Oozing
- Ghast tears can be unpickable (*15% chance*)
- Ghast music disc drop chance reduced to *50%* when vanilla conditions are met
- Fire ender pearls (rare drop from nether endermen)
  - cause explosion a few seconds after throwing
- Rare ender pearls
  - you can charge a rare ender pearl with 8 echo shards for expensive teleporting to yours spawnpoint
  - don't throw charged rare ender pearl `:)`
  - you can't teleport if there are monsters nearby
  - can be found with a chance (*7%*) in stronghold chests
  - can be offered by wandering trader (*3% chance*)
  - drops from an enderman killed by a golden sword with knockback (without looting) in the overworld (*0.4% chance*) 
