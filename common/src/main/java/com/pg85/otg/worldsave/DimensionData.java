package com.pg85.otg.worldsave;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.logging.LogMarker;

// TODO: Since dimensionId's are stored in the dimensionconfig, and load order shouldn't matter (not even for 
// biome registration, since biome id's are saved after creation), is this still needed? 
// * This contains data for generated dims tho, which may not match or be edited via the config.yaml after creation (dimname/keeploaded). 
public class DimensionData
{
	public int dimensionOrder;
	public int dimensionId;
	public String dimensionName;
	public boolean keepLoaded;
	public long seed = 0; // TODO: Why is this not used? Remove?
	
    // Saving / Loading
    // TODO: It's crude but it works, can improve later
	
	public static void saveDimensionData(File worldSaveDirectory, ArrayList<DimensionData> dimensionData)
	{
		StringBuilder stringBuilder = new StringBuilder();
		for(DimensionData dimData : dimensionData)
		{
			stringBuilder.append((stringBuilder.length() == 0 ? "" : ",") + dimData.dimensionId + "," + dimData.dimensionName + "," + dimData.keepLoaded + "," + dimData.seed + "," + dimData.dimensionOrder);
		}
		saveDimensionData(worldSaveDirectory, stringBuilder);
	}

	public static void saveDimensionData(File worldSaveDirectory, StringBuilder stringBuilder)
	{
		File dimensionDataFile = new File(worldSaveDirectory + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.DimensionsDataFileName);
		File dimensionDataBackupFile = new File(worldSaveDirectory + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.DimensionsDataBackupFileName);
		
		BufferedWriter writer = null;
        try
        {
    		if(!dimensionDataFile.exists())
    		{
    			dimensionDataFile.getParentFile().mkdirs();
    		} else {
    			Files.move(dimensionDataFile.toPath(), dimensionDataBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    		}
        	
        	writer = new BufferedWriter(new FileWriter(dimensionDataFile));
            writer.write(stringBuilder.toString());
            OTG.log(LogMarker.DEBUG, "Custom dimension data saved");
        }
        catch (IOException e)
        {
			e.printStackTrace();
			throw new RuntimeException(
				"OTG encountered a critical error writing " + dimensionDataFile.getAbsolutePath() + ", exiting. "
				+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
				+ "If your world's " + WorldStandardValues.DimensionsDataFileName + " and its backup have been corrupted, "
				+ "you can replace it with a backup or create a new world with the same dimensions and copy its " 
				+ WorldStandardValues.DimensionsDataFileName + ".");
        }
        finally
        {
            try
            {
                writer.close();
            }
            catch (Exception e)
            {
            	String breakpoint = "";
            }
        }
	}
	
	public static ArrayList<DimensionData> loadDimensionData(File worldSaveDir)
	{
		File dimensionDataFile = new File(worldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.DimensionsDataFileName);
		File dimensionDataBackupFile = new File(worldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.DimensionsDataBackupFileName);		
		
		if(!dimensionDataFile.exists() && !dimensionDataBackupFile.exists())
		{
			return null;
		}		

		if(dimensionDataFile.exists())
		{
			String[] dimensionDataFileValues = {};
			boolean bSuccess = false;
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
				    bSuccess = true;				    
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				OTG.log(LogMarker.WARN, "Failed to load " + dimensionDataFile.getAbsolutePath() + ", trying to load backup.");
			}
			
			if(bSuccess)
			{
				try
				{
					return parseDimensionDataValues(dimensionDataFileValues);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					OTG.log(LogMarker.WARN, "Failed to load " + dimensionDataFile.getAbsolutePath() + ", trying to load backup.");
				}
			}
		}
		
		if(dimensionDataBackupFile.exists())
		{
			String[] dimensionDataFileValues = {};
			boolean bSuccess = false;
			try {
				StringBuilder stringbuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(dimensionDataBackupFile));
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
			    	bSuccess = true;				    
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			if(bSuccess)
			{
				try
				{
					return parseDimensionDataValues(dimensionDataFileValues);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		
		throw new RuntimeException(
			"OTG encountered a critical error loading " + dimensionDataFile.getAbsolutePath() + " and could not load a backup, exiting. "
			+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
			+ "If your world's " + WorldStandardValues.DimensionsDataFileName + " and its backup have been corrupted, "
			+ "you can replace it with a backup or create a new world with the same dimensions and copy its " 
			+ WorldStandardValues.DimensionsDataFileName + ".");			
	}
	
	private static ArrayList<DimensionData> parseDimensionDataValues(String[] dimensionDataFileValues)
	{
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
		    OTG.log(LogMarker.DEBUG, "Custom dimension data loaded");
		}
		return dimensionData;
	}

	// TODO: Move this somewhere more sensical?
	public static void deleteDimSavedData(Path worldSaveDir, DimensionConfig dimConfig)
	{
		Path dimensionSaveDir = Paths.get(worldSaveDir + File.separator + "DIM" + dimConfig.DimensionId);
		if(Files.exists(dimensionSaveDir) && Files.isDirectory(dimensionSaveDir))
		{
			OTG.log(LogMarker.INFO, "Deleting MC world save data for dimension " + dimConfig.DimensionId);
			try {
			    Files.walk(dimensionSaveDir)
			      .sorted(Comparator.reverseOrder())
			      .map(Path::toFile)
			      .forEach(File::delete);
			 
			    if(Files.exists(dimensionSaveDir))
			    {
			    	OTG.log(LogMarker.ERROR, "Could not delete directory: " + dimensionSaveDir.toString());
			    }
			} catch (IOException e) {
				OTG.log(LogMarker.ERROR, "Could not delete directory " + dimensionSaveDir.toString() + ". Error: " + e.toString());
				e.printStackTrace();
			}
		}
		
		dimensionSaveDir = Paths.get(worldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + "DIM-" + dimConfig.DimensionId);
		if(Files.exists(dimensionSaveDir) && Files.isDirectory(dimensionSaveDir))
		{
			OTG.log(LogMarker.INFO, "Deleting OTG world save data for dimension " + dimConfig.DimensionId);
			// Delete structure and pregenerator data
			try {	   										 
			    Files.walk(dimensionSaveDir)
			      .sorted(Comparator.reverseOrder())
			      .map(Path::toFile)
			      .forEach(File::delete);
			 
			    if(Files.exists(dimensionSaveDir))
			    {
			    	OTG.log(LogMarker.ERROR, "Could not delete directory: " + dimensionSaveDir.toString());
			    }
			} catch (IOException e) {
				OTG.log(LogMarker.ERROR, "Could not delete directory " + dimensionSaveDir.toString() + ". Error: " + e.toString());
				e.printStackTrace();
			}
			
			// Remove any biome id's used for the dim.
			ArrayList<BiomeIdData> biomeIds = BiomeIdData.loadBiomeIdData(worldSaveDir.toFile());
			ArrayList<BiomeIdData> newBiomeIds = new ArrayList<>();
			for(BiomeIdData biomeIdData : biomeIds)
			{
				if(!biomeIdData.biomeName.startsWith(dimConfig.PresetName + "_"))
				{
					newBiomeIds.add(biomeIdData);
				}
			}
			BiomeIdData.saveBiomeIdData(worldSaveDir.toFile(), newBiomeIds);
			
			// Remove any dimension data used for the dim,
			// update the load order for the remaining dims.
			ArrayList<DimensionData> dimensionData = loadDimensionData(worldSaveDir.toFile());
			ArrayList<DimensionData> newDimensionData = new ArrayList<>();
			int removedIndex = -1;
			for(DimensionData dimData : dimensionData)
			{
				if(dimData.dimensionId != dimConfig.DimensionId)
				{
					newDimensionData.add(dimData);
				} else {
					removedIndex = dimData.dimensionOrder;
				}
			}
			if(removedIndex > -1)
			{
				for(DimensionData dimData : newDimensionData)
				{
					if(dimData.dimensionOrder > removedIndex)
					{
						dimData.dimensionOrder = dimData.dimensionOrder - 1;	
					}
				}
			}
			saveDimensionData(worldSaveDir.toFile(), newDimensionData);
		}
	}	
}
