package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.mixin.accessor.LootPoolSingletonContainerAccessor;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$EntryBase")
public abstract class LootEntryDefaultQualityMixin {
    @Redirect(
            method = "getWeight",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/storage/loot/entries/LootPoolSingletonContainer;quality:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int verseplus$useDefaultQuality(LootPoolSingletonContainer entry) {
        int quality = ((LootPoolSingletonContainerAccessor) entry).verseplus$getQuality();
        return quality == 0 ? 1 : quality;
    }
}
