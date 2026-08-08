package io.github.brookite.verseplus.features.loyalpets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import java.util.Optional;

public record WolfHornMemory(
        int requiredSignals,
        Optional<Identifier> trainingSound,
        int trainingProgress,
        Optional<Identifier> learnedSound
) {
    private static final int MIN_REQUIRED_SIGNALS = 3;
    private static final int MAX_REQUIRED_SIGNALS = 7;

    public static final Codec<WolfHornMemory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(MIN_REQUIRED_SIGNALS, MAX_REQUIRED_SIGNALS)
                    .fieldOf("required_signals").forGetter(WolfHornMemory::requiredSignals),
            Identifier.CODEC.optionalFieldOf("training_sound").forGetter(WolfHornMemory::trainingSound),
            Codec.intRange(0, MAX_REQUIRED_SIGNALS).optionalFieldOf("training_progress", 0)
                    .forGetter(WolfHornMemory::trainingProgress),
            Identifier.CODEC.optionalFieldOf("learned_sound").forGetter(WolfHornMemory::learnedSound)
    ).apply(instance, WolfHornMemory::new));

    public static WolfHornMemory create(RandomSource random) {
        return new WolfHornMemory(
                random.nextIntBetweenInclusive(MIN_REQUIRED_SIGNALS, MAX_REQUIRED_SIGNALS),
                Optional.empty(),
                0,
                Optional.empty()
        );
    }

    public LearningAdvance hear(Identifier sound) {
        if (learnedSound.isPresent()) {
            return new LearningAdvance(this, false);
        }

        int nextProgress = trainingSound.filter(sound::equals).isPresent() ? trainingProgress + 1 : 1;
        if (nextProgress < requiredSignals) {
            return new LearningAdvance(
                    new WolfHornMemory(requiredSignals, Optional.of(sound), nextProgress, Optional.empty()),
                    false
            );
        }

        return new LearningAdvance(
                new WolfHornMemory(requiredSignals, Optional.empty(), 0, Optional.of(sound)),
                true
        );
    }

    public record LearningAdvance(WolfHornMemory memory, boolean completed) {
    }
}
