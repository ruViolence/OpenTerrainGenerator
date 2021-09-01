package com.pg85.otg.gen.resource.util;

import java.util.Random;

import com.google.common.collect.AbstractIterator;

public class PositionHelper
{
	public static Iterable<int[]> randomBetweenClosed(Random random, int limit, int x1, int y1, int z1, int x2, int y2,
			int z2)
	{
		int width = x2 - x1 + 1;
		int height = y2 - y1 + 1;
		int depth = z2 - z1 + 1;

		return () -> new AbstractIterator<int[]>()
		{
			int counter = limit;

			protected int[] computeNext()
			{
				if (this.counter <= 0)
					return endOfData();

				int[] pos = new int[]
				{ x1 + random.nextInt(width), y1 + random.nextInt(height), z1 + random.nextInt(depth) };

				this.counter--;
				return pos;
			}
		};
	}

	public static Iterable<int[]> betweenClosed(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		int width = x2 - x1 + 1;
		int height = y2 - y1 + 1;
		int depth = z2 - z1 + 1;
		int end = width * height * depth;

		return () -> new AbstractIterator<int[]>()
		{
			private int index;

			protected int[] computeNext()
			{
				if (this.index == end)
					return endOfData();

				int lvt_1_1_ = this.index % width;
				int lvt_2_1_ = this.index / width;
				int lvt_3_1_ = lvt_2_1_ % height;
				int lvt_4_1_ = lvt_2_1_ / height;
				this.index++;

				return new int[]
				{ x1 + lvt_1_1_, y1 + lvt_3_1_, z1 + lvt_4_1_ };
			}
		};
	}

	public static int distManhattan(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		float lvt_2_1_ = Math.abs(x2 - x1);
		float lvt_3_1_ = Math.abs(y2 - y1);
		float lvt_4_1_ = Math.abs(z2 - z1);
		return (int) (lvt_2_1_ + lvt_3_1_ + lvt_4_1_);
	}

	public static Iterable<int[]> withinManhattan(int originX, int originY, int originZ, int reachX, int reachY, int reachZ)
	{
		int maxDepth = reachX + reachY + reachZ;

		return () -> new AbstractIterator<int[]>()
		{
			private int[] cursor = new int[3];
			private int currentDepth;

			private int maxX;
			private int maxY;
			private int x;
			private int y;

			private boolean zMirror;

			protected int[] computeNext()
			{
				if (this.zMirror)
				{
					this.zMirror = false;
					this.cursor[2] = (originZ - this.cursor[2] - originZ);
					return this.cursor;

				}
				int[] lvt_1_1_ = null;
				while (lvt_1_1_ == null)
				{
					if (this.y > this.maxY)
					{
						this.x++;
						if (this.x > this.maxX)
						{
							this.currentDepth++;
							if (this.currentDepth > maxDepth)
								return endOfData();

							this.maxX = Math.min(reachX, this.currentDepth);
							this.x = -this.maxX;
						}
						this.maxY = Math.min(reachY, this.currentDepth - Math.abs(this.x));
						this.y = -this.maxY;

					}
					int lvt_2_1_ = this.x;
					int lvt_3_1_ = this.y;
					int lvt_4_1_ = this.currentDepth - Math.abs(lvt_2_1_) - Math.abs(lvt_3_1_);
					if (lvt_4_1_ <= reachZ)
					{
						this.zMirror = (lvt_4_1_ != 0);
						lvt_1_1_ = new int[]
						{ originX + lvt_2_1_, originY + lvt_3_1_, originZ + lvt_4_1_ };
						this.cursor = lvt_1_1_;
					}
					this.y++;
				}
				return lvt_1_1_;
			}
		};
	}
}
