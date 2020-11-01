OTG can be installed both as a Spigot plugin and as a Forge mod. As a Forge mod, players are required to have the mod installed client-side. The Spigot plugin does not require the single player mod to be installed, however certain features (foliage/grass/sky/fog colours & biome names in F3 menu) will not function properly for custom biomes.

Below is an outline of the different methods of installation for the various platforms, be aware that without [installing a world preset](/installation/installing-worlds.md) OTG will replace the overworld with an approximation of vanilla worldgen and the configs OTG driving that in `mods/openterraingenerator/worlds` or `plugins/openterraingenerator/worlds`.

## Forge Installation Instructions
### Forge Single Player Edit
1. Go to [files.minecraftforge.net](https://files.minecraftforge.net/) to download & install the latest recommended forge corresponding to the version of Minecraft you want to use.
2. Run Minecraft once to create the `/mods` folder (usually found at `C:\Users\[USERNAME]\AppData\Roaming\.minecraft\mods\` ).
3. [Download OpenTerrainGenerator.jar](https://minecraft.curseforge.com/projects/open-terrain-generator) and put it in the `.minecraft/mods` folder.
4. [Install a world preset](/installation/installing-worlds.md)

*Note: OTG usually requires 2gb+ memory to be assigned to Minecraft (depending on world preset). If you experience long/never ending world loading times, increase memory allocation.*

### Forge Server
1. Go to files.minecraftforge.net to download & install the latest recommended forge corresponding to the version of Minecraft you want to use.
2. Download the OpenTerrainGenerator.jar put it in the server's mods folder.
3. Install a world preset
4. Go to the server.properties file and set level-type to OTG and ensure level-name is set to your world pack name (e.g. Biome Bundle).

If you have already generated any chunks with vanilla or other world settings, delete the level.dat and region files of your main world to get one with the correct level type.

## Spigot Installation Instructions
The tutorial for setting up a Spigot server for the first time is [here](https://www.spigotmc.org/wiki/spigot-installation/).

1. Set up the server in the normal way.
2. Download and put the Open Terrain Generator .jar into the server's plugins folder.
3. Run the server once to create the /plugins/OpenTerrainGenerator folder (delete the world afterwards).
4. [Install a world preset](/installation/installing-worlds.md)

[Spigot Installation Video Tutorial](https://www.youtube.com/watch?v=ThYH_YsX4EU)

Although clients do not require Forge or OTG installed when joining a Spigot server running OTG, it is recommended that clients do install them if the world preset in use makes heavy use of custom foliage, grass or sky colors.

### Spigot console command permissions

The following console commands can be be used with Spigot permissions plugins:

* cmd.tp
* cmd.list
* cmd.biome
* cmd.map
* cmd.check
* cmd.spawn
* cmd.help
* cmd.reload

For a description of each of them see [the usage page](/usage/gui-and-commands.md).

## Other Tips
* OTG for Spigot doesn't require your players to have the singleplayer (forge) version installed (meaning vanilla clients can connect), but they will get some more features if they have it.
* Both servers and clients running OTG will require more memory than vanilla Minecraft. If you are experiencing long/endless world loading times or other performance issues it is recommended you increase Minecraft's allocated memory to 2gb+

## Versions & Platforms
The newest releases of OTG are available only for Minecraft 1.12.2, both for Spigot and Forge. Older versions are available for 1.10.2 and 1.11.2.

## Mod Compatibility
Open Terrain Generator works well alongside most other mods and has Forge Biome Dictionary integration to help with modded resource placement, as well as mob spawning rule inheritance to allow biome creators to apply automatic compatibility with mods which spawn mobs on a per biome basis. There are some exceptions, of course. Below are a list of mods that are known to cause incompatibilities or other problems:

* Mods that change terrain generation like Realistic Terrain Generation/Alternate Terrain Generation/Biomes O` Plenty will either not work or crash.
  * Custom cave mods should work fine with v8 and above
* Certain mods, like Pixelmon, don't use Forge Biome Dictionary and you might have problems getting things to spawn in custom biomes. Pixelmon's pokemon is a common example of this.
* MoCreatures 'CustomMobSpawner' conflicts with OTG's mob spawning mechanics (use MoCreatures without it until this can be solved).
* NetherPortalFix causes a crash on teleportation


There are other mods that can cause issues if added after the world is created, mostly mods that add new biomes. This can crash the game in v8, and can lead to unintended behaviours in v9. It's strongly advised to not add any mods post world creation.

## Sponge Server
OTG does not officially support Sponge. The mod is not tested with Sponge, nor does OTG interact directly with the Sponge API. That being said, newer versions of OTG should work with Sponge.

If you're running Sponge alongside a permissions plugin (like LuckPerms), then you should be extra careful which version of OTG you use. OTG v9 contains a vital fix for permissions for Sponge, and using any version below v9 opens your world up to serious damage from players. OTG v9 adds the following permissions for Sponge servers:

* `openterraingenerator.command.<command name>`
  * Gives a player access to a given command. Not all commands require permissions. To see a list of commands, do /otg help, or see [Console Commands](/usage/gui-and-commands.md)
* `openterraingenerator.ui.create`
  * Allows a player to create or delete a dimension using the GUI
* `openterraingenerator.ui.update`
  * Allows a player to update world settings using the GUI
* `openterraingenerator.ui.teleport`
  * Allows a player to teleport between dimensions using the GUI
