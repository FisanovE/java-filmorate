package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@Slf4j
@RestController
@Component
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public Collection<Mpa> getAllRatingsMpa() {
        log.info("Get all Mpa");
        return mpaService.getAllRatingsMpa();
    }

    @GetMapping("/{id}")
    public Mpa getRatingsMpaById(@PathVariable(required = false) Long id) {
        log.info("Get Mpa {}", id);
        return mpaService.getRatingsMpaById(id);
    }
}
