package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.enums.SpiderVariant;
import io.github.brookite.verseplus.interfaces.SizeableSpider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.brookite.verseplus.VersePlusChances.*;


@Mixin(SpiderEntity.class)
public class SpiderSizesMixin extends HostileEntity implements SizeableSpider {
    private static final TrackedData<Byte> SPIDER_SIZE_VARIANT = DataTracker.registerData(SpiderEntity.class, TrackedDataHandlerRegistry.BYTE);

    protected SpiderSizesMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        setVariant(SpiderVariant.values()[world.random.nextBetween(2, 3)]);
    }

    @Override
    public void setVariant(SpiderVariant variant) {
        this.dataTracker.set(SPIDER_SIZE_VARIANT, variant.getIndexAsByte());
    }

    @Override
    public SpiderVariant getVariant() {
        return SpiderVariant.values()[this.dataTracker.get(SPIDER_SIZE_VARIANT)];
    }

    @Inject(method = "initDataTracker(Lnet/minecraft/entity/data/DataTracker$Builder;)V", at=@At("TAIL"))
    protected void initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(SPIDER_SIZE_VARIANT, SpiderVariant.LARGE.getIndexAsByte());
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        SpiderEntity spider = (SpiderEntity) (Object) this;

        if (spider.getHealth() - amount <= 0) {
            Difficulty difficulty = world.getDifficulty();

            if (getVariant() == SpiderVariant.LARGE && difficulty == Difficulty.HARD
                    && world.random.nextDouble() < PREGNANT_SPIDER_CHANCE
                    && spider.getClass().equals(SpiderEntity.class) // only for spiders (not cave spiders)
            ) {
                int count = world.getRandom().nextBetween(BABY_SPIDER_MIN_COUNT, BABY_SPIDER_MAX_COUNT);
                for (int i = 0; i < count; i++) {
                    SpiderEntity baby = new SpiderEntity(EntityType.SPIDER, world);
                    SpiderVariant variant = SpiderVariant.values()[world.random.nextBetween(0, 2)];

                    ((SizeableSpider)baby).setVariant(variant);

                    double x = spider.getX() + (world.random.nextDouble() - 0.5) * 3;
                    double z = spider.getZ() + (world.random.nextDouble() - 0.5) * 3;

                    baby.setPosition(x, spider.getY(), z);
                    baby.setAiDisabled(false);
                    if (source.getAttacker() instanceof LivingEntity living
                            && !(living instanceof PlayerEntity p && p.isCreative())) {
                        baby.setTarget(living);
                    }

                    world.spawnEntity(baby);
                }
            }
        }
        return super.damage(world, source, amount);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("type", SpiderVariant.CODEC, this.getVariant());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setVariant(view.read("type", SpiderVariant.CODEC).orElse(SpiderVariant.DEFAULT));
    }
}
