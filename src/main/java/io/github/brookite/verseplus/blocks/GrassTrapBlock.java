package io.github.brookite.verseplus.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GrassTrapBlock extends Block {
    private static final int CHAIN_REACTION_DELAY_TICKS = 2;
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);

    public GrassTrapBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            collapse(serverLevel, pos, state, player);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        collapse(level, pos, state, null);
    }

    private void collapse(ServerLevel level, BlockPos pos, BlockState state, Entity triggeringEntity) {
        if (!state.is(this) || !level.getBlockState(pos).is(this)) {
            return;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            if (level.getBlockState(neighborPos).is(this)
                    && !level.getBlockTicks().hasScheduledTick(neighborPos, this)) {
                level.scheduleTick(neighborPos, this, CHAIN_REACTION_DELAY_TICKS);
            }
        }

        level.destroyBlock(pos, false, triggeringEntity, Block.UPDATE_LIMIT);
    }
}
