# README_CLAUDE.md (인계)

## menkinds/komelia 포크 (Android 전용)
- 추가: `.github/workflows/android-release.yml`(태그 v* → 서명 APK Release), `.github/workflows/upstream-sync.yml` + `.github/scripts/upstream-sync.sh`(월요일 Snd-R/Komelia 최신 릴리스 병합 PR, 충돌 시 이슈), README "포크 사용법".
- Gradle: `./gradlew :androidRelease`(= androidApp:assembleRelease). 서명은 Gradle이 아니라 워크플로에서 zipalign+apksigner (업스트림 파일 수정 없음).
- 네이티브: `cmake/android-build.sh aarch64`를 러너에서 직접 실행(NDK 28.0.13004108), 이후 `android-aarch64_copyJniLibs`, `buildEpubReaders`. arm64만 빌드.
- 미검증: 이 세션에는 java/Android SDK/actionlint/gh가 없어 빌드·actionlint를 못 돌림. YAML 파싱과 `bash -n`만 확인. 첫 실행(workflow_dispatch)에서 네이티브 빌드 의존성(meson 등) 오류 가능.
- komga 저장소의 upstream-sync.yml은 읽지 못해(도구 없음, raw 404) 같은 패턴을 새로 작성함.

## android-release SDK 설치 수정 (2026-10-31)
- 원인: compileSdk=37인데 저장소 패키지명은 `platforms;android-37.0`(`android-37` 없음). 워크플로 env `PLATFORM_PACKAGE`로 지정, `cmdline-tools;latest` 먼저 설치, 설치 후 `--list_installed`와 디렉터리 확인.
- NDK 28.0.13004108, build-tools 36.0.0은 저장소에 존재 확인. SDK용 CMake는 빌드가 apt cmake를 쓰므로 설치하지 않음.
- 미검증: sdkmanager 직접 실행 불가(java/SDK 없음). 러너에서 재실행 필요.

## android-release 네이티브 빌드 수정 (2026-10-31, #2026-10)
- 증상: run 41179339901의 "Build native libraries (aarch64)"가 `ninja: build stopped: subcommand failed`로 실패.
- 이 세션에서는 `gh` 실행이 거부되어 전체 로그를 못 봄 → 첫 오류 줄은 확인 못 함. 원인은 추정: 호스트(ubuntu-24.04 + pip meson)와 업스트림 환경(ubuntu 25.10 Docker) 차이.
- 조치: 업스트림 README 방식대로 `cmake/android.Dockerfile`을 빌드해 실행(`--user $(id -u):$(id -g)`, chown/chmod 없음). 호스트 apt 설치 단계 제거. 실패 시 로그를 tee해 첫 오류를 출력하고 artifact로 업로드.
- 미검증: 러너에서 재실행 필요. 재실행 후 native-build-log artifact에서 실제 오류 확인.
