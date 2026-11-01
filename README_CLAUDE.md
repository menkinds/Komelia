# README_CLAUDE.md (인계)

## 현재 상태 (2026-11-01 일일 정리, KST)
- 저장소: menkinds/Komelia (Snd-R/Komelia 포크, 앱 이름 PAGES, Android 전용). 기본 브랜치 main.
- PR 상태 (main의 git log 기준. gh는 이 세션에서 거부되어 미사용):
  - #1~#8 (CI, SDK, 네이티브 빌드, 수동 Release, PAGES 이름/Unread first, 앱 내 업데이트, 자동 릴리스, 자동 넘김): 병합됨
  - #9 리더 시계: 병합됨 (main에 커밋 a2dd6af3, 병합 ba33594e)
  - #10 2026-10-31 일일 정리: 병합됨
  - #11 19+ 잠금: 병합됨
  - #12 19+ 잠금 v2(여러 라이브러리): 병합됨
  - #13 런처 아이콘(사이버펑크 네온 P): 병합됨
  - #14 라이브러리 필터 기억 + 태그 탭 전체 검색: 병합됨
  - #15 시리즈 별점 ★N 태그: 병합됨
  - #16 19+ 잠금 재인증 후 화면 유지: 병합됨
  - #17 19+ 잠금 컬렉션/읽기 목록: 병합됨
  - #18 텍스트 인식 자동 넘김(온디바이스 OCR): main 로그에 없음. 병합 여부 미확인 (이 문서에 내용 없음)
- PR 없는 작업: 2026-10-31 job 022017(실패: git clone), job 075855(v0.1.0 태그 푸시). 2026-11-01 job 093056(새 런처 아이콘, PR 없음). 아이콘은 job 114822 재시도가 #13으로 병합됨. job 093056의 결과는 미확인.
- 이 세션들에서 빌드·테스트를 실제로 돌린 기록은 없음(java/Android SDK 없음). 모든 기능은 CI 확인 전까지 미검증.

