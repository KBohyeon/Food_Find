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
	
	 @PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}")
	public String createComments(Model model, @PathVariable("id") Integer id,  @Valid CommentsForm commentsForm, 
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
	
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String commentsVote(Principal principal, @PathVariable("id") Integer id) {
        Comments comments = this.commentsService.getComments(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.commentsService.vote(comments, siteUser);
        return String.format("redirect:/upload/detail/%s", comments.getUpload().getId());
    }
}
