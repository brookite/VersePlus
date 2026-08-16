package io.github.brookite.verseplus.mixin.accessor;

import java.io.IOException;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public interface LevelStorageAccessInvoker {
    @Invoker("modifyLevelDataWithoutDatafix")
    void verseplus$modifyLevelDataWithoutDatafix(Consumer<CompoundTag> updater) throws IOException;
}
