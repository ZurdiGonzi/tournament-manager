package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.ScryfallCard;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ScryfallCardRepository {
    void saveAll(List<ScryfallCard> cards);
    Map<UUID, ScryfallCard> findByIds(List<UUID> ids);
}
