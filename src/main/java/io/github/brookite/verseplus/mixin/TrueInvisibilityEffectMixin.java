package io.github.brookite.verseplus.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class TrueInvisibilityEffectMixin {

    @Inject(
            method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddStatusEffect(
            StatusEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir
    ) {
        if (effect.getEffectType() == StatusEffects.INVISIBILITY
                && effect.getAmplifier() >= 1
                && effect.shouldShowParticles()) {

            StatusEffectInstance noParticles = new StatusEffectInstance(
                    effect.getEffectType(),
                    effect.getDuration(),
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    false,
                    true
            );

            LivingEntity self = (LivingEntity) (Object) this;
            cir.setReturnValue(self.addStatusEffect(noParticles, source));
        }
    }
}
