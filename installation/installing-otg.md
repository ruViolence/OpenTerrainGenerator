OTG can be installed both as a Spigot plugin and as a Forge mod. As a Forge mod, players are required to have the mod installed client-side. The Spigot plugin does not require the single player mod to be installed, however certain features (foliage/grass/sky/fog colours & biome names in F3 menu) will not function properly for custom biomes.

Below is an outline of the different methods of installation for the various platforms. Be aware that without [installing a world preset](/OpenTerrainGenerator/installation/installing-worlds) OTG will replace the overworld with an approximation of vanilla worldgen and the configs OTG driving that in `mods/openterraingenerator/worlds` or `plugins/openterraingenerator/worlds`.

# 1.16 and above

OTG can be installed as a Paper plugin or as a Forge mod using the same OTG .jar file. Fabric is not yet supported.

As a Paper plugin, OTG is not required client-side. For Forge servers, the client-side mod is required, but we intend to lift this requirement before 1.0.0. When OTG is run server-side without a client, features like foliage/grass/sky/fog colors don't work fully (plain colors instead of gradient colors), and features like dimension portals for Forge are disabled.

Installation instructions for each platform are included below. Please be aware that without installing a world preset OTG will use its default preset, which is made to look like a vanilla overworld. The Default preset is unpacked when Minecraft loads to `/config/OpenTerrainGenerator/Presets/Default/` for Forge or `/plugins/OpenTerrainGenerator/Presets/Default/` for Paper. Configuration files can be edited with any text editor.

## Forge Installation Instructions

The process for installing presets is slightly different depending on whether you are installing OTG for your client or for a server.

### Forge Single Player

