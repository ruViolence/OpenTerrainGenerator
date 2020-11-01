## Using the OTG World Creation GUI

_This section is written for OTG v8 and above, and is a summary.

Worlds and dimensions can be created, edited and deleted using the world creation GUI. Press "O" to open the GUI in-game. Creating/editing/deleting dimensions is also possible during the game, no server restarts are required.

When first making a world, you can either press "Create New World" or "Create OTG World". If you create a vanilla world, you can use the GUI in-game to create OTG dimensions you can teleport to. This is useful if you have other mods that need a vanilla overworld, and want to use these worlds alongside OTG.

If making an OTG world, you need to select an [installed preset](installation/installing-worlds.md), or make your own preset by pressing "New". Making your own preset can be a lot of work, but is very rewarding. There will eventually be an in-depth tutorial added to the wiki, but for now go to the Discord server for help.

If you've selected a preset, you will then be presented with the Dimension Creation screen. This lets you change world settings, like game rules, seed, player game mode, etc. On the left you can add more dimensions to the world, which you can access through portals.

Once in the game, in order to use the menu you need to be OPed on a server, or have cheats enabled in single player. If you created the world and didn't enable cheats, you can temporarily enable them by using the "Open to LAN" feature. You can change most of the settings from within the game.

## Using the map pre-generator

OTG contains a built in map pre-generator which allows you to generate chunks in a radius around spawn, eliminating world generation related lag. It's recommended that you pre-generate while not actively playing as running the pregenerator is intensive especially at high speeds (check OTG.ini for speed settings).

**For OTG v7 and above,** the pregenerator is started by setting a radius in the 'O' menu. To stop it, set the radius to 0 chunks.

For OTG v6 and below, see the `/otg pregen` command below.

| Command    | Description                                                                                                                                                                                    |
|-|-|
| /otg       | Show help info                                                                                                                                                                                 |
| /otg biome | Show biome information for the biome at the player's coordinates<br>Usage: `/otg biome [-f -s -d -m]`<br>`-f` shows temperature<br>`-s` shows id's<br>`-d` shows biome dictionary tags<br>`-m` shows registered mobs |
| /otg spawn | _Unavailable for Forge in OTG v8 and below._<br>Spawns a custom object at the block you are looking at<br>Usage: `/otg spawn <bo3> [preset-name]`<br>e.g. `/otg spawn PumpkinHead1 Biome Bundle` |
| /otg structure | View author and description information for any structure at the player's coordinates |
| /otg tp | Teleports the player to the specified dimension or biome.<br>Usage: `/otg tp <biome id or name>` or `/otg tp <dimension name>` |
| /otg blocks | Show a list of block names that can be spawned inside BO3's with the Block() tag and used in biome- and world-configs. |
| /otg biomes | View a list of registered biomes |
| /otg entities | Show a list of entities that can be spawned inside BO3's using the Entity() tag |
| /otg export | _Requires OTG v9 or above_<br>_Requires WorldEdit installed_<br>Exports a WorldEdit selection to a BO3 (or BO4) file with a given name<br>Usage: `/otg export <name> [center block] [-a -t -o -b -bo4]`<br>`-a` makes the export include air blocks<br>`-t` makes the export include tile entities<br>`-o` makes the export override an existing file<br>`-b` makes the export use branches<br>`-bo4` exports as a BO4 file instead of a BO3 file |
| /otg exportbo4data | Exports all BO4 files and BO3 files that have isOTGPlus:true as BO4Data files, if none exist already. BO3Data files can significantly reduce file size and loading times, and should be used by OTG content creators when packaging presets for players. |
| /otg flushcache | Unloads all loaded object files. Use this to refresh objects after editing them. Also flushes chunk generator cache to free up memory. |
| /otg getmoddata | Sends any ModData() tags in BO3's within the specified radius in chunks to the specified mod. Some OTG mob spawning commands can be used this way. Be sure to set up ModData() tags in your BO3 to make this work.<br>Usage: `/otg getmoddata <mod name> <radius>`|
| /otg lookup | Look up a registered biome by name or ID<br>Usage `/otg lookup <name or id>` |
| /otg map | Generate a square biome and temperature map in the server's root of the specified world<br>Usage: `/otg map [world name] [-s -r -o -l]`<br>`-s <size>` determines the size of the map, in chunks (positive integer)<br>`-r <angle>` determines the angle of rotation of the image (must be 0/90/180/270)<br>`-o <offset x> <offset y>`  determines the image offset (must be integers)<br>-l adds the coordinate label to the file name |
| /otg mods | View a list of mods and their mod IDs |
| /otg reload | _Unavailable for Forge in OTG v8 and below<br>Reloads the configuration settings |
| **Deprecated in OTG 1.12.2 v7 and above** | |
| /otg dim | Shows the name and ID of the dimension the player is currently in.<br>Usage: `/otg dim -c/-d <preset name>`<br>`-c` creates a new dimension with the specified preset<br>`-d` deletes the dimension with the specified preset (must be unloaded, which happens automatically when no players are inside) |
| /otg pregen | Sets the pre-generation radius for the player's current world to `<radius>` chunks in a given dimension. If no dimension given, assumes current. To disable the pregenerator, run the command again with a radius of 0 chunks.<br>Usage: `/otg pregen <radius> [dimension ID]` |

### Tips
* Generate a set of default world configs and check out OpenTerrainGenerator.ini and each world's WorldConfig.ini, comments and explanations of each setting are contained within the files.
* When using the pre-generator in single player, open the chat window so you can leave Minecraft running in the background without pausing the game.
