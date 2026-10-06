package com.aayusheklavya.onboarding.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory NAICS classification lookup loaded from a bundled CSV (code,title,sector).
 * Search is a simple ranked match: exact code prefix first, then title keyword hits.
 */
@Service
public class NaicsService {

    private final List<NaicsCode> codes;
    private final Map<String, NaicsCode> byCode;

    public NaicsService() {
        this.codes = load();
        this.byCode = codes.stream().collect(Collectors.toMap(NaicsCode::code, c -> c));
    }

    public Optional<NaicsCode> find(String code) {
        return Optional.ofNullable(byCode.get(code));
    }

    public boolean isValid(String code) {
        return code != null && byCode.containsKey(code);
    }

    public List<NaicsCode> search(String query, int limit) {
        if (query == null || query.isBlank()) return codes.stream().limit(limit).toList();
        String q = query.trim().toLowerCase(Locale.ROOT);
        String[] terms = q.split("\\s+");
        return codes.stream()
                .map(c -> Map.entry(c, score(c, q, terms)))
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    private static int score(NaicsCode c, String q, String[] terms) {
        if (c.code().startsWith(q)) return 100;
        String title = c.title().toLowerCase(Locale.ROOT);
        String sector = c.sector().toLowerCase(Locale.ROOT);
        int s = 0;
        for (String t : terms) {
            if (title.contains(t)) s += 10;
            else if (sector.contains(t)) s += 3;
        }
        return s;
    }

    private static List<NaicsCode> load() {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                new ClassPathResource("naics.csv").getInputStream(), StandardCharsets.UTF_8))) {
            List<NaicsCode> out = new ArrayList<>();
            String line;
            boolean header = true;
            while ((line = r.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.isBlank()) continue;
                String[] p = line.split(",", 3);
                out.add(new NaicsCode(p[0].trim(), p[1].trim(), p[2].trim()));
            }
            return List.copyOf(out);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load naics.csv", e);
        }
    }
}
