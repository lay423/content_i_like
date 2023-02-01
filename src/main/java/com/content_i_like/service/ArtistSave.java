package com.content_i_like.service;

import com.content_i_like.domain.entity.Artist;
import com.content_i_like.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ArtistSave implements DBSaveOption<Artist> {
    private final ArtistRepository artistRepository;

    @Override
    public Artist saveNewRow(Artist artist) {
        return artistRepository.save(artist);
    }

    @Override
    public List<Artist> saveNewRows(List<Artist> entities) {
        return artistRepository.saveAll(entities);
    }

    @Override
    public List<Artist> fetchEverything() {
        return artistRepository.findAll();
    }
}