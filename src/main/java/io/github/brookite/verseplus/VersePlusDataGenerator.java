package io.github.brookite.verseplus;

import io.github.brookite.verseplus.registries.RegisterFeatures;
import io.github.brookite.verseplus.registries.RegisterPlacedFeatures;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class VersePlusDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
	}

	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
		registryBuilder
				.addRegistry(RegistryKeys.CONFIGURED_FEATURE, RegisterFeatures::bootstrap)
				.addRegistry(RegistryKeys.PLACED_FEATURE, RegisterPlacedFeatures::bootstrap);
	}
}
