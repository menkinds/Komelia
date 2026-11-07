# Komelia README_CLAUDE에서 옮긴 기록 2026-10

## 날짜별 구역 — 2026-11-07에 옮김

## android-release SDK 설치 수정 (2026-10-31)
- 원인: compileSdk=37인데 저장소 패키지명은 `platforms;android-37.0`(`android-37` 없음). 워크플로 env `PLATFORM_PACKAGE`로 지정, `cmdline-tools;latest` 먼저 설치, 설치 후 `--list_installed`와 디렉터리 확인.
- NDK 28.0.13004108, build-tools 36.0.0은 저장소에 존재 확인. SDK용 CMake는 빌드가 apt cmake를 쓰므로 설치하지 않음.
- 미검증: sdkmanager 직접 실행 불가(java/SDK 없음). 러너에서 재실행 필요.

## android-release 네이티브 빌드 수정 (2026-10-31, #2026-10)
- 증상: run 41179339901의 "Build native libraries (aarch64)"가 `ninja: build stopped: subcommand failed`로 실패.
- 이 세션에서는 `gh` 실행이 거부되어 전체 로그를 못 봄 → 첫 오류 줄은 확인 못 함. 원인은 추정: 호스트(ubuntu-24.04 + pip meson)와 업스트림 환경(ubuntu 25.10 Docker) 차이.
- 조치: 업스트림 README 방식대로 `cmake/android.Dockerfile`을 빌드해 실행(`--user $(id -u):$(id -g)`, chown/chmod 없음). 호스트 apt 설치 단계 제거. 실패 시 로그를 tee해 첫 오류를 출력하고 artifact로 업로드.
- 미검증: 러너에서 재실행 필요. 재실행 후 native-build-log artifact에서 실제 오류 확인.
