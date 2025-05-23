# 🔍 Food_Find

**Food_Find**은 내 주변 식당에서 빠르게 선택할때 사용하는 **웹 기반 사이트**입니다.  

## 🖥️ 프로젝트 개요

- 🧩 **개발 환경**: ECLIPSE
- 🛠️ **구현 방식**: Spring Boot 기반 웹 개발
- 🗃️ **데이터베이스**: MySQL
- 🎯 **주요 기능**:
  - 사용자 회원가입 및 로그인
  - 식당 등록, 검색, 리뷰 등록 및 추천 기능
  - 사용자와 식당의 거리 표시
  - 근처 식당 분류 기능 (선택)
  - 카카오맵API를 이용하여 식당 위치 표시
  - 식당 예약 및 나의 예약 목록 확인
  - 내 식당 관리 및 예약 관리

---

## ⚙️ 기술 스택

| 구성 요소      | 사용 기술                |
|----------------|--------------------------|
| 백엔드         | Java, MySQL               |
| 프론트엔드     | HTML, CSS, JavaScript    |
| 서버           | Spring Boot 내장 서버     |
| 데이터베이스    | MySQL                    |

---

## 🌄 화면 미리보기

Food_Find의 주요 화면들을 아래에서 확인하실 수 있습니다.

<table>
  <tr>
    <td align="center"><b>🏠 메인 페이지</b></td>
    <td align="center"><b>📱 모바일 페이지</b></td>
  </tr>
  <tr>
    <td><img src="./images/메인페이지.png" width="100%"></td>
    <td><img src="./images/메인페이지 모바일.png" width="50%"></td>
  </tr>
  <tr>
    <td align="center"><b>📄 상세페이지1</b></td>
    <td align="center"><b>📄 상세페이지 지도</b></td>
  </tr>
  <tr>
    <td><img src="./images/상세페이지1.png" width="100%"></td>
    <td><img src="./images/상세페이지 지도.png" width="100%"></td>
  </tr>
  <tr>
    <td align="center"><b>📄 상세페이지 식당 정보</b></td>
    <td align="center"><b>📄 상세페이지 리뷰</b></td>
  </tr>
  <tr>
    <td><img src="./images/상세페이지 식당 정보.png" width="100%"></td>
    <td><img src="./images/상세페이지 리뷰.png" width="100%"></td>
  </tr>
    <tr>
    <td align="center"><b>📄 식당 예약</b></td>
    <td align="center"><b>📄 나의 예약 목록</b></td>
  </tr>
  <tr>
    <td><img src="./images/식당 예약.png" width="100%"></td>
    <td><img src="./images/나의 예약 목록.png" width="100%"></td>
  </tr>
    <tr>
    <td align="center"><b>📄 내 식당 관리</b></td>
    <td align="center"><b>📄 식당 예약 관리</b></td>
  </tr>
  <tr>
    <td><img src="./images/내 식당 관리.png" width="100%"></td>
    <td><img src="./images/예약 관리.png" width="100%"></td>
  </tr>
    <tr>
    <td align="center"><b>🔍 검색페이지</b></td>
    <td align="center"><b>💾 DB</b></td>
  </tr>
  <tr>
    <td><img src="./images/검색페이지.png" width="100%"></td>
    <td><img src="./images/데이터베이스1.png" width="100%"></td>
  </tr>
</table>

---

## 🛠️ 기술 설명

- 거리 계산
  
  식당 신규 등록시 주소를 입력하게 됩니다.<br/> 입력과 동시에 카카오맵API를 이용하여 주소를 위도와 경도로 변환 후
  저장시 DB에 주소와 같이 저장되어 사용자와 식당의 거리를 계산시 하버사인 공식을 사용하여 서버에서 처리 후 표시 하게 됩니다.
    <tr>
    <td><img src="./images/위도 경도.png" width="30%"></td>
  </tr>
## 📌 향후 개선 방향

- 코드 최적화 및 쿼리 개선

---

## 📮 문의

- 디자인: **김보현**
- 백엔드, 프론트 개발자: **김보현**  
- 이메일: `qhgus9346@gmail.com`
