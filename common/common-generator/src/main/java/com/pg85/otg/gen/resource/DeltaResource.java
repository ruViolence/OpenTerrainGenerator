package com.pg85.otg.gen.resource;

import java.util.List;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.gen.resource.util.PositionHelper;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.OTGDirection;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.materials.MaterialSet;

public class DeltaResource extends FrequencyResourceBase
{
	private int baseSize;
	private int sizeVariance;
	private int baseRimSize;
	private int rimVariance;
	private int minAltitude;
	private int maxAltitude;
	private LocalMaterialData material;
	private LocalMaterialData rimMaterial;
	private final MaterialSet sourceBlocks;

	public DeltaResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader)
			throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(8, args);

		this.material = readMaterial(args.get(0), materialReader);
		this.rimMaterial = readMaterial(args.get(1), materialReader);
		this.frequency = readInt(args.get(2), 1, 100);
		this.rarity = readRarity(args.get(3));
		this.baseSize = readInt(args.get(4), 1, 5);
		this.sizeVariance = readInt(args.get(5), 0, 5);
		this.baseRimSize = readInt(args.get(6), 1, 5);
		this.rimVariance = readInt(args.get(7), 0, 5);
		this.minAltitude = readInt(args.get(8), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(9), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.sourceBlocks = readMaterials(args, 10, materialReader);

	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, int x, int z)
	{
		int y = world.getHighestBlockYAt(x, z, true, false, true, true, true);

		if (y < this.minAltitude || y > this.maxAltitude)
			return;

		boolean boolA = (random.nextDouble() < 0.9D);
		int rimSizeA = boolA
				? this.rimVariance == 0 ? this.baseRimSize : this.baseRimSize + random.nextInt(this.rimVariance + 1)
				: 0;
		int rimSizeB = boolA
				? this.rimVariance == 0 ? this.baseRimSize : this.baseRimSize + random.nextInt(this.rimVariance + 1)
				: 0;
		boolean boolB = (boolA && rimSizeA != 0 && rimSizeB != 0);

		int sizeA = this.sizeVariance == 0 ? this.baseSize : this.baseSize + random.nextInt(this.sizeVariance + 1);
		int sizeB = this.sizeVariance == 0 ? this.baseSize : this.baseSize + random.nextInt(this.sizeVariance + 1);
		int sizeMax = Math.max(sizeA, sizeB);

		int x2;
		int z2;
		for (int[] pos : PositionHelper.withinManhattan(x, y, z, sizeA, 0, sizeB))
		{
			if (PositionHelper.distManhattan(pos[0], pos[1], pos[2], x, y, z) > sizeMax)
				break;

			if (isClear(world, pos[0], pos[1], pos[2]))
			{
				if (boolB)
				{
					world.setBlockDirect(pos[0], pos[1], pos[2], this.rimMaterial);
				}
				x2 = pos[0] + rimSizeA;
				z2 = pos[2] + rimSizeB;
				if (isClear(world, x2, pos[1], z2))
				{
					world.setBlockDirect(x2, pos[1], z2, this.material);
				}
			}
		}
	}

	private boolean isClear(IWorldGenRegion world, int x, int y, int z)
	{
		LocalMaterialData material = world.getMaterialDirect(x, y, z);
		if (material.isMaterial(this.material))
			return false;

//		if (CANNOT_REPLACE.contains(lvt_3_1_.getBlock()))
//			return false; 

		int x2;
		int y2;
		int z2;
		for (OTGDirection direction : OTGDirection.values())
		{
			x2 = x + direction.getX();
			y2 = y + direction.getY();
			z2 = z + direction.getZ();
			boolean air = world.getMaterialDirect(x2, y2, z2).isAir();
			if ((air && direction != OTGDirection.UP) || (!air && direction == OTGDirection.UP))
				return false;

		}
		return true;
	}

	@Override
	public String toString()
	{
		return "Delta(" + this.material + "," + this.rimMaterial + "," + this.frequency + "," + this.rarity + ","
				+ this.baseSize + "," + this.sizeVariance + "," + this.baseRimSize + "," + this.rimVariance + ","
				+ this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
	}
}
