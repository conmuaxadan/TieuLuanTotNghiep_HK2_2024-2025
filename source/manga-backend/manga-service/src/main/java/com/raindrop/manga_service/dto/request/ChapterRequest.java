package com.raindrop.manga_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChapterRequest {
    int chapterNumber;
    String title;
    String content;
    String mangaId;
}
