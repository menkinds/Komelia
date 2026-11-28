---
type: "case"
project: "komelia"
date: "2026-11-28"
session: 1
summary: "리더를 열 때 뜨던 읽기 방식 알림(ReaderTypeNotification) 제거, PR #36 머지·릴리스 #32"
tool: "Claude Code"
model: "claude-opus-5-5"
tokens: "응답 수 170 · 입력 377 · 캐시 생성 981,160 · 캐시 읽기 37,808,413 · 출력 91,835 · 합계 38,881,785"
skills: ""
free_llm: ""
applied: "예 (menkinds/komelia)"
tags: ["case", "komelia"]
hub: "[[gbrain-notes/bossard/project-logs/Komelia/AI_CASE_LOG.md|AI_CASE_LOG]]"
pair: "[[komelia-2026-11-28-1-handoff]]"
prev: "[[komelia-2026-11-18-2-case]]"
next: ""
gbrain: "전송됨"
---

# 2026-11-28 — 리더를 열 때 뜨던 읽기 방식 알림(ReaderTypeNotification) 제거, PR #36 머지·릴리스 #32

- 목표/요청: 만화를 새로 읽기 시작할 때마다 아래에 뜨는 "Paged / Left to right" 팝업을 없애기.
- 도구: Claude Code (클라우드 세션)
- 모델: claude-opus-5-5
- 토큰 사용량: 응답 수 170 · 입력 377 · 캐시 생성 981,160 · 캐시 읽기 37,808,413 · 출력 91,835 · 합계 38,881,785 (같은 대화 전체 누적값, 앞선 komga 11-26·11-27 기록 포함)
- 핵심 프롬프트(원문):
  > (리더 화면 스크린샷) 새로 읽기 시작할때마다 저 팝업 안 뜨게 하고싶늠
  > komelia 리포 연결해서 그쪽도 봐바
  > 머지해 다
- 진행 내용:
  1. AI가 처음엔 같은 서버의 웹 리더(Komga) 팝업으로 잘못 짚고 그쪽을 고침.
  2. 사용자가 휴대폰 앱(Komelia) 저장소를 붙이라고 해서 확인하니, 팝업 문구를 만드는 코드가 앱 쪽에 정확히 있었음.
  3. 그 알림을 띄우는 함수와 호출을 지움(47줄 삭제). 읽기 방식 자체는 그대로.
  4. 컴파일 확인 후 머지 → 자동 APK 릴리스 빌드 성공.
- 막힌 점 · 실패와 원인:
  - 출처를 확인하기 전에 다른 프로그램으로 추정함. 팝업 문구를 코드에서 검색하면 바로 가려졌을 일.
  - 클라우드에서 앱을 빌드하려니 외부 라이브러리 하나(onnxruntime)를 받을 권한이 없어 다른 라이브러리까지 빈 채로 남음 → 필요한 3개만 따로 받아 해결.
  - Maven Central 요청 제한(429)으로 여러 번 재시도.
- 결과물: PR #36 머지(main `1194568`), android-release #32 성공. 실기기 확인 전.
## 연결
- 허브: [[gbrain-notes/bossard/project-logs/Komelia/AI_CASE_LOG.md|AI_CASE_LOG]]
- 짝: [[komelia-2026-11-28-1-handoff]]
- 이전: [[komelia-2026-11-18-2-case]]
- 다음:
