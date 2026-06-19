package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.interfaces.RenamablePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerRenameMixin implements RenamablePlayer {
    @Unique
    private static final String VERSEPLUS_PLAYER_NAME_TAG = "VersePlusSavedName";

    @Unique
    private String verseplus$savedName;

    @Override
    public void verseplus$setSavedName(String name) {
        this.verseplus$savedName = name;
    }

    @Override
    public String verseplus$getSavedName() {
        return this.verseplus$savedName;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveVersePlusName(ValueOutput output, CallbackInfo ci) {
        if (this.verseplus$savedName != null) {
            output.putString(VERSEPLUS_PLAYER_NAME_TAG, this.verseplus$savedName);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readVersePlusName(ValueInput input, CallbackInfo ci) {
        this.verseplus$savedName = input.getString(VERSEPLUS_PLAYER_NAME_TAG).orElse(null);
    }

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void getVersePlusName(CallbackInfoReturnable<Component> cir) {
        if (this.verseplus$savedName != null) {
            cir.setReturnValue(Component.literal(this.verseplus$savedName));
        }
    }

    @Inject(method = "getPlainTextName", at = @At("HEAD"), cancellable = true)
    private void getVersePlusPlainTextName(CallbackInfoReturnable<String> cir) {
        if (this.verseplus$savedName != null) {
            cir.setReturnValue(this.verseplus$savedName);
        }
    }
}
