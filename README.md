# 타이어 수명 예측 어플리케이션

# 마음가짐
- 본인이 공부해서 도움이 될 만한 자료는 여기 ReadME에 업로드
- 본인이 작성한 코드는 **ipynb 을 통해서 markdown, code**를 적절히 혼합하여 <p style="color:green">누구든 쉽게 알아보고 활용할 수 있도록 작성할 것</p>
  
# 참여인력 및 역할분담
- 양호준
  - Kotlin 기반 안드로이드 어플리케이션 개발
    - 학습된 인공지능 모델 가중치 파일 안드로이드에 Load
    - RESTFUL API 통신을 통한 데이터 전송, 수신
    - 모바일 Local DataBase 구축
    - 최적화 작업 (TensorRT 적용 예정)

- 이문형
  - 현정이가 보내준 데이터(약 6GB 분량) 분석 및 전처리 방법 고안(석채가 Mobilenet v3로 학습 돌릴 수 있게 코드 제공해주는걸 우선으로 해볼것)

- 정회준
  - Machine-learning 기법으로 접근
    - 사이킷런 Decision Tree(XGBOOST)로 classification으로 학습 (당장 안드로이드 폰에 넣을것은 딥러닝 모델이므로 3월 1일 일정과는 별개로 실험느낌으로 접근하면 좋을듯)

- 이석채
  - 딥러닝 기법으로 접근
    - (Mobilenet v3 모델)해서 classification으로 학습

## 깃허브 공부할 때 추천하는 블로그
- 구독, 좋아요, 알람설정 :)  
[Juneer blog](https://yanghojun.github.io/categories/%EA%B9%83%ED%97%88%EB%B8%8C)

# 일정
- 3월 1일까지  
  - [ ] 인공지능 모델 모바일 어플리케이션 탑재

# 실행파일
- V 1.0 (2022-02-17)
  - [apk 파일](/app/release/app-release.apk)