# README_CLAUDE.md (인계)

## 현재 상태 (2026-10-31 일일 정리, KST)
- 저장소: menkinds/Komelia (Snd-R/Komelia 포크, 앱 이름 PAGES, Android 전용). 기본 브랜치 main.
- PR 상태 (main의 git log 기준. gh는 이 세션에서 거부되어 미사용):
  - #1 CI 워크플로+주간 동기화: 병합됨
  - #2 SDK 설치 수정: 병합됨
  - #3 네이티브 빌드 수정: 병합됨
  - #4 수동 실행 Release 생성: 병합됨
  - #5 PAGES 이름/Unread first: 병합됨
  - #6 앱 내 업데이트: 병합됨
  - #7 main 푸시 자동 릴리스: 병합됨
  - #8 페이지 모드 자동 넘김: 병합됨
  - #9 리더 시계(옵션): main 로그에 없음. 병합 여부 미확인
- PR 없는 작업: job 022017(실패: git clone, 원격에 master 브랜치 없음), job 075855(v0.1.0 태그 푸시, 코드 변경 없음. 태그 결과는 미확인).
- 이 세션들에서 빌드·테스트를 실제로 돌린 기록은 없음(java/Android SDK 없음). 모든 기능은 CI 확인 전까지 미검증.

## 열린 항목
- android-release 실제 러너 실행 결과 확인(SDK 설치, 네이티브 Docker 빌드, 서명, 자동 릴리스) — 미확인.
- `./gradlew :komelia-domain:core:jvmTest`, komelia-ui jvmTest 실행 필요.
- #9(시계) 병합 여부와 내용 확인. 이 파일에 시계 항목 없음.
- 동시 머지 시 가운데 릴리스 실행이 취소될 수 있음(아래 자동 릴리스 항목).

## 다음 단계 (제안, 추정)
- 러너 로그(특히 native-build-log artifact) 확인 후 실패 시 수정.
- 첫 자동 릴리스 태그와 앱 내 업데이트 동작을 실기기로 확인.
- 시계 PR 병합 후 이 문서에 항목 추가.

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

## 1b: PAGES 이름, Unread first, 태그 APK 이름 (2026-10-31)
- 앱 라벨 `komelia-app/androidApp/.../res/values/strings.xml` app_name=PAGES (applicationId 불변, versionCode 25 그대로 → 올리려면 gradle/libs.versions.toml).
- Unread first: `BooksSort.UNREAD_FIRST`(기본값). `SeriesBooksState.fetchBooksPage`가 미완독(UNREAD+IN_PROGRESS) → READ 두 쿼리로 페이징(경계 페이지는 짧을 수 있음). 순서/페이지 로직은 `series/UnreadFirstOrdering.kt`, 테스트는 komelia-ui `commonTest`(jvmTest로 실행). 정렬 선택은 기존대로 저장되지 않음(화면 진입마다 기본값).
- 장르/저자 탭 필터는 업스트림에 이미 있음(SeriesContent/BookInfoContent → LibraryScreen(filter) → SeriesFilterState.applyFilter, 필터 UI에 활성 표시). 변경 없음. 시리즈 화면은 writer/penciller만 표시.
- 워크플로: 서명 단계에서 RELEASE_TAG가 있으면 `komelia-<tag>-arm64.apk`.
- 미검증: java/Gradle 없어 컴파일·테스트 실행 못 함. `Page.totalElements` 사용은 다른 코드(CollectionViewModel)와 같은 API.

