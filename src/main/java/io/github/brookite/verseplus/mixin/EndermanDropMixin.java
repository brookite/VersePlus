package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.VersePlusChances;
import io.github.brookite.verseplus.registries.RegisterItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(EnderMan.class)
public class EndermanDropMixin extends Monster {
    protected EndermanDropMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public void dropFromLootTable(ServerLevel world, DamageSource damageSource,
                         boolean causedByPlayer, ResourceKey<LootTable> lootTableKey) {
        this.dropFromLootTable(world, damageSource, causedByPlayer, lootTableKey, (stack) -> {
            if (BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(Identifier.withDefaultNamespace("ender_pearl"))
                    && world.dimensionTypeRegistration().is(Identifier.withDefaultNamespace("the_nether"))
                    && world.getRandom().nextDouble() < VersePlusChances.ENDERMAN_FIRE_ENDER_PEARL_LOOT
            ) {
                stack = new ItemStack(RegisterItems.FIRE_ENDER_PEARL_ITEM, 1);
            }
            if (damageSource.getEntity() instanceof LivingEntity livingEntity) {
                var attackerItemStack = livingEntity.getMainHandItem();
                var itemId = BuiltInRegistries.ITEM.getKey(attackerItemStack.getItem());

                var registryManager = world.registryAccess();

                if (itemId.equals(Identifier.withDefaultNamespace("golden_sword"))
                    && EnchantmentHelper.getItemEnchantmentLevel(registryManager.getOrThrow(Enchantments.LOOTING), attackerItemStack) == 0
                        && world.dimensionTypeRegistration().is(Identifier.withDefaultNamespace("overworld"))
                        && EnchantmentHelper.getItemEnchantmentLevel(registryManager.getOrThrow(Enchantments.KNOCKBACK), attackerItemStack) > 0
                        && world.getRandom().nextDouble() < VersePlusChances.ENDERMAN_RARE_ENDER_PEARL_LOOT
                ) {
                    this.spawnAtLocation(world, new ItemStack(RegisterItems.RARE_ENDER_PEARL_ITEM, 1));
                }
            }
            this.spawnAtLocation(world, stack);
        });
    }
}
