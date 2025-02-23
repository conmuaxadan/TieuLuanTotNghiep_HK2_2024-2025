package com.raindrop.manga_service.dto.response;

import com.raindrop.manga_service.entity.Chapter;
import com.raindrop.manga_service.entity.Genre;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MangaResponse {
    String id;
    String title;
    String author;
    String description;
    Set<Genre> genres;
    Set<Chapter> chapters;
}
