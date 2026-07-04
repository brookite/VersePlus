package io.github.brookite.verseplus.snifferegg.mixin;

import io.github.brookite.verseplus.snifferegg.SnifferEggFertility;
import io.github.brookite.verseplus.snifferegg.SnifferEggFertilitySavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "placeBlock", at = @At("RETURN"))
    private void transferSnifferEggFertility(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()
                || !state.is(Blocks.SNIFFER_EGG)
                || !context.getItemInHand().is(Items.SNIFFER_EGG)
                || !(context.getLevel() instanceof ServerLevel level)) {
            return;
        }

        SnifferEggFertilitySavedData.get(level)
                .setInfertile(context.getClickedPos(), SnifferEggFertility.isInfertile(context.getItemInHand()));
    }
}
