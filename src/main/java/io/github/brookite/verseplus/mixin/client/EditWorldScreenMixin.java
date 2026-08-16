package io.github.brookite.verseplus.mixin.client;

import io.github.brookite.verseplus.mixin.accessor.LevelStorageAccessInvoker;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditWorldScreen.class)
public class EditWorldScreenMixin {
    private static final Component ALLOW_COMMANDS = Component.translatable("selectWorld.allowCommands");
    private static final Component ALLOW_COMMANDS_INFO = Component.translatable("selectWorld.allowCommands.info");

    @Shadow @Final private LinearLayout layout;
    @Shadow @Final private LevelStorageSource.LevelStorageAccess levelAccess;

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/EditBox;setValue(Ljava/lang/String;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void verseplus$addAllowCommandsToggle(CallbackInfo ci) {
        this.layout.addChild(
                CycleButton.onOffBuilder(this.verseplus$allowsCommands())
                        .withTooltip(state -> Tooltip.create(ALLOW_COMMANDS_INFO))
                        .create(0, 0, 200, 20, ALLOW_COMMANDS, (button, allowCommands) -> this.verseplus$setAllowCommands(allowCommands))
        );
    }

    private boolean verseplus$allowsCommands() {
        try {
            return this.levelAccess.getUnfixedDataTag(false).get("allowCommands").asBoolean(false);
        } catch (IOException e) {
            SystemToast.onWorldAccessFailure(Minecraft.getInstance(), this.levelAccess.getLevelId());
            return false;
        }
    }

    private void verseplus$setAllowCommands(final boolean allowCommands) {
        try {
            ((LevelStorageAccessInvoker) this.levelAccess).verseplus$modifyLevelDataWithoutDatafix(
                    data -> data.putBoolean("allowCommands", allowCommands)
            );
        } catch (IOException e) {
            SystemToast.onWorldAccessFailure(Minecraft.getInstance(), this.levelAccess.getLevelId());
        }
    }
}
