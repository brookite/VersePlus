package io.github.brookite.mixin;

import io.github.brookite.VersePlusChances;
import io.github.brookite.registries.RegisterItems;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(EndermanEntity.class)
public class EndermanDropMixin extends HostileEntity {
    protected EndermanDropMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void dropLoot(ServerWorld world, DamageSource damageSource,
                         boolean causedByPlayer, RegistryKey<LootTable> lootTableKey) {
        this.generateLoot(world, damageSource, causedByPlayer, lootTableKey, (stack) -> {
            if (Registries.ITEM.getId(stack.getItem()).equals(Identifier.ofVanilla("ender_pearl"))
                    && world.getDimensionEntry().matchesId(Identifier.ofVanilla("the_nether"))
                    && world.getRandom().nextDouble() < VersePlusChances.ENDERMAN_FIRE_ENDER_PEARL_LOOT
            ) {
                stack = new ItemStack(RegisterItems.FIRE_ENDER_PEARL_ITEM, 1);
            }
            if (damageSource.getAttacker() instanceof LivingEntity livingEntity) {
                var attackerItemStack = livingEntity.getMainHandStack();
                var itemId = Registries.ITEM.getId(attackerItemStack.getItem());

                var registryManager = world.getRegistryManager();

                if (itemId.equals(Identifier.ofVanilla("golden_sword"))
                    && EnchantmentHelper.getLevel(registryManager.getEntryOrThrow(Enchantments.LOOTING), attackerItemStack) == 0
                        && world.getDimensionEntry().matchesId(Identifier.ofVanilla("overworld"))
                        && EnchantmentHelper.getLevel(registryManager.getEntryOrThrow(Enchantments.KNOCKBACK), attackerItemStack) > 0
                        && world.getRandom().nextDouble() < VersePlusChances.ENDERMAN_RARE_ENDER_PEARL_LOOT
                ) {
                    this.dropStack(world, new ItemStack(RegisterItems.RARE_ENDER_PEARL_ITEM, 1));
                }
            }
            this.dropStack(world, stack);
        });
    }
}
