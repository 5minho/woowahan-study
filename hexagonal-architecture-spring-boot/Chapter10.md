# 10장. 아키텍처 경계 강제하기

지금까지 헥사고날 아키텍처에서 코드를 어떻게 작성하고 어디에 위치 시킬지 공부했다. <br>
일정 규모 이상의 모든 프로젝트에서는 시간이 지나면서 아키텍처가 서서히 무너지게 된다. <br>
이번 장에서는 아키텍처 내의 경계를 강제하는 방법을 소개한다.

## 경계와 의존성
아키텍처의 경계를 강제한다는 것은 무엇을 의미할까? <br>
헥사고날 아키텍처에서 경계를 강제한다는 것은 의존성을 한방향 (어댑터 -> 애플리케이션 -> 도메인) 으로 향하게 한다는 것이다.

## 접근 제한자
자바에서 제공하는 가장 기본적인 도구인 접근 제한자로 경계를 강제할 수 있다. <br>
여러 접근제한자 중 package-private 제한자는 자바 패키지를 통해 클래스들을 응집적인 '모듈'로 만들어 줄 수 있다. <br>
모듈의 진입점으로 활용될 클래스들만 골라서 public 으로 만들면 의존성이 잘못된 방향을 가리켜서 의존성 규칙을 위반할 위험이 줄어든다.
package-private 제한자는 몇 개 정도의 클래스로만 이뤄진 작은 모듈에서 가장 효과적이다.

## 컴파일 후 체크


## 빌드 아티팩트


## 유지보수 가능한 소프트웨어를 만드는데 어떻게 도움이 될까?
