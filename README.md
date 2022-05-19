# 타이어 수명 예측 어플리케이션

# 목차
- [타이어 수명 예측 어플리케이션](#타이어-수명-예측-어플리케이션)
- [목차](#목차)
- [참여인력 및 역할분담](#참여인력-및-역할분담)
- [일정](#일정)
- [진행상황](#진행상황)
  - [양호준](#양호준)
  - [이문형](#이문형)
  - [정회준](#정회준)
  - [이석채](#이석채)
- [실행파일](#실행파일)

# 참여인력 및 역할분담
- 양호준
  - Kotlin 기반 안드로이드 어플리케이션 개발
    - 학습된 인공지능 모델 가중치 파일 안드로이드에 Load
    - RESTFUL API 통신을 통한 데이터 전송, 수신
    - 모바일 Local DataBase 구축
    - 최적화 작업 (TensorRT 적용 예정)
  - Pytorch model android 어플에 Load
    - 학습된 인공지능 모델 가중치 파일 인공지능

- 이문형
  - 현정이가 보내준 데이터(약 6GB 분량) 분석 및 전처리 방법 고안(석채가 Mobilenet v3로 학습 돌릴 수 있게 코드 제공해주는걸 우선으로 해볼것)

- 정회준
  - Machine-learning 기법으로 접근
    - 사이킷런 Decision Tree(XGBOOST)로 classification으로 학습 (당장 안드로이드 폰에 넣을것은 딥러닝 모델이므로 3월 1일 일정과는 별개로 실험느낌으로 접근하면 좋을듯)

- 이석채
  - 딥러닝 기법으로 접근
    - (Mobilenet v3 모델)해서 classification으로 학습

# 일정
- 3월 1일까지  
  - [x] 인공지능 모델 모바일 어플리케이션 탑재
- 5월 27일까지
  - [ ] 타이어 세그멘테이션 및 Binearization (양호준)
  - [ ] 타이어 깊이 추청 (이문형, 정회준)

# 진행상황

본인 이름 아래에 자기 계획 정리해두고 진행사항 **ipynb 파일로 정리**해서 남길 것 (전처리 작업, 혹은 계획사항 내지 진행사항 같은것 모두가 보기 쉽게 하여 협업하기 위함임)

## 양호준
- [x] Kotlin 안드로이드 개발을 위한 문법 복습  
  [복습자료](https://yanghojun.github.io/Kotlin%20%EA%B8%B0%EC%B4%88/)
- [x] 안드로이드 인공지능 모델 튜토리얼 진행  
  [안드로이드 Deeplearning 기초 스크립트 작성](/pytorch2android.ipynb)
- [x] 타이어 어플리케이션 코드 리팩토링
  - [x] 카메라 - 갤러리 연동 파트 다시 공부 및 개발
  - [x] EditText의 로그인 기능 및 키보드 이벤트 공부  
  [버튼 이벤트 처리](https://yanghojun.github.io/%EB%82%B4%EA%B0%80%20%EB%B3%B4%EB%A0%A4%EA%B3%A0%20%EB%A7%8C%EB%93%A0%20%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C%20%EA%B8%B0%EC%B4%88%20%EC%B4%9D%EC%A0%95%EB%A6%AC/)
  - [x] 뷰 바인딩, 리사이클러 뷰, 레이아웃 개념, 뷰 페이저, 프래그먼트 공부
  - [x] 코틀린, 자바 문법 비교 및 한번에 복습 및 객체지향 사고 향상, 함수형 프로그래밍 사고 향상
    - [x] 인터페이스, 추상메소드, SAM, 확장함수, 람다식, 람다식 축약  
  [코틀린 기초](https://yanghojun.github.io/Kotlin%20%EA%B8%B0%EC%B4%88/)  
  [자바 기초](https://yanghojun.github.io/categories/Java)  
  - [x] 인공지능 Prediction UI 구현
    - [x] Fragment 접목
    - [x] ViewPager2 접목
    - [x] Custom Gallery 활용을 위해 오픈소스 코드 해석 및 적용
  - [x] GUI 다듬기
- [ ] 타이어 세그멘테이션 & UI 다듬기
  - [x] 이미지 세그멘테이션 어플 돌리기(Pytorch 버젼 문제로 에러 발생해서 정신 나가는줄)
  - [ ] 석채 세그멘테이션 데이터 받아서 학습하고 결과 확인
  - [ ] 이미지 이진화로 타이어 트레드만 추출
  - [ ] 가이드라인 추가

## 이문형
- [ ] DataSet (Nas에 Tire_data.zip 올려둠 )    - Dataset 링크 여기 올려주라 (by 호준)
   * [Trie Dataset]
   * [2021091610417708]
     * [img1.jpg]
     * [img2.jpg]
   * [2021091610417709]
     * [file11.ext]
     * [file12.ext]
- [x] Dataset code **tire_Dataset.py** update 주석참고 (22_02_20)
- [x] 원본 이미지 해상도 __3024 x 4032__ 리사이징시 타이어 마모선이 보이지않는 이슈 및 데이터 사이즈가 너무 커짐 ->
- [ ] RGB 형태가 아닌, gray_scale 로 변경 필요성 및 이미지 분할 필요 -> 해당작업은 0222 ATG 츨장이후 추가적인 방법 생각해봄 
   

## 정회준
- [ ] Write something..

## 이석채
- [ ] Write something..

# 실행파일
- V 1.0 (2022-02-17)
  - [apk 파일](/app/release/app-release.apk)

- V 2.0 (2022-03-09)
  - [apk 파일](/app/build/outputs/apk/debug/app-debug.apk)
  - 데모영상

<img width="60%" src="/videos/Demo.gif">