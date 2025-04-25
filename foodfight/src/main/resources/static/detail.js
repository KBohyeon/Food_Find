
    
    // 리뷰 작성 모달
    const writeReviewBtn = document.querySelector('.write-review-btn');
    const reviewModal = document.getElementById('reviewModal');
    const closeModal = document.querySelector('.close-modal');
    const cancelBtn = document.querySelector('.cancel-btn');
        
    if (writeReviewBtn && reviewModal) {
        writeReviewBtn.addEventListener('click', function() {
            reviewModal.classList.add('active');
        });
        
        if (closeModal) {
            closeModal.addEventListener('click', function() {
                reviewModal.classList.remove('active');
            });
        }
        
        if (cancelBtn) {
            cancelBtn.addEventListener('click', function() {
                reviewModal.classList.remove('active');
            });
        }
        
        // 모달 외부 클릭 시 닫기
        reviewModal.addEventListener('click', function(e) {
            if (e.target === reviewModal) {
                reviewModal.classList.remove('active');
            }
        });
    }
    
    // 별점 선택 기능
    const ratingStars = document.querySelectorAll('.rating-input i');
    const ratingInput = document.getElementById('ratingInput');
    
    if (ratingStars.length > 0 && ratingInput) {
        ratingStars.forEach(star => {
            star.addEventListener('click', function() {
                const rating = this.getAttribute('data-rating');
                ratingInput.value = rating;
                
                // 별점 UI 업데이트
                ratingStars.forEach(s => {
                    const sRating = s.getAttribute('data-rating');
                    if (sRating <= rating) {
                        s.classList.remove('far');
                        s.classList.add('fas');
                    } else {
                        s.classList.remove('fas');
                        s.classList.add('far');
                    }
                });
            });
            
            // 호버 효과
            star.addEventListener('mouseenter', function() {
                const rating = this.getAttribute('data-rating');
                
                ratingStars.forEach(s => {
                    const sRating = s.getAttribute('data-rating');
                    if (sRating <= rating) {
                        s.classList.add('hover');
                    }
                });
            });
            
            star.addEventListener('mouseleave', function() {
                ratingStars.forEach(s => {
                    s.classList.remove('hover');
                });
            });
        });
    }
    
    // 리뷰 쓸때 이미지 미리보기 기능
    const reviewImages = document.getElementById('reviewImages');
    const imagePreview = document.getElementById('imagePreview');
    
    if (reviewImages && imagePreview) {
        reviewImages.addEventListener('change', function() {
            imagePreview.innerHTML = '';
            
            if (this.files) {
                for (let i = 0; i < this.files.length; i++) {
                    const file = this.files[i];
                    if (!file.type.match('image.*')) continue;
                    
                    const reader = new FileReader();
                    
                    reader.onload = function(e) {
                        const preview = document.createElement('div');
                        preview.className = 'image-preview';
                        preview.innerHTML = `
                            <img src="${e.target.result}" alt="미리보기">
                            <span class="remove-image">&times;</span>
                        `;
                        imagePreview.appendChild(preview);
                        
                        // 이미지 삭제 기능
                        const removeBtn = preview.querySelector('.remove-image');
                        removeBtn.addEventListener('click', function() {
                            preview.remove();
                        });
                    };
                    
                    reader.readAsDataURL(file);
                }
            }
        });
    }
    
    // CSRF 토큰 가져오기
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    // 리뷰 수정 모달
    const editReviewModal = document.getElementById('editReviewModal');
    const editReviewForm = document.getElementById('editReviewForm');
    const editCommentId = document.getElementById('editCommentId');
    const editReviewContent = document.getElementById('editReviewContent');
    const editRatingInput = document.getElementById('editRatingInput');
    const existingImages = document.getElementById('existingImages');
    const editImagePreview = document.getElementById('editImagePreview');

    // 수정 모달 닫기 버튼
    const editCloseModal = editReviewModal.querySelector('.close-modal');
    const editCancelBtn = editReviewModal.querySelector('.cancel-btn');

    if (editCloseModal) {
        editCloseModal.addEventListener('click', function() {
            editReviewModal.classList.remove('active');
        });
    }

    if (editCancelBtn) {
        editCancelBtn.addEventListener('click', function() {
            editReviewModal.classList.remove('active');
        });
    }

    // 모달 외부 클릭 시 닫기
    editReviewModal.addEventListener('click', function(e) {
        if (e.target === editReviewModal) {
            editReviewModal.classList.remove('active');
        }
    });

    // 수정 버튼 클릭 이벤트
    const editBtns = document.querySelectorAll('.edit-btn');
    if (editBtns.length > 0) {
        editBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const commentId = this.getAttribute('data-id');
                loadReviewForEdit(commentId);
            });
        });
    }

    // 리뷰 수정을 위한 데이터 로드
    function loadReviewForEdit(commentId) {
        fetch(`/api/comments/${commentId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('리뷰 정보를 불러오는데 실패했습니다.');
                }
                return response.json();
            })
            .then(data => {
                // 폼 데이터 설정
                editCommentId.value = data.id;
                editReviewContent.value = data.content;
                editRatingInput.value = data.rating || 0;
                
                // 별점 UI 업데이트
                const ratingStars = editReviewModal.querySelectorAll('.rating-input i');
                ratingStars.forEach(star => {
                    const starRating = star.getAttribute('data-rating');
                    if (starRating <= data.rating) {
                        star.classList.remove('far');
                        star.classList.add('fas');
                    } else {
                        star.classList.remove('fas');
                        star.classList.add('far');
                    }
                });
                
                // 기존 이미지 표시
                existingImages.innerHTML = '';
                if (data.images && data.images.length > 0) {
                    data.images.forEach(image => {
                        // URL이 /uploads로 시작하는지 확인
                        const imageUrl = image.url.startsWith('/uploads') ? image.url : `/uploads${image.url}`;
                        const imageDiv = document.createElement('div');
                        imageDiv.className = 'existing-image';
                        imageDiv.innerHTML = `
                            <img src="${imageUrl}" alt="리뷰 이미지">
                            <span class="remove-existing-image" data-image-id="${image.id}">&times;</span>
                            <input type="hidden" name="existingImageIds" value="${image.id}">
                        `;
                        existingImages.appendChild(imageDiv);
                    });
                    
                    // 기존 이미지 삭제 이벤트 추가
                    const removeButtons = existingImages.querySelectorAll('.remove-existing-image');
                    removeButtons.forEach(button => {
                        button.addEventListener('click', function() {
                            const imageId = this.getAttribute('data-image-id');
                            const imageDiv = this.parentElement;
                            
                            // 이미지 삭제 표시 (실제 삭제는 폼 제출 시 처리)
                            imageDiv.style.opacity = '0.3';
                            const input = imageDiv.querySelector('input[name="existingImageIds"]');
                            input.name = 'deleteImageIds';
                            
                            // 삭제 취소 버튼 추가
                            const undoButton = document.createElement('span');
                            undoButton.className = 'undo-delete';
                            undoButton.textContent = '취소';
                            undoButton.style.position = 'absolute';
                            undoButton.style.top = '50%';
                            undoButton.style.left = '50%';
                            undoButton.style.transform = 'translate(-50%, -50%)';
                            undoButton.style.backgroundColor = 'rgba(0, 0, 0, 0.7)';
                            undoButton.style.color = 'white';
                            undoButton.style.padding = '4px 8px';
                            undoButton.style.borderRadius = '4px';
                            undoButton.style.cursor = 'pointer';
                            
                            undoButton.addEventListener('click', function(e) {
                                e.stopPropagation();
                                imageDiv.style.opacity = '1';
                                input.name = 'existingImageIds';
                                this.remove();
                            });
                            
                            imageDiv.appendChild(undoButton);
                        });
                    });
                }
                
                // 모달 표시
                editReviewModal.classList.add('active');
                
                // 폼 액션 설정
                editReviewForm.action = `/api/comments/update/${commentId}`;
            })
            .catch(error => {
                console.error('리뷰 로드 중 오류 발생:', error);
                alert(error.message);
            });
    }

    // 이미지 미리보기 기능 (수정 모달)
    const editReviewImages = document.getElementById('editReviewImages');

    if (editReviewImages && editImagePreview) {
        editReviewImages.addEventListener('change', function() {
            editImagePreview.innerHTML = '';
            
            if (this.files) {
                for (let i = 0; i < this.files.length; i++) {
                    const file = this.files[i];
                    if (!file.type.match('image.*')) continue;
                    
                    const reader = new FileReader();
                    
                    reader.onload = function(e) {
                        const preview = document.createElement('div');
                        preview.className = 'image-preview';
                        preview.innerHTML = `
                            <img src="${e.target.result}" alt="미리보기">
                            <span class="remove-image">&times;</span>
                        `;
                        editImagePreview.appendChild(preview);
                        
                        // 이미지 삭제 기능
                        const removeBtn = preview.querySelector('.remove-image');
                        removeBtn.addEventListener('click', function() {
                            preview.remove();
                        });
                    };
                    
                    reader.readAsDataURL(file);
                }
            }
        });
    }

    // 별점 선택 기능 (수정 모달)
    const editRatingStars = editReviewModal.querySelectorAll('.rating-input i');

    if (editRatingStars.length > 0 && editRatingInput) {
        editRatingStars.forEach(star => {
            star.addEventListener('click', function() {
                const rating = this.getAttribute('data-rating');
                editRatingInput.value = rating;
                
                // 별점 UI 업데이트
                editRatingStars.forEach(s => {
                    const sRating = s.getAttribute('data-rating');
                    if (sRating <= rating) {
                        s.classList.remove('far');
                        s.classList.add('fas');
                    } else {
                        s.classList.remove('fas');
                        s.classList.add('far');
                    }
                });
            });
            
            // 호버 효과
            star.addEventListener('mouseenter', function() {
                const rating = this.getAttribute('data-rating');
                
                editRatingStars.forEach(s => {
                    const sRating = s.getAttribute('data-rating');
                    if (sRating <= rating) {
                        s.classList.add('hover');
                    }
                });
            });
            
            star.addEventListener('mouseleave', function() {
                editRatingStars.forEach(s => {
                    s.classList.remove('hover');
                });
            });
        });
    }

    // 리뷰 수정 폼 제출
    if (editReviewForm) {
        editReviewForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const rating = editRatingInput.value;
            const content = editReviewContent.value;
            
            if (rating === '0') {
                alert('별점을 선택해주세요.');
                return;
            }
            
            if (!content.trim()) {
                alert('리뷰 내용을 입력해주세요.');
                return;
            }
            
            // FormData 객체 생성
            const formData = new FormData(this);
            
            // 서버로 리뷰 데이터 전송
            fetch(this.action, {
                method: 'POST',
                headers: {
                    [header]: token
                },
                body: formData
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('리뷰 수정에 실패했습니다.');
                }
                return response.json();
            })
            .then(data => {
                alert('리뷰가 수정되었습니다.');
                
                // 페이지 새로고침
                window.location.reload();
            })
            .catch(error => {
                console.error('리뷰 수정 중 오류 발생:', error);
                alert('리뷰 수정 중 오류가 발생했습니다: ' + error.message);
            });
        });
    }
    
    // 주소 복사 기능
    const copyAddressBtn = document.querySelector('.copy-address');
    
    if (copyAddressBtn) {
        copyAddressBtn.addEventListener('click', function() {
            const address = document.querySelector('.address').textContent;
            
            // 클립보드에 복사
            navigator.clipboard.writeText(address.trim())
                .then(() => {
                    alert('주소가 클립보드에 복사되었습니다.');
                })
                .catch(err => {
                    console.error('주소 복사 실패:', err);
                });
        });
    }
    
    // 카카오맵 API 초기화
    function initMap() {
        if (typeof kakao !== 'undefined' && document.getElementById('map')) {
            const mapContainer = document.getElementById('map');
            const address = document.querySelector('.address').textContent.trim();
            
            // 주소-좌표 변환 객체 생성
            const geocoder = new kakao.maps.services.Geocoder();
            
            // 주소로 좌표 검색
            geocoder.addressSearch(address, function(result, status) {
                // 정상적으로 검색이 완료됐으면
                if (status === kakao.maps.services.Status.OK) {
                    const coords = new kakao.maps.LatLng(result[0].y, result[0].x);
                    
                    const mapOption = {
                        center: coords,
                        level: 3
                    };
                    
                    // 지도 생성
                    const map = new kakao.maps.Map(mapContainer, mapOption);
                    
                    // 마커 생성
                    const marker = new kakao.maps.Marker({
                        position: coords
                    });
                    
                    marker.setMap(map);
                    
                    // 인포윈도우 생성
                    const infowindow = new kakao.maps.InfoWindow({
                        content: '<div style="width:150px;text-align:center;padding:6px 0;">' + 
                                document.querySelector('.restaurant-title').textContent + '</div>'
                    });
                    
                    infowindow.open(map, marker);
                    
                    // 지도 중심을 마커 위치로 이동
                    map.setCenter(coords);
                } else {
                    // 주소 검색 실패 시 서울 시청을 기본 위치로 설정
                    const defaultCoords = new kakao.maps.LatLng(37.566826, 126.9786567);
                    
                    const mapOption = {
                        center: defaultCoords,
                        level: 3
                    };
                    
                    const map = new kakao.maps.Map(mapContainer, mapOption);
                    
                    const marker = new kakao.maps.Marker({
                        position: defaultCoords
                    });
                    
                    marker.setMap(map);
                }
            });
        }
    }
    
    // 카카오맵 API가 로드되었을 때 지도 초기화
    if (typeof kakao !== 'undefined') {
        initMap();
    } else {
        // kakao 객체가 로드되지 않았을 때 이벤트 리스너 추가
        window.addEventListener('load', function() {
            if (typeof kakao !== 'undefined') {
                initMap();
            }
        });
    }
  
    
    // 페이지 로드 시 첫 페이지 리뷰 로드
    if (reviewList) {
        loadMoreReviews();
    }

// 리뷰 추천 함수 (전역 함수로 정의)
function voteUpload(button) {
    const uri = button.dataset.url;  
    if (confirm("정말로 추천하시겠습니까?")) {
        fetch(uri, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
                    alert('로그인이 필요한 기능입니다.');
                    window.location.href = '/user/login';
                    return;
                }
                throw new Error('추천 처리 중 오류가 발생했습니다.');
            }
            return response.json();
        })
        .then(data => {
            // 추천 수 업데이트
            const voteCount = button.querySelector('.badge');
            if (voteCount) {
                voteCount.textContent = data.voteCount;
            }
            alert('추천이 완료되었습니다.');
        })
        .catch(error => {
            console.error('추천 중 오류 발생:', error);
            alert(error.message);
        });
    }
}

// 리뷰 폼 제출 전 유효성 검사 강화
const reviewForm = document.getElementById('reviewForm');

if (reviewForm) {
    reviewForm.addEventListener('submit', function(e) {
        // 기본 제출 동작 방지
        e.preventDefault();
        
        const rating = document.getElementById('ratingInput').value;
        const content = document.getElementById('reviewContent').value;
        
        // 유효성 검사
        if (rating === '0') {
            alert('별점을 선택해주세요.');
            return;
        }
        
        if (!content.trim()) {
            alert('리뷰 내용을 입력해주세요.');
            return;
        }
        
        // 유효성 검사 통과 시 폼 제출
        this.submit();
    });
}

var kakao;