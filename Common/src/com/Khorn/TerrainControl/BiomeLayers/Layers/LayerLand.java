package com.khorn.terraincontrol.biomelayers.layers;


import com.khorn.terraincontrol.biomelayers.ArraysCache;

public class LayerLand extends Layer
{
    public LayerLand(long paramLong, Layer paramGenLayer, int _chance)
    {
        super(paramLong);
        this.child = paramGenLayer;
        this.chance = 101 - _chance;
    }

    public int chance = 5;

    @Override
    public int[] GetBiomes(int cacheId, int x, int z, int x_size, int z_size)
    {

        int[] arrayOfInt1 = this.child.GetBiomes(cacheId, x, z, x_size, z_size);

        int[] arrayOfInt2 = ArraysCache.GetArray(cacheId, x_size * z_size);
        for (int i = 0; i < z_size; i++)
        {
            for (int j = 0; j < x_size; j++)
            {
                SetSeed(x + j, z + i);
                if (nextInt(chance) == 0)
                    arrayOfInt2[(j + i * x_size)] = arrayOfInt1[(j + i * x_size)] | LandBit;
                else
                    arrayOfInt2[(j + i * x_size)] = arrayOfInt1[(j + i * x_size)];
            }
        }

        return arrayOfInt2;
    }
}
