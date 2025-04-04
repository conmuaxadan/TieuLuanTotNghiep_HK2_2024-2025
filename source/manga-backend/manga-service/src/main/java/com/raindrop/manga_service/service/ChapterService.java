package com.raindrop.manga_service.service;

import com.raindrop.manga_service.dto.request.ChapterRequest;
import com.raindrop.manga_service.dto.response.ApiResponse;
import com.raindrop.manga_service.dto.response.ChapterResponse;
import com.raindrop.manga_service.dto.response.FileDataResponse;
import com.raindrop.manga_service.dto.response.PageResponse;
import com.raindrop.manga_service.entity.Chapter;
import com.raindrop.manga_service.entity.Manga;
import com.raindrop.manga_service.entity.Page;
import com.raindrop.manga_service.mapper.ChapterMapper;
import com.raindrop.manga_service.repository.ChapterRepository;
import com.raindrop.manga_service.repository.MangaRepository;
import com.raindrop.manga_service.repository.PageRepository;
import com.raindrop.manga_service.repository.httpclient.UploadClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ChapterService {
    ChapterRepository chapterRepository;
    ChapterMapper chapterMapper;
    UploadClient uploadClient;
    MangaRepository mangaRepository;
    PageRepository pageRepository;

    public ChapterResponse createChapter(ChapterRequest request) {
        if (request.getPages() == null || request.getPages().isEmpty()) {
            throw new IllegalArgumentException("Chapter must have at least one page");
        }

        Manga manga = mangaRepository.findById(request.getMangaId())
                .orElseThrow(() -> new RuntimeException("Manga not found"));

        // **Tạo và lưu các Page, sau đó gán vào Chapter**
        Set<Page> pages = new HashSet<>();
        for (int i = 0; i < request.getPages().size(); i++) {
            MultipartFile file = request.getPages().get(i);
            try {
                ApiResponse<FileDataResponse> apiResponse = uploadClient.uploadMedia(file);
                Page page = Page.builder()
                        .index(i)
                        .pageUrl(apiResponse.getResult().getUrl())
                        .build();
                page = pageRepository.save(page); // Lưu Page để có ID
                pages.add(page); // Thêm vào tập hợp pages
            } catch (Exception e) {
                log.error("Error uploading file [{}]: {}", i, e.getMessage());
                throw new RuntimeException("Failed to upload all images");
            }
        }

        // **Tạo Chapter trước**
        Chapter chapter = Chapter.builder()
                .chapterNumber(request.getChapterNumber())
                .title(request.getTitle())
                .manga(manga)
                .pages(pages)
                .build();
        chapter = chapterRepository.save(chapter); // Lưu để lấy ID

        // **Tạo response**
        return ChapterResponse.builder()
                .title(chapter.getTitle())
                .chapterNumber(chapter.getChapterNumber())
                .mangaId(chapter.getManga().getId())
                .pages(new ArrayList<>(chapter.getPages()).stream()
                        .sorted(Comparator.comparingInt(Page::getIndex))
                        .map(page -> PageResponse.builder()
                                .index(page.getIndex())
                                .pageUrl(page.getPageUrl())
                                .build())
                        .toList())
                .updatedAt(chapter.getUpdatedAt())
                .build();
    }

    public ChapterResponse getChapterById(String id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        return chapterMapper.toChapterResponse(chapter);
    }



}