1. Go to files.minecraftforge.net to download & install the latest recommended Forge release for Minecraft 1.16.5.
2. Run Minecraft once to create the /mods folder (usually found at `C:\Users\[USERNAME]\AppData\Roaming\.minecraft\mods\`).
3. Download [OpenTerrainGenerator.jar](https://www.curseforge.com/minecraft/mc-mods/open-terrain-generator) and put it in the .minecraft/mods folder.
4. [Install a world preset](/OpenTerrainGenerator/installation/installing-worlds)

*Note: If you experience long world loading times, increase memory allocation. OTG usually requires 2GB+ memory to be assigned to Minecraft (depending on world preset).*

### Forge Server

1. Go to [files.minecraftforge.net](https://files.minecraftforge.net) to download & install the latest recommended Forge release for Minecraft 1.16.5.
2. Download [OpenTerrainGenerator.jar](https://www.curseforge.com/minecraft/mc-mods/open-terrain-generator) and put it in the server's mods folder.
3. [Install a world preset](/OpenTerrainGenerator/installation/installing-worlds)
4. Configure server.properties
5. (Optional) Include a DimensionConfig.yaml file to define custom OTG Overworld/Nether/End and add OTG dimensions.

*Note: If you have already generated any chunks with vanilla or other world settings, delete the level.dat and region files of your main world to get one with the correct level type.*

### server.properties

Set `level-type: otg` and `generator-settings: presetname`. `presetname` should match either the folder name of one of your installed presets (`/config/OpenTerrainGenerator/Presets/` for Forge), or the name of a .yaml file in the `/OpenTerrainGenerator/DimensionConfigs/` folder.

### Dimension configs / Modpack configuration (SP&MP)

Dimension configs can be used for Forge MP servers to assign custom OTG Overworld/Nether/End/dimensions, or to define a non-OTG overworld with OTG Nether/End/dimensions. For Forge SP, if a dimension config called `Modpack.yaml` is found, the world creation menu is replaced by a custom screen that locks settings and allows users to create a new world with 1 click, using the settings from `Modpack.yaml`.

To create a dimension config file, add a .yaml file to `/OpenTerrainGenerator/DimensionConfigs/` (create DimensionConfigs folder if necessary). For Forge MP, the .yaml file is used by setting its filename (without .yaml) as server.properties generator-settings. When the MP server is started, the dimensions in the .yaml file should be created automatically.

The .yaml file should look as follows:

```yaml
#TODO: Provide instructions for modpack devs.
---
Version: 1
ModpackName: "My Awesome Modpack"
Overworld:
  NonOTGWorldType: "flat"
  NonOTGGeneratorSettings:
  PresetFolderName:
  Seed: 10
Nether:
  PresetFolderName: "AlienJungle"
  Seed: 11
End:
  PresetFolderName: "NewBB"
  Seed: 12
Dimensions:
- PresetFolderName: "Wildlands"
  Seed: 14
  PortalColor: "beige"
  PortalMob: "minecraft:zombified_piglin"
  PortalIgnitionSource: "minecraft:flint_and_steel"
  PortalBlocks: "REDSTONE_BLOCK"
Settings:
  GenerateStructures: true
  BonusChest: false
GameRules:
  DoFireTick: true
  MobGriefing: true
  KeepInventory: false
  DoMobSpawning: true
  DoMobLoot: true
  DoTileDrops: true
  DoEntityDrops: true
  CommandBlockOutput: true
  NaturalRegeneration: true
  DoDaylightCycle: true
  LogAdminCommands: true
  ShowDeathMessages: true
  RandomTickSpeed: 3
  SendCommandFeedback: true
  SpectatorsGenerateChunks: true
  SpawnRadius: 10
  DisableElytraMovementCheck: false
  MaxEntityCramming: 24
  DoWeatherCycle: true
  DoLimitedCrafting: false
  MaxCommandChainLength: 65536
  AnnounceAdvancements: true
  DisableRaids: false
  DoInsomnia: true
  DrowningDamage: true
  FallDamage: true
  FireDamage: true
  DoPatrolSpawning: true
  DoTraderSpawning: true
  ForgiveDeadPlayers: true
  UniversalAnger: false
  ```
  
* **Overworld**: Can be set to OTG or non-OTG. To use an OTG preset, use PresetFolderName. To use a non-OTG overworld, use NonOTGWorldType and NonOTGGeneratorSettings.
* **Nether**: Optional, if PresetFolderName has a value, the OTG preset is used for the Nether.
* **End**: Optional, if PresetFolderName has a value, the OTG preset is used for the End.
* **Dimensions**: Optional, List. Add as many dimensions as you like, each entry starting with a dash (-).
* **Settings**: Optional, used for Forge SP world creation menu if the file is named Modpack.yaml.
* **GameRules**: Optional, used for Forge MP, and for Forge SP if the file is named Modpack.yaml. If missing, default game rules are used.
* **ModpackName**: Optional, Shown as the title in OTG world creation menu's for Forge SP if the file is named Modpack.yaml.
* **Version**: Ignored at the moment.
* **Seed**: can be set to -1 for a random seed. For the Overworld seed is ignored and taken from server.properties (MP) or world creation menu (SP). *In some cases, dimension seeds may be locked to overworld seed, still need to fix this.*

Dimensions only:

* **PortalBlock**: One or more blocks that can be used to build a portal. For example: "minecraft:stone, minecraft:dirt".
* **PortalMob**: The mob that occasionaly spawns from this portal, "minecraft:zombified_piglin" by default.
* **PortalColor**: The color of the portal. Options are: beige, black, blue, crystalblue, darkblue, darkgreen, darkred, emerald, flame, gold, green, grey, lightblue, lightgreen, orange, pink, red, white, yellow, default.
* **PortalIgnitionSource**: The ignition source for the portal, usually "minecraft:flint_and_steel".

If left empty, portal settings from the preset's WorldConfig are used.

### Mod Compatibility (Forge)

OTG uses MC biome categories and Forge Biome Dictionary tags for its biomes. Modded mobs, resources and structures should spawn in OTG worlds, provided they are configured correctly.

Mods that add carvers (caves, ravines) like Yung's Better Caves also work, OTG's carvers should be automatically disabled when using them.

Terrain/Biome generation mods like TerraForged, Biomes o' Plenty, Oh the Biomes You'll Go etc can be used with OTG. However, modded biomes will only spawn in OTG worlds if the OTG preset includes mod compatibility configs for the biomes. This can be done via template biomes using biome category and biome dictionary tags (see WorldConfig BiomeGroups/TemplateBiomes and BiomeConfig TemplateForBiome). Other mods are not able to spawn OTG biomes properly, this is something we'll look at for the future. Note that it is also possible to make a non-OTG (modded or vanilla) overworld with OTG Nether/End and dimensions.

When using **Quark**, set `"Improved Sleeping" = false` in `quark-common.toml`, or you won't be able to sleep in OTG dimensions.

When using **Biomes o' Plenty**, set `use_world_type = false` in `client.toml` or OTG's custom world creation screen for modpack configs won't work properly.

When using 1.12.2 presets with OTG 1.16.5, some mods don't work due to missing biome categories or biome dictionary tags in OTG biomes, configured via the BiomeConfig (.bc) files. Ice and Fire requires the "overworld" tag for example, which is not configured for 1.12.2 OTG presets. Presets are being updated and will be released on CurseForge. Users can also manually add categories/tags to BiomeConfig (.bc) files (`BiomeCategory:`, `BiomeDictTags:`).

### Mod recommendations (Forge)

ChunkPregenerator integrates seamlessly with OTG world creation UI and provides pre-generation/preview features for all dimensions.

## Paper Installation

1. Set up the server in the normal way.
2. Download and put [OpenTerrainGenerator.jar](https://www.curseforge.com/minecraft/mc-mods/open-terrain-generator) into the server's plugins folder.
3. [Install a world preset](/OpenTerrainGenerator/installation/installing-worlds)
4. Specify OTG as the generator for your desired world:
* With MultiVerse: use the generator flag to define OTG and the desired preset, for example: "-g OpenTerrainGenerator:Wildlands"
* Without, add a generator entry for each desired world to your Bukkit.yml file, for example:
```yaml
worlds:
    world_name:
        generator: OpenTerrainGenerator:Wildlands
```

Although players do **not** require an OTG client for Paper servers, using a Forge OTG client enables features like complex foliage/grass/sky/fog colors.

# 1.12 and below

The instructions below are preserved for legacy users. The installation process for 1.16 and above differs from that for 1.12 and below.

## Forge Installation Instructions

The process for installing presets is slightly different depending on whether you are installing OTG for your client or for a server.

### Forge Single Player
1. Go to [files.minecraftforge.net](https://files.minecraftforge.net/) to download & install the latest recommended forge corresponding to the version of Minecraft you want to use.
2. Run Minecraft once to create the `/mods` folder (usually found at `C:\Users\[USERNAME]\AppData\Roaming\.minecraft\mods\` ).
3. [Download OpenTerrainGenerator.jar](https://minecraft.curseforge.com/projects/open-terrain-generator) and put it in the `.minecraft/mods` folder.
4. [Install a world preset](/OpenTerrainGenerator/installation/installing-worlds)

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
4. [Install a world preset](/OpenTerrainGenerator/installation/installing-worlds)

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

For a description of each of them see [the usage page](/OpenTerrainGenerator/usage/gui-and-commands).

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
  * Gives a player access to a given command. Not all commands require permissions. To see a list of commands, do /otg help, or see [Console Commands](/OpenTerrainGenerator/usage/gui-and-commands)
* `openterraingenerator.ui.create`
  * Allows a player to create or delete a dimension using the GUI
* `openterraingenerator.ui.update`
  * Allows a player to update world settings using the GUI
* `openterraingenerator.ui.teleport`
  * Allows a player to teleport between dimensions using the GUI
