package io.github.brookite.verseplus.features.waypoints.mixin.client;

import io.github.brookite.verseplus.features.waypoints.client.WaypointClientSession;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void verseplus$runSessionWaypointCommand(String command, CallbackInfo ci) {
        if (WaypointClientSession.handleUnsupportedServerCommand(command)) {
            ci.cancel();
        }
    }
}
