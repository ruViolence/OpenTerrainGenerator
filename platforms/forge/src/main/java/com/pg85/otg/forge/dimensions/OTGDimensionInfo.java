package com.pg85.otg.forge.dimensions;

import java.util.HashMap;

import com.pg85.otg.worldsave.DimensionData;

public class OTGDimensionInfo
{
	// Used to recreate dimensions in the correct order
	public int highestOrder;
	public HashMap<Integer, DimensionData> orderedDimensions;
	
	OTGDimensionInfo(int highestOrder, HashMap<Integer, DimensionData> orderedDimensions)
	{
		this.highestOrder = highestOrder;
		this.orderedDimensions = orderedDimensions;
	}
}
