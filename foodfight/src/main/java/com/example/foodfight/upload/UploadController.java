package com.example.foodfight.upload;

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
	
	//제목 누르면 나오는 질문 각자의 페이지
    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, CommentsForm commentsForm) {
        Upload upload = this.uploadService.getUpload(id);
        List<Comments> commentImages = this.commentsService.getCommentsByUpload(upload);
        model.addAttribute("upload", upload);
//        model.addAttribute("comment_image", commentImages);
//        return "upload_detail";  //html
        return "product_detail";
    }

}
