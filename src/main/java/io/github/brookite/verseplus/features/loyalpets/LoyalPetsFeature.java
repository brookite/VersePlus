package io.github.brookite.verseplus.features.loyalpets;

import io.github.brookite.verseplus.features.loyalpets.data.LoyalPetAttachments;
import io.github.brookite.verseplus.features.loyalpets.interaction.GoatHornHandler;
import io.github.brookite.verseplus.features.loyalpets.interaction.PetBondingHandler;
import io.github.brookite.verseplus.features.loyalpets.recall.WolfRecallManager;
import io.github.brookite.verseplus.interfaces.ModFeature;

public final class LoyalPetsFeature implements ModFeature {
    private LoyalPetsFeature() {
    }

    public static void initialize() {
        LoyalPetAttachments.initialize();
        PetBondingHandler.initialize();
        WolfRecallManager.initialize();
        GoatHornHandler.initialize();
    }
}
