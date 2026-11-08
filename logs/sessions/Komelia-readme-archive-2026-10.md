# Komelia README_CLAUDE에서 옮긴 기록 2026-10

## 현재 상태 — 2026-11-08에 옮김

- PR 없는 작업: 2026-10-31 job 022017(실패: git clone), job 075855(v0.1.0 태그 푸시). 2026-11-01 job 093056(새 런처 아이콘, PR 없음, 결과 미확인).

## 날짜별 구역 — 2026-11-08에 옮김

## android-release SDK 설치 수정 (2026-10-31)
- 원인: compileSdk=37인데 저장소 패키지명은 `platforms;android-37.0`(`android-37` 없음). 워크플로 env `PLATFORM_PACKAGE`로 지정, `cmdline-tools;latest` 먼저 설치, 설치 후 `--list_installed`와 디렉터리 확인.
- NDK 28.0.13004108, build-tools 36.0.0은 저장소에 존재 확인. SDK용 CMake는 빌드가 apt cmake를 쓰므로 설치하지 않음.
- 미검증: sdkmanager 직접 실행 불가(java/SDK 없음). 러너에서 재실행 필요.
