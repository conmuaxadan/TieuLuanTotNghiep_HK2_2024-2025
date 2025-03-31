package com.raindrop.manga_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChapterResponse {
    int chapterNumber;
    String title;
    List<String> pages;
    String mangaId;
    LocalDateTime updatedAt;
}
