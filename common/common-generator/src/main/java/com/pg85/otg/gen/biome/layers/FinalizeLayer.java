package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;
import com.pg85.otg.util.interfaces.IBiomeConfig;

/**
 * Gets the biome id of the sample of this position by removing the extra land and other data.
 */
class FinalizeLayer implements ParentedLayer
{
	private final boolean riversEnabled;
	private int[] riverBiomes;
	private IBiomeConfig[] biomes;
	
	public FinalizeLayer(boolean riversEnabled, IBiomeConfig[] biomes, int[] riverBiomes)
	{
		this.riversEnabled = riversEnabled;
		this.biomes = biomes;
		this.riverBiomes = riverBiomes;
	}
	
	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);
        if ((sample & BiomeLayers.LAND_BIT) != 0)
        {
       		sample = sample & BiomeLayers.BIOME_BITS;	
        } else {
        	// TODO: Ocean/FrozenOcean based on ICE_BIT and worldConfig.frozenOcean.
        	// This will work for backwards compatibility, but will need to be 
        	// re-designed for the new ocean biomes?
        	sample = 0;
        }
		
        if (this.riversEnabled && (sample & BiomeLayers.RIVER_BITS) != 0)
		{
    		int riverBiomeId = this.riverBiomes[sample];
    		if(riverBiomeId >= 0)
    		{
    			sample = riverBiomeId;
    		}
		}
        
		return sample;
	}
}