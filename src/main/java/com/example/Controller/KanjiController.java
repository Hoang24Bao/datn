package com.example.Controller;

import com.example.Entity.Kanji;
import com.example.Repository.KanjiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kanji")
public class KanjiController {

    @Autowired
    private KanjiRepository kanjiRepository;

    @GetMapping("/all")
    public ResponseEntity<Page<Kanji>> getAllKanji(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Kanji> kanjiPage = kanjiRepository.findWithFilters(level, search, pageable);
        return ResponseEntity.ok(kanjiPage);
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<Kanji> getKanjiById(@PathVariable Integer id) {
        return kanjiRepository.findById(id)
                .map(kanji -> {
                    kanji.getExamples().size();
                    return ResponseEntity.ok(kanji);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}