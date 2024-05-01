# ⛵INDIAN_FROG

> ️항해99 6조 실전 프로젝트

## ✍🏻 서비스 소개

> 자신의 카드를 모른 채 상대와 심리전을 통해 승패를 가리는 인디언 포커 게임을 웹서비스로!


## 🐬 팀원 소개

* Back End
  - 오진선(팀장)(https://github.com/Rosa-Eau)
  - 강주성(https://github.com/kjs4231)
  - 박용운(https://github.com/eleunadeu)
  - 이주호(https://github.com/lsc713)

- Front End
  - 윤준수(https://github.com/hoheesu)
  - 함석원(https://github.com/holynow)


- Designer
  - 김지우

## 📐 시스템 아키텍처
![indianfrog.jpg](..%2F..%2FDownloads%2Findianfrog.jpg)

## 🖼️ Use Case Diagram

![Use case diagram](https://github.com/HH99-Main-IndianPoker/indian_frog_be/assets/139448668/8f20bb4a-c97d-4b56-89b9-e9a978f75583)

## 🔖 ERD
![frog](https://github.com/HH99-Main-IndianPoker/indian_frog_be/assets/139448668/a47eeba2-feea-41ba-ad2e-bb626607d276)

## 📄 API 명세서

URL: https://lydian-force-d54.notion.site/6-350e72e9edbf4dc8b75263e872dc44de

<img width="1710" alt="스크린샷 2024-04-27 오후 5 34 55" src="https://github.com/HH99-Main-IndianPoker/indian_frog_be/assets/139448668/3e54ed6f-04b2-4911-adc7-cc034ead9f76">
<img width="1710" alt="스크린샷 2024-04-27 오후 5 34 36" src="https://github.com/HH99-Main-IndianPoker/indian_frog_be/assets/139448668/52b18f9a-c57f-4e3b-a273-243d0d320b43">

## 📚 스택

- JDK 17
- Spring Boot 3.2.4
- Spring Boot JPA
- Spring Boot Validation
- Spring Boot Security
- Stomp
- QueryDSL
- JWT
- S3, EC2, RDS
- MySQL, Redis
- Github Actions, Docker-compose

## 🔧 구현 기능

- [x] 회원 가입 기능
- [x] 로그인 기능 & 소셜로그인
    - Access Token 발행
    - Refresh Token 발행
- [x] 마이페이지를 통한 정보 수정 기능
    - 비밀번호 수정
    - 게임포인트 충전기능
- [x] 게임방 생성 및 참가 기능
- [x] 채팅기능
- [x] 게임기능
    - 라운드 별 게임 진행
    - 각 라운드에 대해서 배팅 및 승패결정
    - 포인트 정산
- [x] 랭킹 페이지 기능

