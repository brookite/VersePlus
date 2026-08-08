package io.github.brookite.verseplus.features.loyalpets.mixin;

import io.github.brookite.verseplus.features.loyalpets.ai.MultiOwnerFollowGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Cat.class)
public abstract class CatMixin extends TamableAnimal {
    protected CatMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void useMultiOwnerFollowGoal(CallbackInfo ci) {
        goalSelector.removeAllGoals(goal -> goal instanceof FollowOwnerGoal);
        goalSelector.addGoal(6, new MultiOwnerFollowGoal(this, 1.0, 10.0F, 5.0F));
    }
}
