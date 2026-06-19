package io.github.brookite.verseplus.worldgen;

import com.mojang.serialization.Codec;
import io.github.brookite.verseplus.VersePlus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

public class SuspiciousOceanFloorFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "archaeology/cold_ocean_floor")
    );

    public SuspiciousOceanFloorFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos waterPos = context.origin();
        BlockPos floorPos = waterPos.below();

        if (!level.getBlockState(waterPos).is(Blocks.WATER)) {
            return false;
        }

        BlockState floorState = level.getBlockState(floorPos);
        BlockState replacement = getSuspiciousReplacement(floorState);
        if (replacement == null) {
            return false;
        }

        RandomSource random = context.random();
        level.setBlock(floorPos, replacement, Block.UPDATE_ALL);

        BlockEntity blockEntity = level.getBlockEntity(floorPos);
        if (blockEntity instanceof BrushableBlockEntity brushableBlockEntity) {
            brushableBlockEntity.setLootTable(LOOT_TABLE, random.nextLong());
        }

        return true;
    }

    private static BlockState getSuspiciousReplacement(BlockState floorState) {
        if (floorState.is(Blocks.SAND)) {
            return Blocks.SUSPICIOUS_SAND.defaultBlockState();
        }
        if (floorState.is(Blocks.GRAVEL)) {
            return Blocks.SUSPICIOUS_GRAVEL.defaultBlockState();
        }

        return null;
    }
}
