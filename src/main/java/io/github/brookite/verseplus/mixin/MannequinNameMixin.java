package io.github.brookite.verseplus.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mannequin.class)
public abstract class MannequinNameMixin extends Entity {
    public MannequinNameMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow protected abstract void setHideDescription(boolean hideDescription);

    @Shadow
    protected abstract ResolvableProfile getProfile();

    @Inject(method= "setProfile(Lnet/minecraft/world/item/component/ResolvableProfile;)V",  at=@At("TAIL"))
    public void setMannequinProfile(ResolvableProfile profileComponent, CallbackInfo ci) {
        Mannequin entity = ((Mannequin)(Object)this);
        if (profileComponent != null && !entity.hasCustomName() && profileComponent.name().isPresent()) {
            entity.setCustomName(Component.nullToEmpty(getProfile().name().get()));
        }
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        if (this.hasCustomName())  {
            this.setCustomNameVisible(true);
        }
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", at=@At("TAIL"))
    public void readCustomData(ValueInput readView, CallbackInfo ci) {
        this.setHideDescription(readView.getBooleanOr("hide_description", true));
    }

    @Inject(method= "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at=@At("TAIL"))
    private void init(CallbackInfo ci) {
        this.setHideDescription(true);
    }
}
