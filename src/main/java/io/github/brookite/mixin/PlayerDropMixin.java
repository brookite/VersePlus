package io.github.brookite.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
        if (result != null) {
            result.setCovetedItem();
        }
        return result;
    }

}
