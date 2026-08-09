package io.github.brookite.verseplus.features.containerlocks;

import io.github.brookite.verseplus.VersePlus;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ContainerLockComponents {
    public static final DataComponentType<LockData> LOCK_DATA = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "lock_data"),
            DataComponentType.<LockData>builder().persistent(LockData.CODEC).cacheEncoding().build()
    );

    private ContainerLockComponents() {
    }

    public static void initialize() {
    }
}
