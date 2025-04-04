package com.raindrop.manga_service.controller;

import com.raindrop.manga_service.dto.request.ChapterRequest;
import com.raindrop.manga_service.dto.request.MangaRequest;
import com.raindrop.manga_service.dto.response.ApiResponse;
import com.raindrop.manga_service.dto.response.ChapterResponse;
import com.raindrop.manga_service.repository.ChapterRepository;
import com.raindrop.manga_service.service.ChapterService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ChapterController {
    ChapterService chapterService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<ChapterResponse> createChapter(
            @RequestParam("chapterNumber") String chapterNumber,
            @RequestParam("mangaId") String mangaId,
            @RequestParam("pages") List<MultipartFile> pages
    ) {
        ChapterRequest request = ChapterRequest.builder()
                .chapterNumber(Integer.parseInt(chapterNumber))
                .title("Chương " + chapterNumber)
                .mangaId(mangaId)
                .pages(pages)
                .build();

        log.info("Create chapter request: {}", request);



        return ApiResponse.<ChapterResponse>builder()
                .message("Chapter created successfully")
                .result(chapterService.createChapter(request))
                .build();
    }
}
