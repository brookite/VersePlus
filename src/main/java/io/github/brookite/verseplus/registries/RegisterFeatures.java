package io.github.brookite.verseplus.registries;

import com.mojang.serialization.MapCodec;
import io.github.brookite.verseplus.VersePlus;
import io.github.brookite.verseplus.worldgen.SuspiciousOceanFloorFeature;
import io.github.brookite.verseplus.worldgen.UnderwaterBlockPileFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class RegisterFeatures {
    public static final MapCodec<UnderwaterBlockPileFeature> UNDERWATER_BLOCK_PILE = Registry.register(
            BuiltInRegistries.FEATURE_TYPE,
            Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "underwater_block_pile"),
            UnderwaterBlockPileFeature.CODEC
    );
    public static final MapCodec<SuspiciousOceanFloorFeature> SUSPICIOUS_OCEAN_FLOOR = Registry.register(
            BuiltInRegistries.FEATURE_TYPE,
            Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "suspicious_ocean_floor"),
            SuspiciousOceanFloorFeature.CODEC
    );

    public static void initialize() {
    }
}
