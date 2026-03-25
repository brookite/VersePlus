package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.VersePlus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class PlayerDropMixin extends LivingEntity {

    protected PlayerDropMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    /**
     * @author brookit
     * @reason increase player drop lifetime
     */
    @Override
    public ItemEntity drop(ItemStack stack, boolean dropAtSelf, boolean retainOwnership) {
        ItemEntity result = super.drop(stack, dropAtSelf, retainOwnership);
        if (result == null) {
            return null;
        }
        if (BuiltInRegistries.ITEM.getKey(result.getItem().getItem()).equals(Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "charged_rare_ender_pearl_item"))) {
            result.setPickUpDelay(-1);
        } else {
            result.setExtendedLifetime();
        }
        return result;
    }

}
