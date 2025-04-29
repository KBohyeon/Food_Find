package com.example.foodfight.upload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable; //id 값을 얻을 때
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.foodfight.comments.CommentImage;
import com.example.foodfight.comments.Comments;
import com.example.foodfight.comments.CommentsForm;
import com.example.foodfight.comments.CommentsService;
import com.example.foodfight.user.SiteUser;
import com.example.foodfight.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RequestMapping("/upload")// 프리픽스
@RequiredArgsConstructor
@Controller
public class UploadController {
	
	private final UploadService uploadService;
	private final CommentsService commentsService;
    private final UserService userService;
	//컨트롤러 -> 서비스 -> 리포지터리
	@GetMapping("/list") //프리픽스로 인해 /upload + /list로 됨
	public String list(@RequestParam(value = "keyword", required = false) String keyword,Model model) {
		List<Upload> uploadList;
		
	    if (keyword != null && !keyword.trim().isEmpty()) {
	        uploadList = this.uploadService.searchUploads(keyword); // 키워드 있으면 검색 결과
	    } else {
	        uploadList = this.uploadService.getList(); // 키워드 없으면 전체 카테고리별 평점 높은순 정렬
	        List<Upload> hansikList = this.uploadService.getCategoryListOrderByRating("한식");
	        List<Upload> jungsikList = this.uploadService.getCategoryListOrderByRating("중식");
	        List<Upload> ilsikList = this.uploadService.getCategoryListOrderByRating("일식");
	        List<Upload> yangsikList = this.uploadService.getCategoryListOrderByRating("양식");

	        model.addAttribute("hansikList", hansikList);
	        model.addAttribute("jungsikList", jungsikList);
	        model.addAttribute("ilsikList", ilsikList);
	        model.addAttribute("yangsikList", yangsikList);
	    }
	    

		model.addAttribute("uploadList", uploadList);//기본 전체 리스트
		model.addAttribute("keyword", keyword);
		return "index";
	}
	
    // 식당 등록 폼 페이지
    @GetMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String createForm() {
        return "upload_form";
    }
    
    // 식당 등록 처리
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String create(@RequestParam("subject") String subject,
                         @RequestParam("content") String content,
                         @RequestParam("category") String category,
                         @RequestParam("menu1") String menu1,
                         @RequestParam("menu1Price") String menu1Price,
                         @RequestParam("menu2") String menu2,
                         @RequestParam("menu2Price") String menu2Price,
                         @RequestParam("menu3") String menu3,
                         @RequestParam("menu3Price") String menu3Price,
                         @RequestParam("openTime") String openTime,
                         @RequestParam("closeTime") String closeTime,
                         @RequestParam("dayOff") String dayOff,
                         @RequestParam("phone") String phone,
                         @RequestParam("tag1") String tag1,
                         @RequestParam("tag2") String tag2,
                         @RequestParam(value = "location", required = false) String location,
                         @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
                         @RequestParam(value = "additionalImages", required = false) List<MultipartFile> additionalImages,
                         @RequestParam(value = "latitude", required = false) Double latitude,
                         @RequestParam(value = "longitude", required = false) Double longitude,
                         Principal principal) {
        
        SiteUser siteUser = userService.getUser(principal.getName());
        
        uploadService.create(subject, content, category, location, menu1, menu1Price, menu2, menu2Price, menu3, menu3Price, 
        					 openTime, closeTime, dayOff, phone, tag1, tag2, mainImage, additionalImages, siteUser, latitude, longitude);
        
        return "redirect:/upload/list";
    }
    
    // 식당 수정 폼 페이지
    @GetMapping("/modify/{id}")
    @PreAuthorize("isAuthenticated()")
    public String modifyForm(@PathVariable("id") Long id, Model model, Principal principal) {
        Upload upload = uploadService.getUpload(id);
        
        // 작성자 확인
        if (!upload.getAuthor().getUsername().equals(principal.getName())) {
            return "redirect:/upload/detail/" + id;
        }
        
        model.addAttribute("upload", upload);
        return "upload_form";
    }
    
    // 식당 수정 처리
    @PostMapping("/modify/{id}")
    @PreAuthorize("isAuthenticated()")
    public String modify(@PathVariable("id") Long id,
                         @RequestParam("subject") String subject,
                         @RequestParam("content") String content,
                         @RequestParam("category") String category,
                         @RequestParam(value = "location", required = false) String location,
                         @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
                         @RequestParam(value = "additionalImages", required = false) List<MultipartFile> additionalImages,
                         @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
                         Principal principal) {
        
        Upload upload = uploadService.getUpload(id);
        
        // 작성자 확인
        if (!upload.getAuthor().getUsername().equals(principal.getName())) {
            return "redirect:/upload/detail/" + id;
        }
        
        uploadService.update(upload, subject, content, category, location, mainImage, additionalImages, deleteImageIds);
        
        return "redirect:/upload/detail/" + id;
    }
	
	//제목 누르면 나오는 질문 각자의 페이지
    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Long id,  @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort, CommentsForm commentsForm) {
        Upload upload = this.uploadService.getUpload(id);
        //최신순으로 정렬되어있음 commentsList
//        List<Comments> commentsList = this.commentsService.getCommentsByUploadLatest(upload);
        
        // 정렬 기준에 따라 댓글 리스트 선택
        List<Comments> commentsList;
        switch (sort) {
            case "ratingDesc":
                commentsList = this.commentsService.getCommentsByUploadHighestRating(upload);
                break;
            case "ratingAsc":
                commentsList = this.commentsService.getCommentsByUploadLowestRating(upload);
                break;
            default: // 최신순
                commentsList = this.commentsService.getCommentsByUploadLatest(upload);
                break;
        }

        
        // 각 댓글에 연결된 이미지 가져오기
        Map<Integer, List<CommentImage>> commentImagesMap = new HashMap<>();
        for (Comments comment : commentsList) {
            List<CommentImage> images = this.commentsService.getCommentImagesByComment(comment);
            commentImagesMap.put(comment.getId(), images);
        }
        
        model.addAttribute("upload", upload);
        model.addAttribute("comment_image", commentsList);
        model.addAttribute("commentImagesMap", commentImagesMap); // 이미지 맵 추가
        model.addAttribute("sort", sort); // 현재 정렬 기준 전달
//        return "upload_detail";  //html
        return "product_detail";
    }

}