## 2026-11-01 변경 요약
- 19+ 잠금 신설(#11) → 여러 라이브러리(#12) → 재인증 후 화면 유지(#16) → 컬렉션/읽기 목록 확장(#17). 아래 상세 절 참고.
- 런처 아이콘 교체(#13): mipmap PNG, adaptive icon XML, 배경 #01010D. 아래 상세 없음. 파일 목록은 커밋 b0683f84 참고.
- 라이브러리 정렬/필터 기억(V18)과 태그 탭 전체 검색(#14).
- 시리즈 별점 ★N 태그와 마지막 책 완독 프롬프트(#15).
- DB 마이그레이션: V16, V17, V18 추가 (V15까지는 2026-10-31).

## 열린 항목
- android-release 실제 러너 실행 결과 확인(SDK 설치, 네이티브 Docker 빌드, 서명, 자동 릴리스) — 미확인.
- `./gradlew :komelia-domain:core:jvmTest`, komelia-ui jvmTest 실행 필요 (AdultLockStateTest 포함).
- #18(OCR 자동 넘김) 병합 여부와 내용 확인.
- 19+ 잠금의 남은 누출 가능성(저자 자동완성, 태그 목록, 컬렉션 카드 개수 등)과 Android 오버레이 실기기 확인.
- Ttsu/Komga EPUB 리더는 별점 프롬프트 없음.
- 동시 머지 시 가운데 릴리스 실행이 취소될 수 있음(아래 자동 릴리스 항목).

## 다음 단계 (제안, 추정)
- 러너 로그(특히 native-build-log artifact) 확인 후 실패 시 수정. 오늘 병합분(V16~V18, biometric 의존성)의 첫 컴파일 오류 가능성이 큼.
- 첫 자동 릴리스 태그와 앱 내 업데이트, 19+ 재인증 흐름을 실기기로 확인.
- #18 병합 후 이 문서에 OCR 자동 넘김 항목 추가.

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

## 리더 시계 (2026-10-31, #9 병합됨)
- 설정(톱니) 패널 "Show clock" 토글(기본 OFF, 저장). 켜면 `ReaderClockOverlay`(우하단, 반투명 검정 배경+흰 글씨, 터치 통과)가 모든 리더 모드(paged/continuous/panels)에서 표시. 탭 오버레이가 열려 있는 동안은 숨김(슬라이더 가림 방지).
- 저장: 마이그레이션 `V15__fork_show_clock.sql`(`fork_show_clock`), V14 수정 없음. 포크 마이그레이션은 V15부터 `V<n>__fork_*.sql`.
- 시간: `ClockPlatform`(expect/actual). Android만 구현(기기 12/24h, 분 경계 정렬 갱신, ON_STOP 동안 정지). jvm/wasm은 빈 문자열(미표시).
- 미검증: 컴파일 못 함(java 없음). CI android-release로 확인 필요.

## 19+ 라이브러리 잠금 (2026-11-01) — PR "feat: 19+ library lock (hidden adult library, biometric unlock)"
- 마이그레이션: **V16** `V16__fork_restricted_library.sql` (`AppSettings.fork_restricted_library_id`, nullable TEXT). 설정 이름 `restrictedLibraryId`(코드명), UI 문자열에는 "adult" 없음.
- 상태: `snd.komelia.adult.AdultLockState`(core). `unlocked: StateFlow<Boolean>` 기본 false(콜드 스타트 잠김), `libraryId`(설정값, null이면 기능 꺼짐), `hiddenLibraryId`(지금 숨길 id 또는 null).
- 중앙 필터: `RestrictedKomgaApi`(core, `KomgaApi by delegate`)가 AppModule의 `komgaApi` 두 경로(remote/offline) 모두를 감쌈. library/series/book/collections/readList/referential API만 재정의.
  - libraryIds 인자가 있는 API(getNewSeries, getUpdatedSeries, getBooksOnDeck, collections/readLists getAll, referential 목록)는 서버 필터로 "숨길 것 제외 전체 id"를 넘김. 나머지(검색, 조건 목록, latest, 컬렉션/읽기목록 내용)는 결과를 클라이언트에서 `libraryId`로 거름. 이때 `totalElements`는 그 페이지에서 뺀 개수만큼만 보정(페이지가 짧을 수 있음, 전체 페이지 수는 서버 값).
  - 개별 조회(getOneSeries, getOne(book), getLibrary)는 숨김 라이브러리면 `NotFoundException("Not found")`. `LibraryScreen`은 숨김 id면 빈 화면.
- 잠금 해제: 드로어/네비의 "Libraries" 버튼 길게 누르기(탭 동작 그대로). 버튼 모양은 기능 on/off와 무관하게 동일(항상 long-click 변형 사용). 기능이 꺼졌거나 이미 해제면 아무 일 없음.
- Android(`AdultLockPlatform.android.kt`): `BiometricPrompt` + `BIOMETRIC_STRONG or DEVICE_CREDENTIAL`(API 30 미만은 라이브러리 제약으로 `BIOMETRIC_WEAK or DEVICE_CREDENTIAL`), negative button 없음. 성공 → unlock, 오류/취소 → 아무것도 안 함. 재잠금: `ProcessLifecycleOwner` ON_STOP → lock (MainView에서 세션 내내 구성됨). BiometricPrompt는 같은 Activity의 fragment라 프로세스 ON_STOP을 안 일으킴. API 28-29 기기 자격증명 화면은 ON_STOP 후 성공 콜백이 unlock하므로 최종 상태는 해제.
- 비 Android(jvm/wasmJs): expect/actual 스텁(`rememberAdultUnlockRequest` = 아무것도 안 하는 람다, `AdultRelockEffect` = 없음). 설정이 있으면 라이브러리는 숨겨진 채 해제 수단 없음.
- 화면 갱신: `MainScreenViewModel`이 `hiddenLibraryId` 변화 시 메인 스택을 Home으로 교체+reload, 재잠금 시 루트 스택(리더/설정)을 MainScreen까지 pop. `AppModule`은 `hiddenLibraryId` 변화 시 `KomgaAuthenticationState.reloadLibraries()`.
- 설정 항목: 외형(Appearance) 설정의 드롭다운(`settings_restricted_library*`, ko 포함 `values-ko`). 설정값이 있으면 해제 상태에서만 보임(잠김 땐 숨김). 새로 고르면 즉시 lock.
- 위험한 곳(컴파일 못 함): expect/actual 3개 타깃, `androidx.biometric:biometric:1.1.0`·`androidx.lifecycle:lifecycle-process:2.10.0` 버전, `combinedClickable` opt-in, `Page(...)` 생성자 인자(RemoteBookApi 것과 동일), `LabeledEntry<String?>`, `values-ko` 리소스 폴더, 마이그레이션 파일 등록.
- 남은 누출 가능성: 저자 이름 자동완성(`getAuthorsNames/Roles`), `getSeriesTags(libraryId)`, 컬렉션/읽기목록 카드의 항목 수·썸네일(섞인 항목), 검색 결과 전체 건수/페이지 수(서버 값).

## 19+ 잠금 v2: 여러 라이브러리 + 길게 누르기로 선택 (2026-11-01)
- 마이그레이션 **V17** `V17__fork_restricted_libraries.sql`: 새 열 `fork_restricted_library_ids` (TEXT NOT NULL DEFAULT '', 쉼표 구분). V16의 단일 id는 UPDATE로 복사. 옛 열 `fork_restricted_library_id`는 남겨 두고 코드에서 쓰지 않음. 설정 코드명 `restrictedLibraryIds: Set<String>`(`getRestrictedLibraryIds/putRestrictedLibraryIds`).
- `AdultLockState`: `libraryIds`, `hiddenLibraryIds`, `currentHiddenLibraryIds()`, `isHidden()`(집합 포함 여부). `RestrictedKomgaApi`는 `isHidden`만 쓰므로 필터는 모든 id에 적용됨.
- 설정 > 외형의 19+ 항목 제거. 라이브러리 목록 제목(드로어/네비의 Libraries 버튼) 길게 누르기: 집합이 비었거나 해제 상태 → unlock 후 `RestrictedLibrariesDialog`(체크박스, 변경 즉시 저장). 잠김+비어 있지 않음 → BiometricPrompt, 성공 시 unlock + 다이얼로그.
- 비 Android: `adultLockSupported=false`라 길게 눌러도 아무 일 없음(선택 불가, 해제 불가).
- 미검증: 컴파일 못 함.

## 라이브러리 정렬/필터 기억 + 태그 탭은 전체 라이브러리 검색 (2026-11-01)
- 마이그레이션 **V18** `V18__fork_library_series_filter.sql`: `AppSettings.fork_library_series_filter` (TEXT NOT NULL DEFAULT ''). 값은 JSON(`SavedSeriesFilter`, series/SavedSeriesFilter.kt). 설정 API `getLibrarySeriesFilter/putLibrarySeriesFilter`(CommonSettingsRepository).
- 모든 라이브러리 화면(전체 보기 포함)이 한 세트를 공유. 변경 즉시 저장(`LibrarySeriesTabState.saveFilter`), 화면 진입 시 복원(`restoreSavedFilter`). 검색어는 저장 안 함. "Reset filters" → 기본값 저장.
- 현재 라이브러리에 없는 태그/장르/출판사/언어/연령/출간연도/저자는 적용만 건너뛰고(`SeriesFilterState.restore`), 저장값에서는 지우지 않음(`unavailable`을 저장 시 합침). 저자는 이름 검색으로 확인(조회 실패 시 유지).
- 장르/태그/저자 칩 탭 → `LibraryScreen(null, filter)`(전체 라이브러리 화면 재사용, `SeriesScreenFilter.libraryIdFor`). 필터가 넘어오면 임시 필터라 저장/복원 안 함. 19+ 숨김은 기존 `RestrictedKomgaApi`가 그대로 적용. 다른 칩(출판사, 상태, 연령, 언어)은 기존처럼 해당 라이브러리.
- 알려진 한계: 이미 기본값인데 숨겨진 저장값만 남은 경우 Reset은 상태 변화가 없어 저장을 다시 하지 않음.
- 미검증: 컴파일 못 함(java/SDK 없음).

## 시리즈 별점 ★N 태그 (2026-11-01)
- 별점 = Komga 시리즈 태그 `★N`(U+2605, N=1..5). 다른 저장소/DB 캐시 없음, 항상 시리즈 메타데이터에서 읽음(cBossard가 양방향 동기화).
- 새 파일: `komelia-ui/.../ui/pages/SeriesRating.kt` (파싱/갱신 `SeriesRating`, 별 UI `SeriesRatingStars`, 완독 프롬프트 `SeriesRatingPrompts` + `SeriesRatingPromptHost`).
- 설정: `withRating`이 `★*` 전부 제거 후 `★N` 추가, 다른 태그 유지. 같은 별 다시 탭 → 해제. `SeriesViewModel.setRating`이 즉시 반영, 서버 실패 시 되돌리고 오류 알림.
- 프롬프트: `ReaderState.onProgressChange`가 마지막 페이지 근처에서 책을 다시 조회해 completed 확인 → `getBookSiblingNext == null`(시리즈 마지막 책) → 별점 없음이면 `MainView`의 호스트가 다이얼로그 표시.
- 한계: 이미지 리더(ReaderState)만 연결. Ttsu/Komga EPUB 리더 완독은 프롬프트 안 뜸. 한국어 `dialog_confirm`="확인"을 values-ko에 추가(앱 전체 확인 버튼에 적용).
- 미검증: 컴파일 못 함(java/SDK 없음). `tags` 타입은 List<String>으로 가정.

## 19+ 잠금: 재인증 후 같은 화면 유지 (2026-11-01)
- 이전: ON_STOP마다 `lock()` → `MainScreenViewModel`이 리더 pop + 메인 스택 Home 교체. 인증해도 책 위치를 잃음. 마이그레이션 변경 없음(V16/V17/V18 그대로).
- 지금: `AdultLockState.lockOnBackground()`(Android `AdultRelockEffect`의 ON_STOP). 숨김 라이브러리 화면이 열려 있으면 `resumePending=true` + 잠금(API 필터는 즉시 적용, 유예 없음)만 하고 **화면 스택은 그대로**. 아니면 예전처럼 일반 잠금.
- "숨김 라이브러리 화면" 판별: 화면이 `AdultLockScreenTag(libraryId)`(platform/AdultLockScreenTag.kt)로 지금 보이는 내용의 libraryId를 등록. 적용: ImageReader, Epub, Book, Oneshot, Series, Library 화면. 컬렉션/읽기목록 화면은 태그 없음(일반 잠금).
- 오버레이: `AdultResumeOverlay`(MainView 최상단, Android 구현). 불투명 배경으로 아래 화면을 완전히 가리고 터치 차단. 액티비티가 RESUMED가 되면 BiometricPrompt 자동 표시.
  - 성공 → `unlockAfterResume()`: 오버레이 제거, 화면 리셋 없음(`consumeKeepScreens`), `screenReloadFlow`로 잠긴 동안 실패했을 수 있는 데이터만 재조회. 리더는 같은 ViewModel이라 책/페이지/모드/줌/읽기 진행 그대로.
  - 취소/실패/뒤로가기 → `cancelResume()`: 잠금 유지, 리더 pop + Home 교체(예전 동작). 진행은 리더가 페이지 변경 때마다 저장한 값 유지.
  - 시스템 취소(`ERROR_CANCELED`, 인증 중 다시 백그라운드)는 오버레이 유지, 다음 RESUME에 다시 표시.
- 프로세스 종료(5번): **자동 복원 안 함**. 재실행은 항상 잠김 + 오버레이 대상 아님. 인증 후 책을 직접 열면 서버에 저장된 마지막 페이지에서 이어 읽음(기존 동작). 이유: 복원된 Voyager 스택의 리더가 잠긴 API에서 책 조회 실패 → 자동 복원은 스택 복원/인증 순서를 건드려야 해서 침습적.
- 알려진 한계: 로딩 중(libraryId 미확정)에 백그라운드로 가면 일반 잠금. 시리즈/책 화면의 SSE 핸들러가 잠긴 동안 재조회해 실패할 수 있음(해제 후 reload로 복구).
- 테스트: `komelia-domain/core/src/commonTest/.../adult/AdultLockStateTest.kt`(순수 Kotlin 상태 로직). 러너에 Android SDK/java가 없어 실행 못 함 → 첫 컴파일·테스트는 릴리스 빌드/CI. Android 오버레이·BiometricPrompt 흐름은 실기기 확인 필요.

## 19+ 잠금: 컬렉션/읽기 목록에서 연 책도 재인증 후 유지 (2026-11-01)
- 위 "재인증 후 같은 화면 유지" 방식을 그대로 재사용(같은 `AdultLockScreenTag`/오버레이). 판단은 책·시리즈 화면별: 리더/책/시리즈는 원래 자기 libraryId로 태그됨.
- 컬렉션·읽기 목록 화면: 지금 보이는 항목(`series`/`books`)의 libraryId 전체를 태그(`AdultLockScreenTag(Collection<KomgaLibraryId>)`, `AdultLockState.registerVisibleScreen(token, Collection)`). 하나라도 잠금 라이브러리면 그 화면도 잠금(오버레이로 가림). 모두 일반 라이브러리면 예전처럼 일반 잠금.
- 로딩 중: `ImageReaderScreen(libraryIdHint)`(`readerScreen(book)`이 book.libraryId를 넘김)과 EpubScreen의 `book`으로 책 로딩 전에도 태그. 로딩이 잠금에 끊겨 Error가 되면, 해제 후 `screenReloadEvents`에서 같은 책으로 `initialize` 재시도(이미지 리더). BookScreen/Oneshot/Series는 기존대로 vm의 항목이 이미 있으면 그 libraryId 사용.
- 한계: bookId만으로 열린 리더(힌트 없음)는 로딩 중 일반 잠금. Epub 리더는 로딩 실패 시 재시도 없음(Error 화면).
- 테스트: `AdultLockStateTest`에 혼합 컬렉션, 일반 항목만 있는 읽기 목록, 잠금 항목 있는 읽기 목록, 책 로딩 상태(힌트 유무) 추가. 러너에 Android SDK/java가 없어 실행·컴파일 못 함 → 첫 컴파일은 릴리스 빌드.

## 글자 양에 맞춘 자동 넘김 (ML Kit OCR) (2026-11-01)
- 자동 넘김 오버레이(하단 "10s" 탭)의 슬라이더 아래에 "글자 양에 맞춰 조절"(기본 OFF). 켜면 페이지별 지연 = clamp(기본 간격 + 글자당 ms × 글자 수, 최소, 최대). 기본: 글자당 100ms, 최소 2s, 최대 30s. 꺼져 있으면 동작 변화 없음.
- 마이그레이션 **V19** `V19__fork_text_aware_auto_turn.sql`(ImageReaderSettings에 `fork_auto_turn_*` 5열: text_aware, per_char_ms, min_seconds, max_seconds, ocr_language). 설정 API `getTextAwareAutoTurn/putTextAwareAutoTurn`(`TextAwareAutoTurnSettings`, core `snd.komelia.pages`). OCR 언어 기본 JAPANESE, 선택 저장.
- 순수 로직(core `snd/komelia/pages/TextAwareAutoTurn.kt`): `TextAwareDelay.delayMs`, `OcrTextCounter`(공백·기호 제외, 글자/숫자만 셈; 신뢰도 0.4 미만 줄 제외; 블록 글자 3자 미만=고립 기호/짧은 효과음 제외), `SimpleLru`(50). 테스트 `commonTest/.../pages/TextAwareAutoTurnTest.kt`.
- UI(`komelia-ui/.../ui/pages/`): `TextAwareAutoTurnState`(설정+글자 수 캐시+대기), `TextAwareAutoTurnSettingsPanel`(스위치, 슬라이더, OCR 언어 행: 없음/다운로드 중/설치됨 + 다운로드 버튼, 이유 문구), `PageTextRecognizer`(인터페이스; expect `rememberPageTextRecognizer`). jvm/wasm은 `NoPageTextRecognizer`(null → 기본 간격).
- 타이머(`ReaderContent` LaunchedEffect): 기본 간격으로 시작, 그 안에 글자 수가 도착하면 남은 시간을 조정, 못 오면(또는 OCR 실패) 기본 간격에 넘김. 다음 스프레드는 현재 페이지 표시 중 미리 OCR. 키는 책+페이지+언어, 이미지는 `BookImageLoader.loadImage`로 따로 디코드(디스크 캐시 재사용, 리더 이미지와 공유 안 함).
- Android(`PageTextRecognizer.android.kt`): 번들 없는 Play 서비스 ML Kit(`play-services-mlkit-text-recognition`, `-japanese/-korean/-chinese`), 모델은 `ModuleInstallClient`로 요청 시 다운로드(APK 크기 증가 없음). 긴 변 2048px로 축소 후 ARGB 비트맵으로 인식(Default 디스패처). 이미지 데이터는 기기 밖으로 안 나감.
- 업스트림 파일 수정: ReaderState(ctor에 imageLoader, `textAwareAutoTurn`), ReaderViewModel, ReaderContent, SettingsContent, ImageReaderSettings/Repository/Wrapper/Exposed/Table, AppMigrations, strings(values, values-ko), libs.versions.toml, komelia-ui/build.gradle.kts.
- 한계/미검증: java/Android SDK 없어 컴파일·테스트 못 함. ML Kit 일본어 인식기는 세로쓰기 인식률이 낮을 수 있음(실기기 확인 필요). `play-services-base:18.5.0`, ML Kit 버전, `TextRecognizer`가 `OptionalModuleApi`인 점, `Line.confidence` API는 CI에서 확인.
