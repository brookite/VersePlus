package io.github.brookite.verseplus.features.loyalpets.ai;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;

public class FullMoonPlayerTargetGoal extends NearestAttackableTargetGoal<Player> {
    private final Wolf wolf;

    public FullMoonPlayerTargetGoal(Wolf wolf) {
        super(wolf, Player.class, true);
        this.wolf = wolf;
    }

    @Override
    public boolean canUse() {
        return isFullMoonHunter() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return isFullMoonHunter() && super.canContinueToUse();
    }

    private boolean isFullMoonHunter() {
        return !wolf.isTame()
                && wolf.level() instanceof ServerLevel level
                && level.isDarkOutside()
                && level.getMoonBrightness(wolf.blockPosition()) >= 1.0F;
    }
}
