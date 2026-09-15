package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.mixin.accessor.UniformContainerBaseAccessor;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.level.storage.loot.entries.UniformContainerBase$EntryBase")
public abstract class LootEntryDefaultQualityMixin {
    @Redirect(
            method = "getWeight",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/storage/loot/entries/UniformContainerBase;quality:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int verseplus$useDefaultQuality(UniformContainerBase entry) {
        int quality = ((UniformContainerBaseAccessor) entry).verseplus$getQuality();
        return quality == 0 ? 1 : quality;
    }
}
