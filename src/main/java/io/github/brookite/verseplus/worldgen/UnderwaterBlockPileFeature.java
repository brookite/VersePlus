package io.github.brookite.verseplus.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record UnderwaterBlockPileFeature(Holder<BlockStateProvider> stateProvider) implements Feature {
    public static final MapCodec<UnderwaterBlockPileFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(UnderwaterBlockPileFeature::stateProvider)
    ).apply(instance, UnderwaterBlockPileFeature::new));
    private static final int MAX_BLOCKS_PER_PILE = 4;

    @Override
    public MapCodec<UnderwaterBlockPileFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (origin.getY() < level.getMinY() + 5) {
            return false;
        }

        int xr = 2 + random.nextInt(2);
        int zr = 2 + random.nextInt(2);
        boolean placedAny = false;
        int blocksToPlace = 1 + random.nextInt(MAX_BLOCKS_PER_PILE);

        for (BlockPos blockPos : BlockPos.betweenClosed(origin.offset(-xr, 0, -zr), origin.offset(xr, 1, zr))) {
            int xd = origin.getX() - blockPos.getX();
            int zd = origin.getZ() - blockPos.getZ();
            if (xd * xd + zd * zd <= random.nextFloat() * 10.0F - random.nextFloat() * 6.0F || random.nextFloat() < 0.031F) {
                if (this.tryPlaceBlock(level, blockPos, random)) {
                    placedAny = true;
                    blocksToPlace--;
                    if (blocksToPlace == 0) {
                        break;
                    }
                }
            }
        }

        return placedAny;
    }

    private boolean mayPlaceOn(WorldGenLevel level, BlockPos blockPos) {
        BlockState belowState = level.getBlockState(blockPos.below());
        return belowState.is(Blocks.SAND) || belowState.is(Blocks.WET_SPONGE);
    }

    private boolean tryPlaceBlock(WorldGenLevel level, BlockPos blockPos, RandomSource random) {
        if (level.getBlockState(blockPos).is(Blocks.WATER) && this.mayPlaceOn(level, blockPos)) {
            level.setBlock(blockPos, this.stateProvider.value().getState(level, random, blockPos), Block.UPDATE_ALL);
            return true;
        }

        return false;
    }
}