## 앱 내 업데이트 → menkinds/Komelia Releases (2026-10-31)
- 업스트림에 이미 업데이트 체크(StartupUpdateChecker, AndroidAppUpdater, 설정>Updates 화면, UpdateDialog)가 있어 재사용. 새로 만들지 않음.
- 포크 소유 파일: `komelia-domain/core/.../updates/ForkUpdates.kt` (`FORK_RELEASES_REPO`, 태그 파싱, versionCode 계산, APK/sha256 선택) + `commonTest/.../ForkUpdatesTest.kt`.
- 업스트림 파일 수정: UpdateClient(repo 상수, draft/prerelease 필터, 403/429 메시지, getText), AppVersion(`current`를 var로, `v` 접두 허용, AppRelease.checksumUrl), StartupUpdateChecker(24h→12h), AndroidAppUpdater(파일로 받아 SHA-256 검증 후 PackageInstaller 세션, 알 수 없는 앱 설치 허용 화면 이동, 캐시 정리, 오류는 AppNotifications), AndroidAppModule/App.kt(설치된 versionName을 AppVersion.current로), androidApp/build.gradle.kts(-Pkomelia.versionName/versionCode), strings.xml(Dismiss→Later), core/build.gradle.kts(commonTest 의존성), android-release.yml.
- 설치된 버전 비교는 versionName 기준. 태그 없는 로컬 빌드는 0.20.0이라 업데이트 안 뜸(의도).
- 미검증: java 없어 컴파일/테스트 못 돌림. `./gradlew :komelia-domain:core:jvmTest`로 확인 필요.

## main 푸시 시 자동 릴리스 (2026-10-31)
- `android-release.yml`: `push: branches [main]` 추가. 다음 태그 = 기존 `^vX.Y.Z$` 중 최고 + PATCH 1 (없으면 v0.3.0). 계산은 `.github/scripts/next-tag.sh`(테스트: `bash .github/scripts/next-tag.test.sh`). Release가 태그를 만든다(git push 안 함).
- 건너뛰기: 커밋 메시지에 `[skip release]`, 또는 .md / .github/**(이 워크플로 제외)만 바뀐 푸시(paths 필터). minor/major는 수동 실행 `tag` 입력.
- 동시성: 그룹 `android-release-<ref>`, cancel-in-progress false. GitHub는 대기 중 실행을 1개만 유지하므로 빌드 중 3번 연속 머지하면 가운데 실행은 취소될 수 있음(마지막 커밋은 릴리스됨).
- 미검증: actionlint 없음(YAML 파싱·bash 테스트만). 실제 러너 실행 필요.

## 페이지 모드 자동 넘김 (2026-10-31)
- 하단바(페이지 슬라이더 오른쪽)에 재생/일시정지 버튼 + "10s" 텍스트(탭하면 3~60초 슬라이더 행이 슬라이더 위에 열림). 페이지 모드(PAGED)에서만. 연속/패널 모드 변경 없음.
- 타이머는 `common/ReaderContent.kt`의 LaunchedEffect 하나: 키(켜짐, 간격, 오버레이 열림, 페이지, 줌, 전환 페이지)가 바뀌면 처음부터 다시 셈. 오버레이 열림/줌인(`PagedReaderState.spreadFitZoom`보다 큼)/책 전환 페이지에서는 대기만 함. 마지막 페이지 도달 시 OFF(다음 책으로 안 감).
- OFF 조건: 앱 백그라운드(ON_STOP, `AppBackgroundEffect`, Android만), 리더 종료(`ReaderState.onDispose`), 비 페이지 모드. 켜진 동안 `KeepScreenOnEffect`(View.keepScreenOn, Android만).
- 간격만 저장: ImageReaderSettings.autoTurnIntervalSeconds, 마이그레이션 `V14__auto_page_turn.sql`. ON/OFF는 저장 안 함.
- 설정(톱니) 패널의 간격 항목은 만들지 않음(오버레이 행만).
- 미검증: 컴파일 못 함(java 없음). CI android-release로 확인 필요.

## 리더 시계 (2026-10-31)
- 설정(톱니) 패널 "Show clock" 토글(기본 OFF, 저장). 켜면 `ReaderClockOverlay`(우하단, 반투명 검정 배경+흰 글씨, 터치 통과)가 모든 리더 모드(paged/continuous/panels)에서 표시. 탭 오버레이가 열려 있는 동안은 숨김(슬라이더 가림 방지).
- 저장: 마이그레이션 `V15__fork_show_clock.sql`(`fork_show_clock`), V14 수정 없음. 포크 마이그레이션은 V15부터 `V<n>__fork_*.sql`.
- 시간: `ClockPlatform`(expect/actual). Android만 구현(기기 12/24h, 분 경계 정렬 갱신, ON_STOP 동안 정지). jvm/wasm은 빈 문자열(미표시).
- 미검증: 컴파일 못 함(java 없음). CI android-release로 확인 필요.
