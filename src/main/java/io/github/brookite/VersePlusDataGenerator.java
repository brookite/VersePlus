package io.github.brookite;

import io.github.brookite.datagen.ModBarteningLootTables;
import io.github.brookite.datagen.ModChestLootTables;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class VersePlusDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModBarteningLootTables::new);
        pack.addProvider(ModChestLootTables::new);
	}
}
