package io.github.brookite.verseplus.snifferegg.mixin;

import io.github.brookite.verseplus.snifferegg.SnifferEggFertilitySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "destroy", at = @At("HEAD"))
    private void clearRemovedSnifferEggFertility(LevelAccessor level, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (state.is(Blocks.SNIFFER_EGG) && level instanceof ServerLevel serverLevel) {
            SnifferEggFertilitySavedData.get(serverLevel).setInfertile(pos, false);
        }
    }
}
