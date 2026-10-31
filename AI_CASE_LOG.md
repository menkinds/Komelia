# AI_CASE_LOG

## 2026-10-31 menkinds/komelia 포크 CI #2026-10
- Android 서명 APK 릴리스 워크플로와 주간 업스트림 동기화를 추가. 빌드 환경이 없어 로컬 빌드·actionlint는 미실행.

## 2026-10-31 menkinds/komelia android-release SDK 설치 실패 수정 #2026-10
- `platforms;android-37`은 Google 저장소(repository2-3.xml)에 없고 `platforms;android-37.0`만 있음이 원인. 패키지명 수정, cmdline-tools 선갱신, 설치 검증 추가. sdkmanager는 java 없어 직접 실행 못함.

## 2026-10-31 android-release 네이티브 빌드 수정 #2026-10
- 업스트림 Dockerfile 방식으로 전환. gh 로그 조회가 막혀 근본 원인은 미확정, 진단 로그 추가.
