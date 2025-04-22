package com.example.foodfight.comments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.foodfight.upload.Upload;
import com.example.foodfight.user.SiteUser;
import java.util.Optional;
import com.example.foodfight.DataNotFoundException;
import java.time.LocalDateTime;
import com.example.foodfight.user.SiteUser;

@RequiredArgsConstructor
@Service
public class CommentsService {
	
	private final CommentsRepository commentsRepository;
	
	public void create(Upload upload, String content, SiteUser author) {
		Comments comments = new Comments();
		comments.setContent(content);
		comments.setCreateDate(LocalDateTime.now());
		comments.setUpload(upload);
        comments.setAuthor(author);
		this.commentsRepository.save(comments);
	}
	
    public void vote(Comments comments, SiteUser siteUser) {
    	comments.getVoter().add(siteUser);
        this.commentsRepository.save(comments);
    }

	public Comments getComments(Integer id) {
        Optional<Comments> comments = this.commentsRepository.findById(id);
        if (comments.isPresent()) {
            return comments.get();
        } else {
            throw new DataNotFoundException("comments not found");
        }
	}
}
