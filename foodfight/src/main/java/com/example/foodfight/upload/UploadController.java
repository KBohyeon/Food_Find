package com.example.foodfight.upload;

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

@RequestMapping("/upload")// 프리픽스
@RequiredArgsConstructor
@Controller
public class UploadController {
	
	private final UploadService uploadService;
	private final CommentsService commentsService;
	//컨트롤러 -> 서비스 -> 리포지터리
	@GetMapping("/list") //프리픽스로 인해 /upload + /list로 됨
	public String list(Model model) {
		List<Upload> uploadList = this.uploadService.getList();
		model.addAttribute("uploadList", uploadList);
		return "index";
	}
	
	//식당 등록 테스트 코드
    @GetMapping("/create")
    public String questionCreate() {
        return "uploadtest";
    }
    //식당 등록 테스트 코드
    @PostMapping("/create")
    public String uploadCreate(@RequestParam(value="subject") String subject, 
    							@RequestParam(value="content") String content,
    							@RequestParam(value="category") String category) {
        this.uploadService.create(subject, content, category);
        return "redirect:/upload/list"; // 질문 저장후 질문목록으로 이동
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
