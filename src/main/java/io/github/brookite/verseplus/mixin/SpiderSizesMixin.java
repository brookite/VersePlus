package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.enums.SpiderVariant;
import io.github.brookite.verseplus.interfaces.SizeableSpider;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.brookite.verseplus.VersePlusChances.*;


@Mixin(Spider.class)
public class SpiderSizesMixin extends Monster implements SizeableSpider {
    private static final EntityDataAccessor<Byte> SPIDER_SIZE_VARIANT = SynchedEntityData.defineId(Spider.class, EntityDataSerializers.BYTE);

    protected SpiderSizesMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
        setVariant(SpiderVariant.values()[world.getRandom().nextIntBetweenInclusive(2, 3)]);
    }

    @Override
    public void setVariant(SpiderVariant variant) {
        this.entityData.set(SPIDER_SIZE_VARIANT, variant.getIndexAsByte());
    }

    @Override
    public SpiderVariant getVariant() {
        return SpiderVariant.values()[this.entityData.get(SPIDER_SIZE_VARIANT)];
    }

    @Inject(method = "defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V", at=@At("TAIL"))
    protected void initDataTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(SPIDER_SIZE_VARIANT, SpiderVariant.LARGE.getIndexAsByte());
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        Spider spider = (Spider) (Object) this;

        if (spider.getHealth() - amount <= 0) {
            Difficulty difficulty = world.getDifficulty();

            if (getVariant() == SpiderVariant.LARGE && difficulty == Difficulty.HARD
                    && world.getRandom().nextDouble() < PREGNANT_SPIDER_CHANCE
                    && spider.getClass().equals(Spider.class) // only for spiders (not cave spiders)
            ) {
                int count = world.getRandom().nextIntBetweenInclusive(BABY_SPIDER_MIN_COUNT, BABY_SPIDER_MAX_COUNT);
                for (int i = 0; i < count; i++) {
                    Spider baby = new Spider(EntityTypes.SPIDER, world);
                    SpiderVariant variant = SpiderVariant.values()[world.getRandom().nextIntBetweenInclusive(0, 2)];

                    ((SizeableSpider)baby).setVariant(variant);

                    double x = spider.getX() + (world.getRandom().nextDouble() - 0.5) * 3;
                    double z = spider.getZ() + (world.getRandom().nextDouble() - 0.5) * 3;

                    baby.setPos(x, spider.getY(), z);
                    baby.setNoAi(false);
                    if (source.getEntity() instanceof LivingEntity living
                            && !(living instanceof Player p && p.isCreative())) {
                        baby.setTarget(living);
                    }

                    world.addFreshEntity(baby);
                }
            }
        }
        return super.hurtServer(world, source, amount);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.store("type", SpiderVariant.CODEC, this.getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        this.setVariant(view.read("type", SpiderVariant.CODEC).orElse(SpiderVariant.DEFAULT));
    }
}
