package io.github.brookite.verseplus.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.brookite.verseplus.VersePlusChances.WEAVING_SPIDER_NATURAL_SPAWN_CHANCE;

@Mixin(Mob.class)
public abstract class MobNaturalSpawnEffectsMixin extends LivingEntity {
    private static final int PERMANENT_SPAWN_EFFECT_DURATION = Integer.MAX_VALUE;

    protected MobNaturalSpawnEffectsMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;",
            at = @At("RETURN")
    )
    private void addVersePlusNaturalSpawnEffects(
            ServerLevelAccessor levelAccessor,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            SpawnGroupData spawnGroupData,
            CallbackInfoReturnable<SpawnGroupData> cir
    ) {
        if (spawnReason != EntitySpawnReason.NATURAL) {
            return;
        }

        ServerLevel level = levelAccessor.getLevel();
        if ((Object) this instanceof Spider && level.isDarkOutside()
                && getRandom().nextFloat() < WEAVING_SPIDER_NATURAL_SPAWN_CHANCE) {
            addEffect(new MobEffectInstance(MobEffects.WEAVING, PERMANENT_SPAWN_EFFECT_DURATION));
        }

        BlockPos pos = blockPosition();
        if (level.getMoonBrightness(pos) >= 1.0F
                && (level.getBiome(pos).is(Biomes.SWAMP) || level.getBiome(pos).is(Biomes.MANGROVE_SWAMP))) {
            addEffect(new MobEffectInstance(MobEffects.OOZING, PERMANENT_SPAWN_EFFECT_DURATION));
        }
    }
}
