document.addEventListener('DOMContentLoaded', function() {
    // 예약 가능 시간대 옵션을 생성하는 함수
    function generateTimeOptions(startTime, endTime, interval = 30) {
        const timeSelect = document.getElementById('time');
        if (!timeSelect) return;
        
        // 기존 옵션 제거 (첫 번째 옵션 "시간을 선택하세요" 유지)
        while (timeSelect.options.length > 1) {
            timeSelect.remove(1);
        }
        
        // 시작 시간과 종료 시간 파싱
        const [startHour, startMinute] = startTime.split(':').map(Number);
        const [endHour, endMinute] = endTime.split(':').map(Number);
        
        let currentTime = new Date();
        currentTime.setHours(startHour, startMinute, 0);
        
        const endDateTime = new Date();
        endDateTime.setHours(endHour, endMinute, 0);
        
        // 30분 간격으로 시간 옵션 생성
        while (currentTime < endDateTime) {
            const hour = currentTime.getHours().toString().padStart(2, '0');
            const minute = currentTime.getMinutes().toString().padStart(2, '0');
            const timeString = `${hour}:${minute}`;
            
            const option = document.createElement('option');
            option.value = timeString;
            option.textContent = timeString;
            timeSelect.appendChild(option);
            
            // 다음 시간대로 이동 (30분 추가)
            currentTime.setMinutes(currentTime.getMinutes() + interval);
        }
    }
    
    // 선택한 날짜에 따라 예약 가능 시간을 업데이트하는 함수
    function updateAvailableTimes() {
        const dateInput = document.getElementById('date');
        const restaurantId = document.getElementById('uploadId').value;
        
        if (!dateInput || !restaurantId) return;
        
        const selectedDate = dateInput.value;
        if (!selectedDate) return;
        
        // API 호출로 예약 가능 시간 조회
        fetch(`/api/reservation/available-times/${restaurantId}?date=${selectedDate}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('예약 가능 시간 조회에 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            const timeSelect = document.getElementById('time');
            
            // 기존 옵션 제거 (첫 번째 옵션 "시간을 선택하세요" 유지)
            while (timeSelect.options.length > 1) {
                timeSelect.remove(1);
            }
            
            // 예약 가능 시간 옵션 추가
            data.forEach(slot => {
                const option = document.createElement('option');
                option.value = slot.time;
                option.textContent = slot.time;
                timeSelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            alert('예약 가능 시간을 불러오는데 실패했습니다.');
        });
    }
    
    // 날짜 입력 필드에 이벤트 리스너 추가
    const dateInput = document.getElementById('date');
    if (dateInput) {
        dateInput.addEventListener('change', updateAvailableTimes);
    }
    
    // 식당 영업 시간이 페이지에 제공되어 있는 경우 초기 시간 옵션 생성
    const openTimeElement = document.querySelector('.restaurant-time span');
    if (openTimeElement) {
        const timeRange = openTimeElement.textContent;
        const [openTime, closeTime] = timeRange.split(' ~ ');
        
        if (openTime && closeTime) {
            generateTimeOptions(openTime, closeTime);
        }
    }
    
    // 폼 제출 전 유효성 검사
    const reservationForm = document.getElementById('reservationForm');
    if (reservationForm) {
        reservationForm.addEventListener('submit', function(event) {
            const date = document.getElementById('date').value;
            const time = document.getElementById('time').value;
            const personCount = document.getElementById('personCount').value;
            
            if (!date || !time || !personCount) {
                event.preventDefault();
                alert('모든 필수 항목을 입력해주세요.');
                return;
            }
            
            // 현재 날짜/시간과 비교하여 유효한 예약인지 확인
            const now = new Date();
            const reservationDateTime = new Date(`${date}T${time}`);
            
            if (reservationDateTime <= now) {
                event.preventDefault();
                alert('예약 시간은 현재 시간 이후로 선택해주세요.');
                return;
            }
        });
    }
});