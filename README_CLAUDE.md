# README_CLAUDE.md (인계)

## menkinds/komelia 포크 (Android 전용)
- 추가: `.github/workflows/android-release.yml`(태그 v* → 서명 APK Release), `.github/workflows/upstream-sync.yml` + `.github/scripts/upstream-sync.sh`(월요일 Snd-R/Komelia 최신 릴리스 병합 PR, 충돌 시 이슈), README "포크 사용법".
- Gradle: `./gradlew :androidRelease`(= androidApp:assembleRelease). 서명은 Gradle이 아니라 워크플로에서 zipalign+apksigner (업스트림 파일 수정 없음).
- 네이티브: `cmake/android-build.sh aarch64`를 러너에서 직접 실행(NDK 28.0.13004108), 이후 `android-aarch64_copyJniLibs`, `buildEpubReaders`. arm64만 빌드.
- 미검증: 이 세션에는 java/Android SDK/actionlint/gh가 없어 빌드·actionlint를 못 돌림. YAML 파싱과 `bash -n`만 확인. 첫 실행(workflow_dispatch)에서 네이티브 빌드 의존성(meson 등) 오류 가능.
- komga 저장소의 upstream-sync.yml은 읽지 못해(도구 없음, raw 404) 같은 패턴을 새로 작성함.
