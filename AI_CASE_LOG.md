# AI_CASE_LOG

## 2026-10-31 menkinds/komelia 포크 CI #2026-10
- Android 서명 APK 릴리스 워크플로와 주간 업스트림 동기화를 추가. 빌드 환경이 없어 로컬 빌드·actionlint는 미실행.

## 2026-10-31 menkinds/komelia android-release SDK 설치 실패 수정 #2026-10
- `platforms;android-37`은 Google 저장소(repository2-3.xml)에 없고 `platforms;android-37.0`만 있음이 원인. 패키지명 수정, cmdline-tools 선갱신, 설치 검증 추가. sdkmanager는 java 없어 직접 실행 못함.

## 2026-10-31 android-release 네이티브 빌드 수정 #2026-10
- 업스트림 Dockerfile 방식으로 전환. gh 로그 조회가 막혀 근본 원인은 미확정, 진단 로그 추가.

## 2026-10-31 Komelia 포크 PAGES 1b: 앱 이름, 안 읽은 책 먼저 정렬 #2026-10
- app_name=PAGES, Unread first 기본 정렬(두 쿼리 페이징). 장르/저자 탭 필터는 업스트림에 이미 있어 변경 없음. PR #5 병합. 컴파일·테스트는 미실행.

## 2026-10-31 Komelia 앱 내 업데이트(포크 Releases) #2026-10
- 업스트림 업데이터를 재사용해 menkinds/Komelia Releases를 가리키게 함. SHA-256 검증 추가. PR #6 병합. 컴파일·테스트 미실행.

## 2026-10-31 android-release 자동 릴리스와 수동 Release #2026-10
- 수동 실행(PR #4)과 main 푸시(PR #7) 모두 GitHub Release 생성. 다음 태그는 next-tag.sh로 계산. actionlint 미실행. 태그 v0.1.0 푸시 job(코드 변경 없음)은 결과 미확인. 첫 job(022017)은 git clone 실패로 종료.

## 2026-10-31 Komelia 페이지 모드 자동 넘김 #2026-10
- 재생/일시정지와 3~60초 간격(저장). PR #8 병합. 컴파일 미실행.

## 2026-10-31 Komelia 리더 시계 옵션 #2026-10
- job 130908 완료, PR #9. main 로그에 병합 기록 없음. 병합 여부와 내용은 미확인.

