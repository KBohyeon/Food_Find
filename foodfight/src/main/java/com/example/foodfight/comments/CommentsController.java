package com.example.foodfight.comments;

import java.util.List;
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
import com.example.foodfight.upload.Upload;
import com.example.foodfight.upload.UploadService;
import com.example.foodfight.user.SiteUser;
import com.example.foodfight.user.UserService;
import jakarta.validation.Valid;


@RequestMapping("/comments")
@RequiredArgsConstructor
@Controller
public class CommentsController {
	
	private final UploadService uploadService;
	private final CommentsService commentsService;
	private final UserService userService;
	
	//리뷰 생성 초기 때 만든거 없어도 될거 같은데 확인을 안했음 ㅋㅋ
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}")
	public String createComments(Model model, @PathVariable("id") Long id,  @Valid CommentsForm commentsForm, 
			BindingResult bindingResult, Principal principal) {
		Upload upload = this.uploadService.getUpload(id);
		SiteUser siteUser = this.userService.getUser(principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("upload", upload);
            return "product_detail";
        }
		//CommentsService의 create호출
		this.commentsService.create(upload, commentsForm.getContent(), siteUser);
		return String.format("redirect:/upload/detail/%s", id);
	}
	
	// 평점과 작성자 정보를 포함한 리뷰 생성 메서드
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create-with-rating/{id}")
	public String createCommentsWithRating(Model model, 
	                                     @PathVariable("id") Long id,
	                                     @RequestParam(value="content") String content,
	                                     @RequestParam(value="rating") Double rating,
	                                     Principal principal) {
		Upload upload = this.uploadService.getUpload(id);
		SiteUser user = this.userService.getUserByUsername(principal.getName());
		this.commentsService.create(upload, content, user, rating);
		return String.format("redirect:/upload/detail/%s", id);
	}
	
	// 이미지 업로드를 포함한 리뷰 생성 메서드
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create-with-images/{id}")
	public String createCommentsWithImages(Model model, 
	                                     @PathVariable("id") Long id,
	                                     @RequestParam(value="content") String content,
	                                     @RequestParam(value="rating") Double rating,
	                                     @RequestParam(value="images", required=false) List<MultipartFile> images,
	                                     Principal principal) {
	    try {
	        Upload upload = this.uploadService.getUpload(id);
	        SiteUser user = this.userService.getUserByUsername(principal.getName());
	        this.commentsService.create(upload, content, user, rating, images);
	        return String.format("redirect:/upload/detail/%s", id);
	    } catch (Exception e) {
	        // 로그에 오류 기록
	        e.printStackTrace();
	        // 오류 메시지와 함께 리다이렉트
	        return String.format("redirect:/upload/detail/%s?error=true&message=%s", id, e.getMessage());
	    }
	}
	
	//추천
    @PreAuthorize("isAuthenticated()")
    
    @GetMapping("/vote/{id}")
    public String commentsVote(Principal principal, @PathVariable("id") Integer id) {
        Comments comments = this.commentsService.getComments(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.commentsService.vote(comments, siteUser);
        return String.format("redirect:/upload/detail/%s", comments.getUpload().getId());
    }
    
	
	//리뷰 수정
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String commentsModify(Model model, 
            @PathVariable("id") Integer id,
            @RequestParam(value="content") String content,
            @RequestParam(value="rating") Double rating,
            @RequestParam(value="images", required=false) List<MultipartFile> images,
            Principal principal) {
         Comments comments = this.commentsService.getComments(id);
         if (!comments.getAuthor().getUsername().equals(principal.getName())) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
         }
	       return String.format("redirect:/upload/detail/%s", id);
    }
	
	
}
