package io.github.brookite.mixin;

import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.MannequinEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MannequinEntity.class)
public abstract class MannequinNameMixin extends Entity {
    public MannequinNameMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow protected abstract void setHideDescription(boolean hideDescription);

    @Shadow
    protected abstract ProfileComponent getMannequinProfile();

    @Inject(method= "setMannequinProfile(Lnet/minecraft/component/type/ProfileComponent;)V",  at=@At("TAIL"))
    public void setMannequinProfile(ProfileComponent profileComponent, CallbackInfo ci) {
        MannequinEntity entity = ((MannequinEntity)(Object)this);
        if (profileComponent != null && !entity.hasCustomName() && profileComponent.getName().isPresent()) {
            entity.setCustomName(Text.of(getMannequinProfile().getName().get()));
        }
    }

    @Override
    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        if (this.hasCustomName())  {
            this.setCustomNameVisible(true);
        }
    }

    @Inject(method = "Lnet/minecraft/entity/decoration/MannequinEntity;readCustomData(Lnet/minecraft/storage/ReadView;)V", at=@At("TAIL"))
    public void readCustomData(ReadView readView, CallbackInfo ci) {
        this.setHideDescription(readView.getBoolean("hide_description", true));
    }

    @Inject(method= "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at=@At("TAIL"))
    private void init(CallbackInfo ci) {
        this.setHideDescription(true);
    }
}
