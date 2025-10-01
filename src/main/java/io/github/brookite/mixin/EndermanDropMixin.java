package io.github.brookite.mixin;

import io.github.brookite.VersePlus;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
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
                    && world.getRandom().nextFloat() < 0.05F
            ) {
                Item i = Registries.ITEM.get(Identifier.of(VersePlus.MOD_ID, "fire_ender_pearl_item"));
                stack = new ItemStack(i, 1);
            }
            this.dropStack(world, stack);
        });
    }
}
