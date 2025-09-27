package io.github.brookite.mixin;

import net.minecraft.entity.decoration.MannequinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MannequinEntity.class)
public abstract class MannequinNameMixin {
    @Shadow protected abstract void setHideDescription(boolean hideDescription);

    @Inject(method= "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at=@At("TAIL"))
    private void init(CallbackInfo ci) {
        this.setHideDescription(true);
        ((MannequinEntity)(Object)this).setCustomNameVisible(true);
    }
}
