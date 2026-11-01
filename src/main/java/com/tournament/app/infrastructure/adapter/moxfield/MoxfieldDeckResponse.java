package com.tournament.app.infrastructure.adapter.moxfield;

import java.util.List;
import java.util.UUID;

public record MoxfieldDeckResponse(
        String moxfieldDeckId,
        String name,
        String format,
        List<MoxfieldCardEntry> commanders,
        List<MoxfieldCardEntry> mainboard,
        List<MoxfieldCardEntry> sideboard
) {
    public record MoxfieldCardEntry(
            UUID scryfallId,
            String cardName,
            int quantity
    ) {
    }
}
