---
type: "handoff"
project: "komelia"
date: "2026-11-28"
session: 1
summary: "리더를 열 때 뜨던 읽기 방식 알림(ReaderTypeNotification) 제거, PR #36 머지·릴리스 #32"
tags: ["handoff", "komelia"]
hub: "[[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]"
pair: "[[komelia-2026-11-28-1-case]]"
prev: "[[komelia-2026-11-18-2-handoff]]"
next: ""
gbrain: "전송됨"
---

# 2026-11-28 — 리더를 열 때 뜨던 읽기 방식 알림(ReaderTypeNotification) 제거, PR #36 머지·릴리스 #32

## 한 일
- 사용자 스크린샷(리더 아래 `Paged / Left to right` 팝업)을 보고 처음엔 Komga 웹 리더로 착각해 Komga 쪽을 고침. 사용자가 Komelia 저장소를 붙이라고 해서 확인하니 문구가 `ReaderTypeNotification`(`reader/image/common/ReaderContent.kt`)과 정확히 일치 → 실제 출처는 Komelia.
- 그 호출과 함수, 그 함수만 쓰던 import 8개 삭제(-47줄). 리더 모드·읽기 방향 적용 동작은 그대로, "책의 처음입니다" 같은 다른 알림도 그대로.
- `./gradlew :komelia-ui:compileKotlinJvm` 통과(ReaderContent.kt 경고 없음).
- PR #36 병합(main `1194568`) → android-release #32 성공.

## 결정과 이유
- 사용자 요청: 책을 열 때마다 뜨는 모드·방향 팝업을 없앤다. 설정 토글은 만들지 않고 그냥 제거(요청이 "안 뜨게").

## 막힌 점
- 서브모듈: `microsoft/onnxruntime`은 세션 범위 밖이라 클론 실패 → `git submodule update`가 중단되며 ChipTextField 등이 빈 폴더로 남음. 필요한 3개만 기록된 커밋으로 fetch+checkout해서 해결.
- `.gitmodules`의 일부 path가 실제 gitlink와 다름(indexeddb, libspng 등). 서브모듈 목록은 `git ls-files -s | awk '$1=="160000"'`로 봐야 함.
- Maven Central 429 → 2분 간격 재시도로 통과.
- 얕은 클론이라 작업 브랜치 원격 추적이 없어 훅이 "푸시 안 됨"으로 오인 → fetch refspec 추가 후 upstream 설정.

## 다음에 할 일
- 폰에 #32 APK 설치 후 확인(미확인).

## 연결
- 허브: [[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]
- 짝: [[komelia-2026-11-28-1-case]]
- 이전: [[komelia-2026-11-18-2-handoff]]
- 다음:
