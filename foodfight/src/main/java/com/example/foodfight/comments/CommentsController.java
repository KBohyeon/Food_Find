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
import org.springframework.data.domain.Page;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.example.foodfight.upload.Upload;
import com.example.foodfight.upload.UploadService;


@RequestMapping("/comments")
@RequiredArgsConstructor
@Controller
public class CommentsController {
	
	private final UploadService uploadService;
	private final CommentsService commentsService;
	
	@PostMapping("/create/{id}")
	public String createComments(Model model, @PathVariable("id") Integer id, @RequestParam(value="content") String content) {
		//value="content" upload_detail.html에서 답변으로 입력한 내용(content)을 얻기 위해
		Upload upload = this.uploadService.getUpload(id);
		//CommentsService의 create호출
		this.commentsService.create(upload, content);
		return String.format("redirect:/upload/detail/%s", id);
	}
}
