
//햄버거 버튼 활성화
function toggleMenu() {
    const menu = document.getElementById('mobile-menu');
    if (menu.style.display === 'block') {
        menu.style.display = 'none';
    } else {
        menu.style.display = 'block';
    }
}


// 모바일 메뉴 토글
document.addEventListener('DOMContentLoaded', function() {
    const mobileMenuBtn = document.querySelector('.mobile-menu-btn');
    
    if (mobileMenuBtn) {
        mobileMenuBtn.addEventListener('click', function() {

        });
    }
	else {
		        menu.style.display = 'block';
		    }



    
    // 카테고리 버튼 활성화
    const categoryBtns = document.querySelectorAll('.category-btn');
    
    categoryBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            // 현재 활성화된 버튼에서 active 클래스 제거
            document.querySelector('.category-btn.active').classList.remove('active');
            // 클릭한 버튼에 active 클래스 추가
            this.classList.add('active');
            
            // 실제 구현에서는 선택된 카테고리에 따라 식당 목록을 필터링하는 코드를 추가합니다
            const category = this.textContent;
            console.log(`${category} 카테고리가 선택되었습니다.`);
        });
    });
    
    // 검색 기능
    const searchForm = document.querySelector('.search-container');
    const searchInput = document.querySelector('.search-input');
    
    if (searchForm && searchInput) {
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const searchTerm = searchInput.value.trim();
            
            if (searchTerm) {
                // 실제 구현에서는 검색어를 서버로 전송하거나 클라이언트 측에서 필터링하는 코드를 추가합니다
                console.log(`검색어: ${searchTerm}`);
/*                alert(`"${searchTerm}" 검색 결과를 표시합니다.`);*/
            }
        });
        
        // 검색 버튼 클릭 이벤트
        const searchBtn = document.querySelector('.search-btn');
        if (searchBtn) {
            searchBtn.addEventListener('click', function() {
                const event = new Event('submit');
                searchForm.dispatchEvent(event);
            });
        }
    }
    
    // 식당 카드 호버 효과 개선
    const restaurantCards = document.querySelectorAll('.restaurant-card');
    
    restaurantCards.forEach(card => {
        card.addEventListener('click', function() {
            // 실제 구현에서는 식당 상세 페이지로 이동하는 코드를 추가합니다
            console.log('식당 카드가 클릭되었습니다.');
        });
    });
});