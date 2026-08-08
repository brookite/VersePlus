package io.github.brookite.verseplus.features.loyalpets.recall;

import io.github.brookite.verseplus.VersePlus;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.TicketType;

public final class LoyalPetTicketTypes {
    public static final TicketType PET_RECALL = Registry.register(
            BuiltInRegistries.TICKET_TYPE,
            Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "pet_recall"),
            new TicketType(
                    100L,
                    TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE
            )
    );

    private LoyalPetTicketTypes() {
    }

    public static void initialize() {
    }
}
