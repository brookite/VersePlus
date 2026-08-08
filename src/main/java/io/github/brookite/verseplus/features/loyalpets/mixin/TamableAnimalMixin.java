package io.github.brookite.verseplus.features.loyalpets.mixin;

import io.github.brookite.verseplus.features.loyalpets.data.LoyalPetAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TamableAnimal.class)
public class TamableAnimalMixin {
    @Inject(method = "isOwnedBy", at = @At("HEAD"), cancellable = true)
    private void recognizeAdditionalOwner(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        TamableAnimal pet = (TamableAnimal) (Object) this;
        if (LoyalPetAttachments.isAdditionalOwner(pet, entity.getUUID())) {
            cir.setReturnValue(true);
        }
    }
}
