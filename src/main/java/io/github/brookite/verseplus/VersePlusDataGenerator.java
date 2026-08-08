package io.github.brookite.verseplus;

import io.github.brookite.verseplus.datagen.GrassTrapPitStructureProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class VersePlusDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<GrassTrapPitStructureProvider>)
				GrassTrapPitStructureProvider::new);
	}
}
