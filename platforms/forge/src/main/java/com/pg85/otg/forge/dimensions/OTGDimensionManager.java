package com.pg85.otg.forge.dimensions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionConfigGui;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.forge.generator.Cartographer;
import com.pg85.otg.forge.network.server.ServerPacketManager;
import com.pg85.otg.logging.LogMarker;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class OTGDimensionManager
{
	public static boolean isDimensionNameRegistered(String dimensionName)
	{
		if(dimensionName.equals("overworld"))
		{
			return true;
		}
		for(int i = -1000; i < Long.SIZE << 4; i++) // -1000 For other mods that add dimensions with id's below zero, hopefully -1000 is enough..
		{
			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType dimensionType = DimensionManager.getProviderType(i);

				if(dimensionType.getSuffix() != null && dimensionType.getSuffix().equals("OTG") && dimensionType.getName().equals(dimensionName))
				{
    				return true;
				}
			}
		}
		return false;
	}

	public static void registerDimension(int id, DimensionType type)
	{
		DimensionManager.registerDimension(id, type);

        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("dimensionID", id);
        ArrayList<String> types = new ArrayList<String>();
        types.add("OPEN_TERRAIN_GENERATOR");
        writeNBTStrings("types", types, compound);
        FMLInterModComms.sendMessage(OTGPlugin.MOD_ID, "registerDimension", compound);
	}

    public static void unregisterDimension(int dimensionId)
    {
    	if(dimensionId == 0)
    	{
    		return; // Never unregister the overworld
    	}

    	DimensionManager.unregisterDimension(dimensionId);

        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("dimensionID", dimensionId);

        ArrayList<String> types = new ArrayList<String>();
        types.add("OPEN_TERRAIN_GENERATOR");
        writeNBTStrings("types", types, compound);

        FMLInterModComms.sendMessage(OTGPlugin.MOD_ID, "unregisterDimension", compound);
    }

    public static void writeNBTStrings(String id, Collection<String> strings, NBTTagCompound compound)
    {
        if (strings != null)
        {
            NBTTagList nbtTagList = new NBTTagList();

            for (String s : strings)
                nbtTagList.appendTag(new NBTTagString(s));

            compound.setTag(id, nbtTagList);
        }
    }

	static HashMap<Integer,Integer> dimensionsOrder;

	public static int createDimension(String dimensionName, boolean keepLoaded, boolean initDimension, boolean saveDimensionData)
	{
		return createDimension(-1l, dimensionName, keepLoaded, initDimension, saveDimensionData);
	}
	
	public static int createDimension(long seed, String dimensionName, boolean keepLoaded, boolean initDimension, boolean saveDimensionData)
	{
		int newDimId = DimensionManager.getNextFreeDimId();

		registerDimension(newDimId, DimensionType.register(dimensionName, "OTG", newDimId, OTGWorldProvider.class, keepLoaded));
		
		if(initDimension)
		{
			initDimension(newDimId, seed);
		}

		int maxOrder = -1;
		for(Integer dimOrder : dimensionsOrder.values())
		{
			if(dimOrder > maxOrder)
			{
				maxOrder = dimOrder;
			}
		}
		dimensionsOrder.put(newDimId, maxOrder + 1);

		if(saveDimensionData)
		{
			SaveDimensionData();
		}

		return newDimId;
	}

	public static void DeleteDimension(int dimToRemove, ForgeWorld world, MinecraftServer server, boolean isServerSide)
	{
		if(DimensionManager.getWorld(dimToRemove) != null) // Can be null on the client if the world was unloaded(?)
		{
			DimensionManager.setWorld(dimToRemove, null, server);
		}
		if(DimensionManager.isDimensionRegistered(dimToRemove))
		{
			OTGDimensionManager.unregisterDimension(dimToRemove);
		}
	
		world.unRegisterBiomes();

		((ForgeEngine)OTG.getEngine()).getWorldLoader().RemoveUnloadedWorld(world.getName());

		// Client side only
		((ForgeEngine)OTG.getEngine()).getWorldLoader().RemoveLoadedWorld(world.getName());

		// For MP the client knows nothing about dimensions
		if(isServerSide)
		{
			OTGDimensionManager.UnloadCustomDimensionData(dimToRemove);
	
			BitSet dimensionMap = null;
			try {
				Field[] fields = DimensionManager.class.getDeclaredFields();
				for(Field field : fields)
				{
					Class<?> fieldClass = field.getType();
					if(fieldClass.equals(BitSet.class))
					{
						field.setAccessible(true);
						dimensionMap = (BitSet) field.get(new DimensionManager());
				        break;
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
	
			dimensionMap.clear(dimToRemove);
	
			// This biome was unregistered via a console command, delete its world data
			File dimensionSaveDir = new File(world.getWorld().getSaveHandler().getWorldDirectory() + "/DIM" + dimToRemove);
			if(dimensionSaveDir.exists() && dimensionSaveDir.isDirectory())
			{
				OTG.log(LogMarker.INFO, "Deleting world save data for dimension " + dimToRemove);
				try {
					FileUtils.deleteDirectory(dimensionSaveDir);
				} catch (IOException e) {
					OTG.log(LogMarker.ERROR, "Could not delete directory: " + e.toString());
					e.printStackTrace();
				}
			}

			SaveDimensionData();
			
			ArrayList<DimensionConfig> dimConfigs = new ArrayList<DimensionConfig>(OTG.GetDimensionsConfig().Dimensions);
			for(DimensionConfig dimensionConfig : dimConfigs)
			{
				if(dimensionConfig.PresetName.equals(world.getName()))
				{
					OTG.GetDimensionsConfig().Dimensions.remove(dimensionConfig);
					OTG.GetDimensionsConfig().Save();
					break;
				}
			}
		}
	}
	
	public static boolean DeleteDimensionServer(String worldName, MinecraftServer server)
	{	
		// First make sure world is unloaded            			
		if(((ForgeEngine)OTG.getEngine()).getWorldLoader().isWorldUnloaded(worldName))
		{
			ForgeWorld forgeWorld = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getUnloadedWorld(worldName);
			OTGDimensionManager.DeleteDimension(forgeWorld.getDimensionId(), forgeWorld, server, true);
			ServerPacketManager.SendDimensionSynchPacketToAllPlayers(server);
			return true;
		}		
		return false;
	}

	public static void initDimension(int dim)
	{
		initDimension(dim, -1l);
	}
		
    public static void initDimension(int dim, long seed)
    {
    	String dimensionName = DimensionManager.getProviderType(dim).getName();
        WorldServer overworld = DimensionManager.getWorld(0);
        if (overworld == null)
        {
            throw new RuntimeException("Cannot Hotload Dim: Overworld is not Loaded!");
        }

        try
        {
            DimensionManager.getProviderType(dim);
        }
        catch (Exception e)
        {
            System.err.println("Cannot Hotload Dim: " + e.getMessage());
            return; // If a provider hasn't been registered then we can't hotload the dim
        }
        MinecraftServer mcServer = overworld.getMinecraftServer();
        ISaveHandler savehandler = overworld.getSaveHandler();

        DimensionConfig dimConfig = OTG.GetDimensionsConfig().GetDimensionConfig(dimensionName); 
        if(seed == -1 && dimConfig != null && dimConfig.Seed != null && dimConfig.Seed.trim().length() > 0)
        {
        	seed = Long.parseLong(dimConfig.Seed.trim());
        }
        
        WorldConfig worldConfig = ((ForgeEngine)OTG.getEngine()).LoadWorldConfigFromDisk(new File(OTG.getEngine().getWorldsDirectory(), dimConfig.PresetName));
        
		long seedIn = seed == -1 ? (long) Math.floor((Math.random() * Long.MAX_VALUE)) : seed;
		GameType gameType = dimConfig.GameType.equals("Creative") ? GameType.CREATIVE : GameType.SURVIVAL;
		boolean enableMapFeatures = worldConfig.strongholdsEnabled;
		boolean hardcoreMode = dimConfig.GameType.equals("Hardcore");

		WorldSettings settings = new WorldSettings(seedIn, gameType, enableMapFeatures, hardcoreMode, OTGPlugin.txWorldType);
		settings.setGeneratorOptions("OpenTerrainGenerator");
		
		WorldInfo worldInfo = new WorldInfo(settings, dimensionName);
        WorldServer world = (WorldServer)(new OTGWorldServerMulti(mcServer, savehandler, dim, overworld, mcServer.profiler, worldInfo).init());       
        ApplyGameRulesToWorld(world, dimConfig);
        world.addEventListener(new ServerWorldEventHandler(mcServer, world));
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));

        // Save seed to world config TODO: Remove this
        ForgeWorld forgeWorld = ((ForgeEngine)OTG.getEngine()).getWorldByDimId(dim);
        if(forgeWorld == null)
        {
        	forgeWorld = ((ForgeEngine)OTG.getEngine()).getUnloadedWorldByDimId(dim);
        }
        
        OTG.GetDimensionsConfig().GetDimensionConfig(forgeWorld.getName()).Seed = "" + seedIn;
        OTG.GetDimensionsConfig().Save();
        
        // Apply difficulty and game type
        // TODO: Independent difficulty / spawn types per world?
        if (!mcServer.isSinglePlayer())
        {
            world.getWorldInfo().setGameType(mcServer.getGameType());
        }

        if (world.getWorldInfo().isHardcoreModeEnabled())
        {
        	world.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
        	world.setAllowedSpawnTypes(true, true);
        }
        else if (mcServer.isSinglePlayer())
        {
        	world.getWorldInfo().setDifficulty(mcServer.getDifficulty());
            world.setAllowedSpawnTypes(world.getDifficulty() != EnumDifficulty.PEACEFUL, true);
        } else {
        	world.getWorldInfo().setDifficulty(mcServer.getDifficulty());
            world.setAllowedSpawnTypes(mcServer.allowSpawnMonsters(), mcServer.getCanSpawnAnimals());
        }
    }
    
    public static void ApplyGameRulesToWorld(World world, DimensionConfig dimConfig)
    {
        world.getGameRules().setOrCreateGameRule("commandBlockOutput", dimConfig.GameRules.CommandBlockOutput + "");
		world.getGameRules().setOrCreateGameRule("disableElytraMovementCheck", dimConfig.GameRules.DisableElytraMovementCheck + "");
		world.getGameRules().setOrCreateGameRule("doDaylightCycle", dimConfig.GameRules.DoDaylightCycle + "");
		world.getGameRules().setOrCreateGameRule("doEntityDrops", dimConfig.GameRules.DoEntityDrops + "");
		world.getGameRules().setOrCreateGameRule("doFireTick", dimConfig.GameRules.DoFireTick + "");
		world.getGameRules().setOrCreateGameRule("doLimitedCrafting", dimConfig.GameRules.DoLimitedCrafting + "");
		world.getGameRules().setOrCreateGameRule("doMobLoot", dimConfig.GameRules.DoMobLoot + "");
		world.getGameRules().setOrCreateGameRule("doMobSpawning", dimConfig.GameRules.DoMobSpawning + "");
		world.getGameRules().setOrCreateGameRule("doTileDrops", dimConfig.GameRules.DoTileDrops + "");
		world.getGameRules().setOrCreateGameRule("doWeatherCycle", dimConfig.GameRules.DoWeatherCycle + "");
		world.getGameRules().setOrCreateGameRule("gameLoopFunction", dimConfig.GameRules.GameLoopFunction + "");
		world.getGameRules().setOrCreateGameRule("keepInventory", dimConfig.GameRules.KeepInventory + "");
		world.getGameRules().setOrCreateGameRule("logAdminCommands", dimConfig.GameRules.LogAdminCommands + "");
		world.getGameRules().setOrCreateGameRule("maxCommandChainLength", dimConfig.GameRules.MaxCommandChainLength + "");
		world.getGameRules().setOrCreateGameRule("maxEntityCramming", dimConfig.GameRules.MaxEntityCramming + "");
        world.getGameRules().setOrCreateGameRule("mobGriefing", dimConfig.GameRules.MobGriefing + "");
		world.getGameRules().setOrCreateGameRule("naturalRegeneration", dimConfig.GameRules.NaturalRegeneration + "");
		world.getGameRules().setOrCreateGameRule("randomTickSpeed", dimConfig.GameRules.RandomTickSpeed + "");
        world.getGameRules().setOrCreateGameRule("reducedDebugInfo", dimConfig.GameRules.ReducedDebugInfo + "");
		world.getGameRules().setOrCreateGameRule("sendCommandFeedback", dimConfig.GameRules.SendCommandFeedback + "");
		world.getGameRules().setOrCreateGameRule("showDeathMessages", dimConfig.GameRules.ShowDeathMessages + "");
		world.getGameRules().setOrCreateGameRule("spawnRadius", dimConfig.GameRules.SpawnRadius + "");
        world.getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", dimConfig.GameRules.SpectatorsGenerateChunks + "");       
    }

    // Saving / Loading
    // TODO: It's crude but it works, can improve later

	public static void SaveDimensionData()
	{
		World world = DimensionManager.getWorld(0);
		File dimensionDataFile = new File(world.getSaveHandler().getWorldDirectory() + "/OpenTerrainGenerator/Dimensions.txt");
		if(dimensionDataFile.exists())
		{
			dimensionDataFile.delete();
		}

		StringBuilder stringbuilder = new StringBuilder();

		for(int i = 0; i < Long.SIZE << 4; i++)
		{
			if(i == 1)
			{
				continue; // Ignore dim 1 (End)
			}
			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType dimType = DimensionManager.getProviderType(i);
				if(dimType != null)
				{
					ForgeWorld forgeWorld = (ForgeWorld) OTG.getWorld(dimType.getName());
					if(forgeWorld == null)
					{
						forgeWorld = (ForgeWorld) OTG.getUnloadedWorld(dimType.getName());
					}
					if(forgeWorld == null)
					{
						continue; // If another mod added a dimension
					}

					if(forgeWorld != null)
					{
						stringbuilder.append((stringbuilder.length() == 0 ? "" : ",") + i + "," + dimType.getName() + "," + dimType.shouldLoadSpawn() + "," + forgeWorld.getSeed() + "," + dimensionsOrder.get(i));
					}
				}
			}
		}

		BufferedWriter writer = null;
        try
        {
        	dimensionDataFile.getParentFile().mkdirs();
        	writer = new BufferedWriter(new FileWriter(dimensionDataFile));
            writer.write(stringbuilder.toString());
            OTG.log(LogMarker.TRACE, "Custom dimension data saved");
        }
        catch (IOException e)
        {
        	OTG.log(LogMarker.ERROR, "Could not save custom dimension data.");
            e.printStackTrace();
        }
        finally
        {
            try
            {
                writer.close();
            } catch (Exception e) { }
        }
	}

	public static void UnloadAllCustomDimensionData()
	{
		dimensionsOrder = new HashMap<Integer,Integer>();
		dimensionsOrder.put(0,0);

		BitSet dimensionMap = null;
		try
		{
			Field[] fields = DimensionManager.class.getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(BitSet.class))
				{
					field.setAccessible(true);
					dimensionMap = (BitSet) field.get(new DimensionManager());
			        break;
				}
			}
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}

		for(int i = 2; i < Long.SIZE << 4; i++) // Ignore dim 0 (Overworld) and 1 (End)
		{
			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType dimType = DimensionManager.getProviderType(i);

				if(dimType != null && dimType.getSuffix() != null && dimType.getSuffix().equals("OTG"))
				{
					OTGDimensionManager.unregisterDimension(i);
					dimensionMap.clear(i);
				}
			}
		}
	}

	public static void UnloadCustomDimensionData(int dimId)
	{
		if(dimId == 0) // Never unregister dim 0 (overworld) from DimensionManager.dimensions
		{
			return;
		}

		dimensionsOrder.remove(dimId);

		BitSet dimensionMap = null;
		try
		{
			Field[] fields = DimensionManager.class.getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(BitSet.class))
				{
					field.setAccessible(true);
					dimensionMap = (BitSet) field.get(new DimensionManager());
			        break;
				}
			}
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}

		if(DimensionManager.isDimensionRegistered(dimId))
		{
			DimensionType dimType = DimensionManager.getProviderType(dimId);

			if(dimType != null && dimType.getSuffix() != null && dimType.getSuffix().equals("OTG"))
			{
				OTGDimensionManager.unregisterDimension(dimId);
				dimensionMap.clear(dimId);
			}
		}
	}

	public static OTGDimensionInfo LoadOrderedDimensionData()
	{
		World world = DimensionManager.getWorld(0);
		File dimensionDataFile = new File(world.getSaveHandler().getWorldDirectory() + "/OpenTerrainGenerator/Dimensions.txt");
		String[] dimensionDataFileValues = {};
		if(dimensionDataFile.exists())
		{
			try {
				StringBuilder stringbuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(dimensionDataFile));
				try {
					String line = reader.readLine();

				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
				    if(stringbuilder.length() > 0)
				    {
				    	dimensionDataFileValues = stringbuilder.toString().split(",");
				    }
				    OTG.log(LogMarker.TRACE, "Custom dimension data loaded");
				} finally {
					reader.close();
				}

			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		ArrayList<DimensionData> dimensionData = new ArrayList<DimensionData>();
		if(dimensionDataFileValues.length > 0)
		{
			for(int i = 0; i < dimensionDataFileValues.length; i += 5)
			{
				DimensionData dimData = new DimensionData();
				dimData.dimensionId = Integer.parseInt(dimensionDataFileValues[i]);
				dimData.dimensionName = dimensionDataFileValues[i + 1];
				dimData.keepLoaded = Boolean.parseBoolean(dimensionDataFileValues[i + 2]);
				dimData.seed = Long.parseLong(dimensionDataFileValues[i + 3]);
				dimData.dimensionOrder = Integer.parseInt(dimensionDataFileValues[i + 4]);
				dimensionData.add(dimData);
			}
		}

		// Store the order in which dimensions were added
		dimensionsOrder = new HashMap<Integer, Integer>();
		dimensionsOrder.put(0,0);
		HashMap<Integer, DimensionData> orderedDimensions = new HashMap<Integer, DimensionData>();
		int highestOrder = 0;
		for(DimensionData dimData : dimensionData)
		{
			dimensionsOrder.put(dimData.dimensionId, dimData.dimensionOrder);
			orderedDimensions.put(dimData.dimensionOrder, dimData);
			if(dimData.dimensionOrder > highestOrder)
			{
				highestOrder = dimData.dimensionOrder;
			}
		}

		return new OTGDimensionInfo(highestOrder, orderedDimensions);
	}

	public static ArrayList<DimensionData> GetDimensionData(File worldSaveDir)
	{
		File dimensionDataFile = new File(worldSaveDir + "/OpenTerrainGenerator/Dimensions.txt");
		String[] dimensionDataFileValues = {};
		if(dimensionDataFile.exists())
		{
			try {
				StringBuilder stringbuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(dimensionDataFile));
				try {
					String line = reader.readLine();

				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
				    if(stringbuilder.length() > 0)
				    {
				    	dimensionDataFileValues = stringbuilder.toString().split(",");
				    }
				    OTG.log(LogMarker.TRACE, "Custom dimension data loaded");
				} finally {
					reader.close();
				}

			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		ArrayList<DimensionData> dimensionData = new ArrayList<DimensionData>();
		if(dimensionDataFileValues.length > 0)
		{
			for(int i = 0; i < dimensionDataFileValues.length; i += 5)
			{
				DimensionData dimData = new DimensionData();
				dimData.dimensionId = Integer.parseInt(dimensionDataFileValues[i]);
				dimData.dimensionName = dimensionDataFileValues[i + 1];
				dimData.keepLoaded = Boolean.parseBoolean(dimensionDataFileValues[i + 2]);
				dimData.seed = Long.parseLong(dimensionDataFileValues[i + 3]);
				dimData.dimensionOrder = Integer.parseInt(dimensionDataFileValues[i + 4]);
				dimensionData.add(dimData);
			}
		}
		
		return dimensionData;
	}
	
	public static void LoadCustomDimensionData()
	{
		OTGDimensionInfo otgDimData = LoadOrderedDimensionData();

		DimensionsConfig dimsConfig = OTG.GetDimensionsConfig();
		
		// Recreate dimensions in the correct order
		for(int i = 0; i <= otgDimData.highestOrder; i++)
		{
			if(otgDimData.orderedDimensions.containsKey(i))
			{
				DimensionData dimData = otgDimData.orderedDimensions.get(i);

				if(!DimensionManager.isDimensionRegistered(dimData.dimensionId))
				{
					if(dimData.dimensionId != 0)
					{
						boolean bFound = false;
						for(DimensionConfig dimConfig : dimsConfig.Dimensions)
						{
							if(dimConfig.PresetName.equals(dimData.dimensionName))
							{
								bFound = true;
								break;
							}
						}
						if(!bFound)
						{
							// No DimensionConfig exists for this dimension
							// Must be a legacy dimension, create a config for it based on the worldconfig
							WorldConfig worldConfig = ((ForgeEngine)OTG.getEngine()).LoadWorldConfigFromDisk(new File(OTG.getEngine().getWorldsDirectory(), dimData.dimensionName));
							if(worldConfig == null)
							{
								throw new RuntimeException("Could not initialise dimension " + dimData.dimensionId + "\", OTG preset \"" + dimData.dimensionName + "\" is not installed.");
							}
							DimensionConfigGui dimConfig = new DimensionConfigGui(dimData.dimensionName, worldConfig);
							dimsConfig.Dimensions.add(new DimensionConfig(dimConfig));
						}
					} else {
						if(dimsConfig.Overworld == null)
						{
							// No DimensionConfig exists for the overworld
							// Must be a legacy world, create a config for it based on the worldconfig
							WorldConfig worldConfig = ((ForgeEngine)OTG.getEngine()).LoadWorldConfigFromDisk(new File(OTG.getEngine().getWorldsDirectory(), dimData.dimensionName));
							if(worldConfig == null)
							{
								throw new RuntimeException("Could not initialise dimension " + dimData.dimensionId + "\", OTG preset \"" + dimData.dimensionName + "\" is not installed.");
							}
							DimensionConfigGui dimConfig = new DimensionConfigGui(dimData.dimensionName, worldConfig);
							dimsConfig.Overworld = new DimensionConfig(dimConfig);
						}
					}
					
					OTGDimensionManager.registerDimension(dimData.dimensionId, DimensionType.register(dimData.dimensionName, "OTG", dimData.dimensionId, OTGWorldProvider.class, dimData.keepLoaded));
					if(dimData.dimensionName.equals("DIM-Cartographer"))
					{
						Cartographer.CartographerDimension = dimData.dimensionId;
					}
					OTGDimensionManager.initDimension(dimData.dimensionId);
				}
			}
		}
		
		dimsConfig.Save();
	}

	private static Hashtable<Integer, Object> oldDims;
	public static void RemoveOTGDims()
	{
    	Hashtable dimensions = null;

		try
		{
			Field[] fields = DimensionManager.class.getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(Hashtable.class))
				{
					field.setAccessible(true);
					Hashtable fieldAsHashTable = (Hashtable) field.get(new DimensionManager());
					if(fieldAsHashTable.values().size() > 0)
					{
						Object value = fieldAsHashTable.values().toArray()[0];
						if(value instanceof DimensionType || !(value instanceof WorldServer)) // Forge 1.11.2 - 13.20.0.2228 uses DimensionType, Forge 1.11.2 - 13.20.0.2315 uses Dimension
						{
							dimensions = fieldAsHashTable;
					        break;
						}
					}
				}
			}
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}

		oldDims = new Hashtable<Integer, Object>();
		for(int i = 2; i < Long.SIZE << 4; i++) // Ignore dim 0 (Overworld) and 1 (End)
		{
			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType type = DimensionManager.getProviderType(i);
				if(type.getSuffix() != null && type.getSuffix().equals("OTG"))
				{
					oldDims.put(i, dimensions.get(i));
					dimensions.remove(i);
				}
			}
		}
	}

	public static HashMap<Integer, String> GetAllOTGDimensions()
	{
		HashMap<Integer, String> otgDims = new HashMap<Integer, String>();

		for(int i = 0; i < Long.SIZE << 4; i++)
		{
			if(i == 1)
			{
				continue; // Ignore dim 1 (End)
			}

			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType type = DimensionManager.getProviderType(i);
				if(type.getSuffix() != null && type.getSuffix().equals("OTG"))
				{
					otgDims.put(new Integer(type.getId()), type.getName());
				}
			}
		}

		return otgDims;
	}

	// TODO: Document this
	public static void ReAddOTGDims()
	{
    	Hashtable dimensions = null;

		try
		{
			Field[] fields = DimensionManager.class.getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(Hashtable.class))
				{
					field.setAccessible(true);
					Hashtable fieldAsHashTable = (Hashtable) field.get(new DimensionManager());
					if(fieldAsHashTable.values().size() > 0)
					{
						Object value = fieldAsHashTable.values().toArray()[0];
						if(value instanceof DimensionType || !(value instanceof WorldServer)) // Forge 1.11.2 - 13.20.0.2228 uses DimensionType, Forge 1.11.2 - 13.20.0.2315 uses Dimension
						{
							dimensions = fieldAsHashTable;
					        break;
						}
					}
				}
			}
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}

		if(oldDims != null)
		{
			for(Entry<Integer, Object> oldDim : oldDims.entrySet())
			{
				dimensions.put(oldDim.getKey(), oldDim.getValue());
			}
		}
		oldDims = new Hashtable<Integer, Object>();
	}

	public static void CreateNewDimensionSP(DimensionConfig dimensionConfig, MinecraftServer server)
	{
		DimensionsConfig dimsConfig = OTG.GetDimensionsConfig();
		dimensionConfig.isNewConfig = false;
		dimsConfig.Dimensions.add(dimensionConfig);
		
		// Create new world
		long seed = (long) Math.floor((Math.random() * Long.MAX_VALUE));   	        				
		try
		{
			seed = Long.parseLong(dimensionConfig.Seed);
		}
		catch(NumberFormatException ex)
		{
			// TODO
		}
		
		OTGDimensionManager.createDimension(seed, dimensionConfig.PresetName, false, true, true);
		ForgeWorld createdWorld = (ForgeWorld) OTG.getWorld(dimensionConfig.PresetName);
		if(createdWorld == null)
		{
			createdWorld = (ForgeWorld) OTG.getUnloadedWorld(dimensionConfig.PresetName);
		}
		if(dimensionConfig.Settings.CanDropChunk)
		{
			DimensionManager.unloadWorld(createdWorld.getWorld().provider.getDimension());
		}

		OTG.GetDimensionsConfig().Save();
	}
}
