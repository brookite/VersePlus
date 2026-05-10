package io.github.brookite.verseplus.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class UnderwaterBlockPileFeature extends Feature<BlockPileConfiguration> {
    public UnderwaterBlockPileFeature(Codec<BlockPileConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockPileConfiguration> context) {
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPileConfiguration config = context.config();
        if (origin.getY() < level.getMinY() + 5) {
            return false;
        }

        int xr = 2 + random.nextInt(2);
        int zr = 2 + random.nextInt(2);
        boolean placedAny = false;

        for (BlockPos blockPos : BlockPos.betweenClosed(origin.offset(-xr, 0, -zr), origin.offset(xr, 1, zr))) {
            int xd = origin.getX() - blockPos.getX();
            int zd = origin.getZ() - blockPos.getZ();
            if (xd * xd + zd * zd <= random.nextFloat() * 10.0F - random.nextFloat() * 6.0F || random.nextFloat() < 0.031F) {
                placedAny |= this.tryPlaceBlock(level, blockPos, random, config);
            }
        }

        return placedAny;
    }

    private boolean mayPlaceOn(WorldGenLevel level, BlockPos blockPos) {
        BlockState belowState = level.getBlockState(blockPos.below());
        return belowState.is(Blocks.SAND) || belowState.is(Blocks.WET_SPONGE);
    }

    private boolean tryPlaceBlock(WorldGenLevel level, BlockPos blockPos, RandomSource random, BlockPileConfiguration config) {
        if (level.getBlockState(blockPos).is(Blocks.WATER) && this.mayPlaceOn(level, blockPos)) {
            level.setBlock(blockPos, config.stateProvider.getState(level, random, blockPos), 260);
            return true;
        }

        return false;
    }
}
