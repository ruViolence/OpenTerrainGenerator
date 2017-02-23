package com.khorn.terraincontrol.forge;

import com.google.common.collect.Maps;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ClientConfigProvider;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.forge.generator.BiomeGenCustom;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Responsible for loading and unloading the world.
 *
 */
public final class WorldLoader
{
    private final File configsDir;
    private final Map<String, ServerConfigProvider> configMap = Maps.newHashMap();
    public final HashMap<String, ForgeWorld> worlds = new HashMap<String, ForgeWorld>();

    WorldLoader(File configsDir)
    {        
        File dataFolder;
        try
        {
            Field minecraftDir = Loader.class.getDeclaredField("minecraftDir");
            minecraftDir.setAccessible(true);
            dataFolder = new File((File) minecraftDir.get(null), "mods" + File.separator + "TerrainControl");
        } catch (Throwable e)
        {
            dataFolder = new File("mods" + File.separator + "TerrainControl");
            System.out.println("Could not reflect the Minecraft directory, save location may be unpredicatble.");
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
        }
        this.configsDir = dataFolder;
    }

    public ForgeWorld getWorld(String name)
    {
        return this.worlds.get(name);
    }

    public File getConfigsFolder()
    {
        return this.configsDir;
    }

    public LocalWorld getWorld(World world)
    {
        return getWorld(WorldHelper.getName(world));
    }

    protected File getWorldDir(String worldName)
    {
        return new File(this.configsDir, "worlds/" + worldName);
    }

