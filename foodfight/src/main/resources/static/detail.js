document.addEventListener('DOMContentLoaded', function() {
    // 리뷰 더보기 기능
    const reviewList = document.getElementById('reviewList');
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    let currentPage = 1;
    const itemsPerPage = 7;
    
    if (loadMoreBtn) {
        loadMoreBtn.addEventListener('click', function() {
            currentPage++;
            loadMoreReviews();
        });
    }
    
    function loadMoreReviews() {
        // 실제 구현에서는 서버에서 추가 리뷰를 가져오는 AJAX 요청을 수행합니다
        const uploadId = document.getElementById('uploadId').value;
        fetch(`/api/upload/${uploadId}/comments?page=${currentPage}&size=${itemsPerPage}`)
            .then(response => response.json())
            .then(data => {
                if (data.length > 0) {
                    data.forEach(comment => {
                        addReviewToList(comment);
                    });
                }
                
                // 더 이상 불러올 리뷰가 없으면 더보기 버튼 숨기기
                if (data.length < itemsPerPage) {
                    loadMoreBtn.style.display = 'none';
                }
            })
            .catch(error => {
                console.error('리뷰 로드 중 오류 발생:', error);
            });
    }
    
	function addReviewToList(comment) {
	    const reviewItem = document.createElement('div');
	    reviewItem.className = 'review-item';
	    reviewItem.innerHTML = `
	        <div class="review-header">
	            <div class="reviewer-info">
	                <div class="reviewer-avatar">
	                    <img src="https://source.unsplash.com/50x50/?person" alt="리뷰어 이미지">
	                </div>
	                <div class="reviewer-details">
	                    <div class="reviewer-name">${comment.author ? comment.author.username : '익명'}님</div>
	                    <div class="review-date">${formatDate(comment.createDate)}</div>
	                </div>
	            </div>
	            <div class="review-rating">
	                <span class="stars">⭐</span>
	                <span class="rating-value">${comment.rating || '0.0'}</span>
	            </div>
	        </div>
	        <div class="review-content">
	            <p>${comment.content}</p>
	        </div>
	        <div class="review-images">
	            ${comment.images ? renderImages(comment.images) : ''}
	        </div>
	        <div class="review-actions">
	            <button type="button" class="like-btn" data-url="/comments/vote/${comment.id}"
	                onclick="voteUpload(this)">
	                <i class="far fa-thumbs-up"></i> 도움됨  
	                <span class="badge rounded-pill bg-success">${comment.voter ? comment.voter.length : 0}</span>
	            </button>
	            <button class="report-btn"><i class="far fa-flag"></i> 신고</button>
	        </div>
	    `;
	    reviewList.appendChild(reviewItem);
	}
    
    function formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
    }
    
	
	//이미지 보여주는데 필요한건지 모르겠음 일단 필요없음
    function renderImages(images) {
        if (!images || images.length === 0) return '';
        
        let html = '';
        images.forEach(image => {
            html += `
                <div class="review-image">
                    <img src="${image.url}" alt="리뷰 이미지">
                </div>
            `;
        });
        return html;
    }
	
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
    
    // 리뷰 필터 기능
    const filterBtns = document.querySelectorAll('.filter-btn');
    
    if (filterBtns.length > 0) {
        filterBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                // 현재 활성화된 버튼에서 active 클래스 제거
                document.querySelector('.filter-btn.active').classList.remove('active');
                // 클릭한 버튼에 active 클래스 추가
                this.classList.add('active');
                
                // 필터 타입에 따라 리뷰 정렬
                const filterType = this.textContent;
                const uploadId = document.getElementById('uploadId').value;
                
                let sortParam = '';
                if (filterType === '최신순') {
                    sortParam = 'latest';
                } else if (filterType === '평점 높은순') {
                    sortParam = 'highest';
                } else if (filterType === '평점 낮은순') {
                    sortParam = 'lowest';
                }
                
                // 서버에서 정렬된 리뷰 가져오기
                fetch(`/api/upload/${uploadId}/comments?sort=${sortParam}`)
                    .then(response => response.json())
                    .then(data => {
                        // 기존 리뷰 목록 비우기
                        reviewList.innerHTML = '';
                        
                        // 새로운 정렬 순서로 리뷰 추가
                        if (data.length > 0) {
                            data.forEach(comment => {
                                addReviewToList(comment);
                            });
                        } else {
                            // 리뷰가 없을 경우
                            reviewList.innerHTML = `
                                <div class="no-reviews">
                                    <i class="far fa-comment-dots"></i>
                                    <p>아직 등록된 리뷰가 없습니다.</p>
                                    <p>첫 리뷰를 작성해보세요!</p>
                                </div>
                            `;
                        }
                    })
                    .catch(error => {
                        console.error('리뷰 정렬 중 오류 발생:', error);
                    });
            });
        });
    }
});

// 리뷰 추천 함수 (전역 함수로 정의)
function voteUpload(button) {
    const uri = button.dataset.url;  
    if (confirm("정말로 추천하시겠습니까?22222222222")) {
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
