package se.curanexus.journal.api.dto;

import java.util.List;

public record NoteSummaryResponse(
        int total,
        int signed,
        int unsigned,
        List<String> unsignedNoteTitles
) {}
