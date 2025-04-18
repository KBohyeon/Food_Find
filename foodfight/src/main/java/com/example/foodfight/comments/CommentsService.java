package com.example.foodfight.comments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.foodfight.upload.Upload;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CommentsService {
	
	private final CommentsRepository commentsRepository;
	
	public void create(Upload upload, String content) {
		Comments comments = new Comments();
		comments.setContent(content);
		comments.setCreateDate(LocalDateTime.now());
		comments.setUpload(upload);
		this.commentsRepository.save(comments);
	}
}