    /**
     * For a dedicated server, we need to register custom biomes
     * really early, even before we can know that TerrainControl
     * is the desired world type. As a workaround, we tentatively
     * load the configs if a config folder exists in the usual
     * location.
     * @param server The Minecraft server.
     */
    public void onServerAboutToLoad()
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || !server.isDedicatedServer())
        {
            // Registry works differently on singleplayer
            // We cannot load things yet
            return;
        }

        String worldName = ((DedicatedServer) server).getStringProperty("level-name", "");
        if (worldName.isEmpty())
        {
            return;
        }
        ForgeWorld forgeWorld = this.getOrCreateForgeWorld(worldName);
        if (forgeWorld == null)
        {
            // TerrainControl is probably not enabled for this world
            return;
        }
    }

    public void onServerStopped()
    {
        unloadAllWorlds();
    }

    public void unloadAllWorlds()
    {
    	ArrayList<ForgeWorld> worldsToRemove = new ArrayList<ForgeWorld>();
        for (ForgeWorld world : this.worlds.values())
        {
            if (world != null)
            {
            	worldsToRemove.add(world);
            }
        }
        for (ForgeWorld worldToRemove : worldsToRemove)
        {
            TerrainControl.log(LogMarker.INFO, "Unloading world \"{}\"...", worldToRemove.getName());
            this.configMap.remove(worldToRemove.getName());
            TerrainControl.log(LogMarker.INFO, "WorldLoader.worlds remove " + worldToRemove.getName());
            worldToRemove.unRegisterBiomes();
            this.worlds.remove(worldToRemove.getName());
        }
    }

    public void unloadWorld(ForgeWorld world)
    {
        TerrainControl.log(LogMarker.INFO, "Unloading world \"{}\"...", world.getName());
        TerrainControl.log(LogMarker.INFO, "WorldLoader.worlds remove " + world.getName());
        world.unRegisterBiomes();
        this.worlds.remove(world.getName());
    }

    public void addWorldConfig(String worldName, ServerConfigProvider config)
    {
        this.configMap.put(worldName, config);
    }

    @Nullable
    public ConfigProvider getWorldConfig(String worldName)
    {
        return this.configMap.get(worldName);
    }

    @Nullable
    public ForgeWorld getOrCreateForgeWorld(World mcWorld)
    {
        ForgeWorld forgeWorld = this.getOrCreateForgeWorld(WorldHelper.getName(mcWorld));
        if (forgeWorld != null && forgeWorld.getWorld() == null)
        {
            forgeWorld.provideWorldInstance((WorldServer) mcWorld);
        }

        return forgeWorld;
    }

    @Nullable
    public ForgeWorld getOrCreateForgeWorld(String worldName)
    {
        File worldConfigsFolder = this.getWorldDir(worldName);
        if (!worldConfigsFolder.exists())
        {
            // TerrainControl is probably not enabled for this world
            return null;
        }

        ForgeWorld world = this.getWorld(worldName);
        if (world == null)
        {
            world = new ForgeWorld(worldName);
            ServerConfigProvider config = this.configMap.get(worldName);
            if (config == null)
            {
                TerrainControl.log(LogMarker.INFO, "Loading configs for world \"{}\"..", world.getName());
                config = new ServerConfigProvider(worldConfigsFolder, world);
                // Remove fake biome to avoid Forge detecting it on restart and causing level.dat to be restored
                Iterator<Map.Entry<ResourceLocation, Biome>> iterator = Biome.REGISTRY.registryObjects.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Map.Entry<ResourceLocation, Biome> mapEntry = iterator.next();
                    Biome biome = mapEntry.getValue();
                    int biomeId = Biome.getIdForBiome(biome);
                    if (biomeId == BiomeGenCustom.MAX_TC_BIOME_ID)
                    {
                        iterator.remove();
                    }
                }
                IntIdentityHashBiMap<Biome> underlyingIntegerMap = new IntIdentityHashBiMap<Biome>(256);
                Iterator<Biome> biomeIterator = Biome.REGISTRY.underlyingIntegerMap.iterator();
                while (biomeIterator.hasNext())
                {
                    Biome biome = biomeIterator.next();
                    int biomeId = Biome.getIdForBiome(biome);
                    if (biomeId == BiomeGenCustom.MAX_TC_BIOME_ID)
                    {
                        continue;
                    }
                    underlyingIntegerMap.put(biome, biomeId);
                }
                Biome.REGISTRY.underlyingIntegerMap = underlyingIntegerMap;
            }
            world.provideConfigs(config);
            TerrainControl.log(LogMarker.INFO, "WorldLoader.worlds add " + worldName);
            this.worlds.put(worldName, world);
        }

        return world;
    }

    @SideOnly(Side.CLIENT)
    public void onQuitFromServer()
    {        
    	ArrayList<ForgeWorld> worldsToRemove = new ArrayList<ForgeWorld>();
        for (ForgeWorld world : this.worlds.values())
        {
            if (world != null)
            {
            	worldsToRemove.add(world);
            }
        }
        for (ForgeWorld worldToRemove : worldsToRemove)
        {
            TerrainControl.log(LogMarker.INFO, "Unloading world \"{}\"...", worldToRemove.getName());
            this.configMap.remove(worldToRemove.getName());
            TerrainControl.log(LogMarker.INFO, "WorldLoader.worlds remove " + worldToRemove.getName());
            worldToRemove.unRegisterBiomes();
            this.worlds.remove(worldToRemove.getName());
        }
    }

    @SideOnly(Side.CLIENT)
    public void unloadClientWorld(ForgeWorld world)
    {
        TerrainControl.log(LogMarker.INFO, "Unloading world \"{}\"...", world.getName());
        TerrainControl.log(LogMarker.INFO, "WorldLoader.worlds remove " + world.getName());
        this.worlds.remove(world.getName());
        world.unRegisterBiomes();
    }
    
    public static void clearBiomeDictionary()
    {
    	// TODO: This will remove all BiomeDict info, including non-TC biomes' BiomeDict info and may cause problems for other mods/worlds.
    	
		try {
			Field[] fields = BiomeDictionary.class.getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.isArray())
				{
			        field.setAccessible(true);			        
			        field.set(null, Array.newInstance(field.getType().getComponentType(), Array.getLength(field.get(null))));
				}
				if(fieldClass.getSuperclass().equals(java.util.AbstractMap.class))
				{
			        field.setAccessible(true);			        
			        field.set(null, new HashMap());
				}
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
    }
		
    // TODO: Should this be client only?? Like markBiomeIdsAsFree?
    // TODO: This will break multi-world support? It might remove vanilla-id biomes added by other worlds? 
    public static void unRegisterDefaultBiomes()
	{
		BitSet biomeRegistryAvailabiltyMap = getBiomeRegistryAvailabiltyMap();
		// Unregister default biomes so they can be replaced by TC biomes (this allows us to fully customise the biomes)
		for(DefaultBiome defaultBiome : DefaultBiome.values())
		{
			biomeRegistryAvailabiltyMap.set(defaultBiome.Id, false); // This should be enough to make Forge re-use the biome id
		}
		
		clearBiomeDictionary();
	}
	
	public static BitSet getBiomeRegistryAvailabiltyMap()
	{
		BitSet biomeRegistryAvailabiltyMap = null;
		try {
			Field[] fields = Biome.REGISTRY.getClass().getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(BitSet.class))
				{
					field.setAccessible(true);
					biomeRegistryAvailabiltyMap = (BitSet) field.get(Biome.REGISTRY);
			        break;
				}
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return biomeRegistryAvailabiltyMap;
	}

    @SideOnly(Side.CLIENT)
    public void registerClientWorld(WorldClient mcWorld, DataInputStream wrappedStream) throws IOException
    {
        ForgeWorld world = new ForgeWorld(ConfigFile.readStringFromStream(wrappedStream));
        ClientConfigProvider configs = new ClientConfigProvider(wrappedStream, world);
        world.provideClientConfigs(mcWorld, configs);
        TerrainControl.log(LogMarker.INFO, "WorldLoader.worlds add " + world.getName());
        this.worlds.put(world.getName(), world);
    }
}
