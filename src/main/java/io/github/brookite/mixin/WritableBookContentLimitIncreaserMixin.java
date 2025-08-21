package io.github.brookite.mixin;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.RawFilteredPair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(WritableBookContentComponent.class)
public class WritableBookContentLimitIncreaserMixin {
    private static int NEW_MAX_PAGES = 256;

    @Shadow
    @Final
    @Mutable
    private static Codec<List<RawFilteredPair<String>>> PAGES_CODEC;

    @Shadow @Final @Mutable
    private static PacketCodec<ByteBuf, WritableBookContentComponent> PACKET_CODEC;

    static {
        Codec<RawFilteredPair<String>> pageCodec = RawFilteredPair.createCodec(Codec.string(0, 1024));
        PAGES_CODEC = pageCodec.sizeLimitedListOf(NEW_MAX_PAGES);
        PACKET_CODEC = RawFilteredPair.createPacketCodec(PacketCodecs.string(1024))
                .collect(PacketCodecs.toList(NEW_MAX_PAGES))
                .xmap(WritableBookContentComponent::new, WritableBookContentComponent::pages);
    }
}