package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.VersePlus;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class PlayerDropMixin extends LivingEntity {

    protected PlayerDropMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * @author brookit
     * @reason increase player drop lifetime
     */
    @Override
    public ItemEntity dropItem(ItemStack stack, boolean dropAtSelf, boolean retainOwnership) {
        ItemEntity result = super.dropItem(stack, dropAtSelf, retainOwnership);
        if (result == null) {
            return null;
        }
        if (Registries.ITEM.getId(result.getStack().getItem()).equals(Identifier.of(VersePlus.MOD_ID, "charged_rare_ender_pearl_item"))) {
            result.setPickupDelay(-1);
        } else {
            result.setCovetedItem();
        }
        return result;
    }

}
