package io.github.brookite.verseplus.features.containerlocks;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class ContainerLockInteraction {
    private record LockTarget(BaseContainerBlockEntity blockEntity, LockData data) {
    }

    private ContainerLockInteraction() {
    }

    static void initialize() {
        UseBlockCallback.EVENT.register(ContainerLockInteraction::interact);
        AttackBlockCallback.EVENT.register(ContainerLockInteraction::attack);
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (state.getBlock() instanceof ButtonBlock && level instanceof ServerLevel serverLevel) {
                ButtonLockSavedData.get(serverLevel).removeLock(pos);
            }
        });
    }

    private static InteractionResult attack(
            Player player,
            Level level,
            net.minecraft.world.InteractionHand hand,
            BlockPos pos,
            Direction direction
    ) {
        if (level.isClientSide() || player.isSpectator() || !player.getItemInHand(hand).is(ItemTags.AXES)) {
            return InteractionResult.PASS;
        }

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ChestBlock) && !(state.getBlock() instanceof ShulkerBoxBlock)) {
            return InteractionResult.PASS;
        }

        LockTarget target = findLock(level, pos, state);
        if (target == null || target.data().closed()) {
            return InteractionResult.PASS;
        }

        removeLockData(target.blockEntity());
        ItemStack lockStack = ContainerLocks.createLock(target.data());
        if (!player.getInventory().add(lockStack) && !lockStack.isEmpty()) {
            player.drop(lockStack, false);
        }

        level.playSound(
                null,
                target.blockEntity().getBlockPos(),
                SoundEvents.CHAIN_BREAK,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
        player.sendOverlayMessage(Component.translatable("message.verseplus.lock.removed"));
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult interact(Player player, Level level, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (player.isSpectator()) {
            return InteractionResult.PASS;
        }

        ItemStack heldStack = player.getItemInHand(hand);
        if (state.getBlock() instanceof ButtonBlock) {
            return interactWithButton(player, level, pos, heldStack);
        }

        if (!isSupportedContainer(state)) {
            return InteractionResult.PASS;
        }
        if (!(heldStack.getItem() instanceof LockItem) && !(heldStack.getItem() instanceof KeyItem)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (heldStack.getItem() instanceof LockItem lockItem) {
            return installLock(player, level, pos, state, heldStack, lockItem);
        }
        return toggleLock(player, level, pos, state, heldStack);
    }

    private static InteractionResult interactWithButton(
            Player player,
            Level level,
            BlockPos pos,
            ItemStack heldStack
    ) {
        if (level.isClientSide()) {
            return heldStack.getItem() instanceof LockItem || heldStack.getItem() instanceof KeyItem
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }

        ButtonLockSavedData savedData = ButtonLockSavedData.get((ServerLevel) level);
        LockData installed = savedData.getLock(pos);
        if (installed == null) {
            if (heldStack.getItem() instanceof LockItem lockItem) {
                return installButtonLock(player, level, pos, heldStack, lockItem, savedData);
            }
            return InteractionResult.PASS;
        }

        if (heldStack.getItem() instanceof KeyItem) {
            if (!ContainerLocks.matches(heldStack, installed)) {
                player.sendOverlayMessage(Component.translatable("message.verseplus.lock.button_requires_key"));
                return InteractionResult.SUCCESS;
            }

            LockData toggled = installed.toggled();
            savedData.setLock(pos, toggled);
            level.playSound(
                    null,
                    pos,
                    toggled.closed() ? SoundEvents.IRON_TRAPDOOR_CLOSE : SoundEvents.IRON_TRAPDOOR_OPEN,
                    SoundSource.BLOCKS,
                    0.8F,
                    1.0F
            );
            player.sendOverlayMessage(Component.translatable(
                    toggled.closed() ? "message.verseplus.lock.closed" : "message.verseplus.lock.opened"
            ));
            return InteractionResult.SUCCESS;
        }

        if (installed.closed()) {
            player.sendOverlayMessage(Component.translatable("message.verseplus.lock.button_requires_key"));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult installButtonLock(
            Player player,
            Level level,
            BlockPos pos,
            ItemStack lockStack,
            LockItem lockItem,
            ButtonLockSavedData savedData
    ) {
        LockData data = lockStack.get(ContainerLockComponents.LOCK_DATA);
        if (data == null || data.material() != lockItem.material()) {
            player.sendOverlayMessage(Component.translatable("message.verseplus.lock.invalid"));
            return InteractionResult.SUCCESS;
        }

        savedData.setLock(pos, data.installed(false));
        lockStack.consume(1, player);
        if (data.containsKey()) {
            ItemStack key = ContainerLocks.createKey(data);
            if (!player.getInventory().add(key) && !key.isEmpty()) {
                player.drop(key, false);
            }
        }

        level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.sendOverlayMessage(Component.translatable("message.verseplus.lock.button_installed"));
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult installLock(
            Player player,
            Level level,
            BlockPos pos,
            BlockState state,
            ItemStack lockStack,
            LockItem lockItem
    ) {
        LockData data = lockStack.get(ContainerLockComponents.LOCK_DATA);
        if (data == null || data.material() != lockItem.material()) {
            player.sendOverlayMessage(Component.translatable("message.verseplus.lock.invalid"));
            return InteractionResult.SUCCESS;
        }

        if (findLock(level, pos, state) != null) {
            player.sendOverlayMessage(Component.translatable("message.verseplus.lock.already_installed"));
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BaseContainerBlockEntity container)) {
            return InteractionResult.PASS;
        }

        setLockData(container, data.installed(false));
        lockStack.consume(1, player);
        if (data.containsKey()) {
            ItemStack key = ContainerLocks.createKey(data);
            if (!player.getInventory().add(key) && !key.isEmpty()) {
                player.drop(key, false);
            }
        }

        level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.sendOverlayMessage(Component.translatable("message.verseplus.lock.installed"));
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult toggleLock(Player player, Level level, BlockPos pos, BlockState state, ItemStack keyStack) {
        LockTarget target = findLock(level, pos, state);
        if (target == null) {
            return InteractionResult.PASS;
        }

        if (!ContainerLocks.matches(keyStack, target.data())) {
            BaseContainerBlockEntity.sendChestLockedNotifications(
                    Vec3.atCenterOf(target.blockEntity().getBlockPos()),
                    player,
                    target.blockEntity().getDisplayName()
            );
            return InteractionResult.SUCCESS;
        }

        LockData toggled = target.data().toggled();
        setLockData(target.blockEntity(), toggled);
        level.playSound(
                null,
                target.blockEntity().getBlockPos(),
                toggled.closed() ? SoundEvents.IRON_TRAPDOOR_CLOSE : SoundEvents.IRON_TRAPDOOR_OPEN,
                SoundSource.BLOCKS,
                0.8F,
                1.0F
        );
        player.sendOverlayMessage(Component.translatable(
                toggled.closed() ? "message.verseplus.lock.closed" : "message.verseplus.lock.opened"
        ));
        return InteractionResult.SUCCESS;
    }

    private static LockTarget findLock(Level level, BlockPos pos, BlockState state) {
        LockTarget local = lockAt(level, pos);
        if (local != null) {
            return local;
        }

        if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            return lockAt(level, ChestBlock.getConnectedBlockPos(pos, state));
        }
        return null;
    }

    private static LockTarget lockAt(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BaseContainerBlockEntity container)) {
            return null;
        }
        LockData data = container.components().get(ContainerLockComponents.LOCK_DATA);
        return data == null ? null : new LockTarget(container, data);
    }

    private static void setLockData(BaseContainerBlockEntity container, LockData data) {
        DataComponentMap components = DataComponentMap.builder()
                .addAll(container.components())
                .set(ContainerLockComponents.LOCK_DATA, data)
                .build();
        container.setComponents(components);
        container.setChanged();
    }

    private static void removeLockData(BaseContainerBlockEntity container) {
        DataComponentMap components = DataComponentMap.builder()
                .addAll(container.components())
                .set(ContainerLockComponents.LOCK_DATA, null)
                .build();
        container.setComponents(components);
        container.setChanged();
    }

    private static boolean isSupportedContainer(BlockState state) {
        return state.getBlock() instanceof ChestBlock
                || state.getBlock() instanceof BarrelBlock
                || state.getBlock() instanceof ShulkerBoxBlock;
    }
}
