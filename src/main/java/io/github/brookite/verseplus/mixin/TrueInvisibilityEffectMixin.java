package io.github.brookite.verseplus.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class TrueInvisibilityEffectMixin {

    @Inject(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddStatusEffect(
            MobEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir
    ) {
        if (effect.getEffect() == MobEffects.INVISIBILITY
                && effect.getAmplifier() >= 1
                && effect.isVisible()) {

            MobEffectInstance noParticles = new MobEffectInstance(
                    effect.getEffect(),
                    effect.getDuration(),
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    false,
                    true
            );

            LivingEntity self = (LivingEntity) (Object) this;
            cir.setReturnValue(self.addEffect(noParticles, source));
        }
    }
}
