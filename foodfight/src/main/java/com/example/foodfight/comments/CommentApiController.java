package com.example.foodfight.comments;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.upload.UploadService;
import com.example.foodfight.user.SiteUser;
import com.example.foodfight.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentsService commentsService;
    private final UploadService uploadService;
    private final UserService userService;
    private final CommentImageService commentImageService;

//    // 리뷰 조회 API 아래 중복인거 같음
//    @GetMapping("/comments/{id}")
//    public ResponseEntity<Comments> getComment(@PathVariable("id") Long id) {
//        try {
//            Comments comment = commentsService.getComments(id);
//            return ResponseEntity.ok(comment);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }

    // 리뷰 수정 API
    @PostMapping("/comments/update/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Comments> updateComment(
            @PathVariable("id") Long id,
            @RequestParam("content") String content,
            @RequestParam("rating") Double rating,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds) {
        
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            SiteUser user = userService.getUserByUsername(authentication.getName());
            
            // 리뷰 정보 가져오기
            Comments comment = commentsService.getComments(id);
            
            // 작성자 확인
            if (!comment.getAuthor().getUsername().equals(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            // 리뷰 수정
            Comments updatedComment = commentsService.update(comment, content, rating, images, deleteImageIds);
            
            return ResponseEntity.ok(updatedComment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // 리뷰 상세 정보 조회 API
    @GetMapping("/comments/{id}")
    public ResponseEntity<Comments> getComment(@PathVariable("id") Long id) {
        try {
            Comments comment = commentsService.getComments(id);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 정렬 기능이 있는 리뷰 목록 조회 API - 데이터베이스에서 직접 정렬
    @GetMapping("/upload/{uploadId}/comments")
    public ResponseEntity<List<Map<String, Object>>> getCommentsByUpload(
            @PathVariable("uploadId") Long uploadId,
            @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        
        try {
            Upload upload = uploadService.getUpload(uploadId);
            List<Comments> comments;
            
            // 정렬 방식에 따라 다른 메서드 호출
            switch (sort) {
                case "latest":
                    comments = commentsService.getCommentsByUploadLatest(upload);
                    break;
                case "highest":
                    comments = commentsService.getCommentsByUploadHighestRating(upload);
                    break;
                case "lowest":
                    comments = commentsService.getCommentsByUploadLowestRating(upload);
                    break;
                default:
                    comments = commentsService.getCommentsByUploadLatest(upload);
            }
            
            // 페이징 처리
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, comments.size());
            
            List<Comments> pagedComments;
            if (fromIndex < comments.size()) {
                pagedComments = comments.subList(fromIndex, toIndex);
            } else {
                pagedComments = List.of();
            }
            
            // 순환 참조 문제를 방지하기 위해 DTO로 변환
            List<Map<String, Object>> result = new ArrayList<>();
            for (Comments comment : pagedComments) {
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("id", comment.getId());
                commentMap.put("content", comment.getContent());
                commentMap.put("createDate", comment.getCreateDate());
                commentMap.put("rating", comment.getRating());
                
                if (comment.getAuthor() != null) {
                    Map<String, Object> authorMap = new HashMap<>();
                    authorMap.put("id", comment.getAuthor().getId());
                    authorMap.put("username", comment.getAuthor().getUsername());
                    commentMap.put("author", authorMap);
                }
                
                if (comment.getVoter() != null) {
                    commentMap.put("voterCount", comment.getVoter().size());
                }
                
                // 이미지 정보 추가
                if (comment.getImages() != null && !comment.getImages().isEmpty()) {
                    List<Map<String, Object>> imagesList = new ArrayList<>();
                    for (CommentImage image : comment.getImages()) {
                        Map<String, Object> imageMap = new HashMap<>();
                        imageMap.put("id", image.getId());
                        imageMap.put("url", image.getUrl());
                        imagesList.add(imageMap);
                    }
                    commentMap.put("images", imagesList);
                }
                
                result.add(commentMap);
            }
            
            // 총 페이지 수와 현재 페이지 정보 추가
            Map<String, Object> metaData = new HashMap<>();
            metaData.put("totalElements", comments.size());
            metaData.put("totalPages", (int) Math.ceil((double) comments.size() / size));
            metaData.put("currentPage", page);
            metaData.put("hasMore", toIndex < comments.size());
            
            // 메타데이터를 첫 번째 요소로 추가
            if (!result.isEmpty()) {
                result.get(0).put("_meta", metaData);
            } else {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("_meta", metaData);
                result.add(emptyResult);
            }
            
            return ResponseEntity.ok()
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}