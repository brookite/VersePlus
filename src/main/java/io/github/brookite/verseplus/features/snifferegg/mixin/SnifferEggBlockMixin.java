package io.github.brookite.verseplus.features.snifferegg.mixin;

import io.github.brookite.verseplus.features.snifferegg.SnifferEggFertilitySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SnifferEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnifferEggBlock.class)
public class SnifferEggBlockMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sometimesHatchWithoutOffspring(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (state.getValue(SnifferEggBlock.HATCH) < SnifferEggBlock.MAX_HATCH_LEVEL
                || !SnifferEggFertilitySavedData.get(level).isInfertile(pos)) {
            return;
        }

        level.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
        ci.cancel();
    }
}
