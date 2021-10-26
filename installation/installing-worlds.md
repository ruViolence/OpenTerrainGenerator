_Note: Both servers and clients running OTG will require more memory than vanilla Minecraft. If you are experiencing long/endless world loading times or other performance issues it is recommended you increase Minecraft's allocated memory to 2gb+._

### Download links to Team OTG presets:
* [Biome Bundle](https://minecraft.curseforge.com/projects/biome-bundle)
* [Vanilla Vistas](https://www.curseforge.com/minecraft/mc-mods/vanilla-vistas)
* [Skylands](https://minecraft.curseforge.com/projects/otg-skylands?gameCategorySlug=mc-mods&projectID=265901)
* [Flatlands](https://minecraft.curseforge.com/projects/otg-flatlands?gameCategorySlug=mc-mods&projectID=265902)
* [Void](https://minecraft.curseforge.com/projects/otg-the-void?gameCategorySlug=mc-mods&projectID=265903)
* [Dungeons](https://www.curseforge.com/minecraft/mc-mods/otg-dungeons)
* [Biome Bundle O Plenty](https://www.curseforge.com/minecraft/mc-mods/biome-bundle-o-plenty)

### Download links to community presets

* [Dregora](https://www.curseforge.com/minecraft/mc-mods/dregora)
* [Terra Incognita](https://www.curseforge.com/minecraft/mc-mods/terra-incognita)
* [Dungeoneering](https://drive.google.com/file/d/1weNLc3frosh-Na2HvPs9mugcU1wFKaSX/view)
* [Mysource Testworld](https://mega.nz/#!XMYiwBxa!7CECuNUUGT30O3zA1ik3x1vcnQmzPskFc-mbbxFZU-M)
  * Provided by [Wahrheit](https://github.com/SXRWahrheit), who purchased the rights from mysource.
* [Traveler's Dream](https://www.curseforge.com/minecraft/mc-mods/travelers-dream)

## 1.16 and above Installation instructions

Presets are being updated for 1.16.x and will be available on CurseForge as soon as possible. In the mean time, 1.12.2 presets can be used with OTG 1.16.x. See also: [Installing OTG](/OpenTerrainGenerator/installation/installing-otg)

### Installing jars created with PresetPacker

OTG 1.16 and above includes support for the newest iteration of PresetPacker, which allows presets to be distributed in easy-to-use .jar format for both Forge and Spigot/Paper. The process for installing presets created with PresetPacker is simple.

#### Forge Single-Player

1. Download the packed .jar file for the preset you want to use. **The preset must be packed with PresetPacker and updated for 1.16 and above**
2. Install the preset by copying the .jar file to your `mods` folder
3. Select the OpenTerrainGenerator world type in the world creation menu and select your desired preset

#### Forge Server

1. Download the packed .jar file for the preset you want to use. **The preset must be packed with PresetPacker and updated for 1.16 and above**
2. Install the preset by copying the .jar file to your `mods` folder
3. Configure `server.properties`:
* Set `level-type: otg` and `generator-settings: presetname`. `presetname` should match either the folder name of one of your installed presets (`/config/OpenTerrainGenerator/Presets/` for Forge), or the name of a .yaml file in the `/OpenTerrainGenerator/DimensionConfigs/` folder.


#### Spigot/Paper

1. Download the packed .jar file for the preset you want to use. **The preset must be packed with PresetPacker and updated for 1.16 and above.**
2. Install the preset by copying the .jar file to your `mods` folder for Forge or `plugins` folder for Spigot/Paper
3. Configure `bukkit.yml` or Multiverse for Spigot/Paper
* bukkit.yml:
```yaml
worlds:
    world_name:
        generator: OpenTerrainGenerator:Wildlands
```
* Multiverse: `/mv create my_world_name normal -g OpenTerrainGenerator:PresetName` (e.g. `/mv create vanilla_vistas normal -g "OpenTerrainGenerator:Vanilla Vistas"` for a preset name that contains spaces)


### Using 1.12.2 presets with OTG 1.16.x

1.12.2 preset jars (like Biome Bundle.jar) downloaded from CurseForge will not automatically install for 1.16.x. However, you can unpack the .jar and copy preset files yourself:

1. Use an app like WinRar to unpack the .jar like you'd unpack a .zip or .rar file.
2. Find the preset folder(s) in unpackedjar/assets/worldpacker/presetname.
3. Copy the preset folder(s) to config/OpenTerrainGenerator/Presets/ for Forge or plugins/OpenTerrainGenerator/Presets/ for Spigot/Paper.
4. This way, 1.12.2 presets can be used with OTG 1.16.5, however presets will need to be updated to make use of all the new features.

**Note**: When using 1.12.2 presets with OTG 1.16.5, some mods don't work due to missing biome categories or biome dictionary tags in OTG biomes, configured via the BiomeConfig (.bc) files. Ice and Fire requires the "overworld" tag for example, which is not configured for 1.12.2 OTG presets. Presets are being updated and will be released on CurseForge. Users can also manually add categories/tags to BiomeConfig (.bc) files (`BiomeCategory:`, `BiomeDictTags:`).

### Generating a default "vanilla worldgen" preset to edit

If no world preset is installed, when generating a world OTG will generate a set of configs that approximate 'vanilla' Minecraft world generation in the /OpenTerrainGenerator/Presets/Default folder (found in either the /mods or /plugins folder depending on platform).

## 1.12 and below Installation instructions

### Forge Single Player

1. [Install OTG](/OpenTerrainGenerator/installation/installing-otg)
2. Download the .jar for your desired world preset from the links above and put it in the `/mods` folder.
3. Select the world from the world creation menu in-game.

### Forge Server

1. [Install OTG](/OpenTerrainGenerator/installation/installing-otg)
2. Download the .jar for your desired world preset from the links above and put it in the `/mods` folder.
3. Go to the server.properties file and change the following two settings (replacing <worldname> with the name of the world preset (ie, level-name=Biome Bundle):

```
level-type=OTG
level-name=worldname
```
_Note: If you have already generated any chunks with vanilla or other world settings, delete the world folder of your main world to begin generating one with the correct level type._

### Spigot server

Watch the [video tutorial](https://youtu.be/3n0bFhnFERE) or follow the instructions below

1. [Install OTG](/OpenTerrainGenerator/installation/installing-otg)
2. Run the server once to create the /plugins/OpenTerrainGenerator folder
3. Delete the world folders for the world that was generated
4. Download the .jar for your desired world preset from the links above and open it with WinRaR or 7zip 
  * **Do not put the world preset .jar in the plugins folder**
5. The folder structure should be: \assets\worldpacker\<worldname> (where in place of <worldname> you will have the name of the world preset you downloaded (e.g Biome Bundle). Copy that folder to \plugins\OpenTerrainGenerator\worlds (in your server) If done correctly it'll look something like this:
```
\plugins\OpenTerrainGenerator\worlds\<worldname>
```
With the following subfolders:
```
\WorldBiomes\
\WorldObjects\
\WorldConfig.ini
```
6. Ensure that `level-name` in `server.properties` is set to the name of the world you're using - for example, `Biome Bundle`.
7. Add the following to your bukkit.yml, altering it as necessary with the name of the world you're using:
```
worlds:
  Biome Bundle:
    generator: OpenTerrainGenerator
```

### Manually installing a (non-jar) preset

If you have an existing Terrain Control preset or an OTG preset not in .jar format but instead consisting of a world directory containing its Worldconfig.ini and WorldObjects and WorldBiomes subfolders, follow the installation instructions above but instead of installing a .jar file, put your world preset folder into the `/OpenTerrainGenerator/worlds` folder.


