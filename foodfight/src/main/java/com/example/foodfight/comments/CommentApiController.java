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
    public ResponseEntity<List<Comments>> getCommentsByUpload(
            @PathVariable("uploadId") Long uploadId,
            @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        
        try {
            // Long을 Integer로 변환 (UploadService의 getUpload 메서드가 Integer를 받는 경우)
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
            
            if (fromIndex < comments.size()) {
                comments = comments.subList(fromIndex, toIndex);
            } else {
                comments = List.of();
            }
            
            // 캐싱 방지 헤더 추가
            return ResponseEntity.ok()
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .body(comments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}