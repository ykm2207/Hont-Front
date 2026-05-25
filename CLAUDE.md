# Hont - 헬스케어 Android 앱

## 프로젝트 개요
- **앱 이름**: Hont
- **패키지**: `com.hont.app`
- **언어**: Kotlin
- **빌드**: Gradle Kotlin DSL (build.gradle.kts)
- **minSdk**: 26 / **targetSdk**: 36

## 백엔드
- **Base URL**: `https://hont-production.up.railway.app`
- Spring Boot 기반, Railway 호스팅
- OAuth2 소셜 로그인 지원 (Google, Kakao)

## 아키텍처
- **UI**: XML 레이아웃 + ViewBinding (Jetpack Compose 사용 안 함)
- **네비게이션**: BottomNavigationView + Fragment 전환 (FragmentManager)
- **네트워크**: Retrofit2 + OkHttp + Gson
- **토큰 저장**: DataStore Preferences (JWT accessToken / refreshToken)
- **인증 흐름**: OAuth2 -> 브라우저 -> 딥링크 콜백 (`hont://auth`) -> TokenManager 저장 -> MainActivity

## 주요 패키지 구조
```
com.hont.app
├── MainActivity.kt           # BottomNav + Fragment 컨테이너
├── auth/
│   ├── LoginActivity.kt      # OAuth2 진입점, 딥링크 처리
│   └── TokenManager.kt       # DataStore JWT 토큰 관리
├── network/
│   └── RetrofitClient.kt     # Retrofit 싱글톤
├── workout/
│   └── WorkoutFragment.kt
├── diet/
│   └── DietFragment.kt
├── profile/
│   └── ProfileFragment.kt
└── settings/
    └── SettingsFragment.kt
```

## 코딩 규칙

### 일반
- 모든 주석과 커밋 메시지는 **한국어**로 작성
- 코드는 Kotlin 관용 표현 사용 (data class, extension function, sealed class 등)
- 불필요한 주석, 빈 함수, 임시 코드 남기지 않기

### 네트워크
- API 인터페이스는 `network/` 패키지에 위치
- Retrofit 서비스 인터페이스는 `ApiService.kt`로 통합 관리
- 인증이 필요한 API는 OkHttp Interceptor로 Authorization 헤더 자동 주입

### 토큰 / 인증
- 토큰은 반드시 `TokenManager`를 통해서만 읽고 씀
- accessToken 만료 시 refreshToken으로 재발급하는 로직은 OkHttp Authenticator에서 처리

### UI
- ViewBinding 사용 필수 (findViewById 금지)
- Fragment 간 데이터 전달은 ViewModel 또는 Safe Args 사용
- 다크모드 대응: `values-night/` 리소스 유지

### 보안
- 토큰, 비밀키 등 민감 정보는 코드에 하드코딩 금지
- BASE_URL은 `RetrofitClient`에서만 정의 (LoginActivity의 하드코딩 추후 제거 필요)

## 추가 규칙 (여기에 작성)
<!-- 새 규칙은 이 아래에 추가 -->
