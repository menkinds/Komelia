---
type: "handoff"
project: "Komelia"
date: "2026-11-07"
session: 1
summary: "시리즈 커버 읽기 버튼, PR #29 머지, v0.2.18 릴리스"
tags: ["handoff", "Komelia"]
hub: "[[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]"
pair: "[[Komelia-2026-11-07-1-case]]"
prev: ""
next: ""
gbrain: "전송됨"
---

# 2026-11-07 — 시리즈 커버 읽기 버튼, PR #29 머지, v0.2.18 릴리스

## 한 일
- 시리즈 화면 커버 가운데 빨간 원형 읽기 버튼(#FF0335) 추가. 커버 탭도 같은 동작. 대상은 `SeriesViewModel.nextBook`(진행 중 → 첫 안 읽은 책 → 첫 권, READY·삭제 안 된 책), 초기화·reload·이 시리즈의 BookEvent/ReadProgressSeriesEvent 때 다시 계산.
- PR menkinds/Komelia#29 생성. 머지 전 android-release workflow_dispatch(태그 없음)로 컴파일 확인(run 42338688253 성공) → 머지(60f0aa0c) → 자동 릴리스 v0.2.18.

## 결정과 이유
- 폰에는 hover가 없어 버튼을 항상 표시. 열기는 기존 `onBookReadClick(book, true)` 재사용(진행 저장). 19+ 숨김은 RestrictedKomgaApi가 그대로 적용.

## 막힌 점
- 로컬 gradle 컴파일: 처음엔 서브모듈 없음, `git submodule update --init` 후에는 Maven Central 429(Too Many Requests)로 실패. 대신 Actions의 빌드 전용 실행으로 확인.

## 다음에 할 일
- v0.2.18 실기기에서 커버 버튼(진행 중/다음 책/다 읽음), EPUB 시리즈, 19+ 잠금 상태 확인.
- Komga처럼 길게 눌렀을 때만 버튼 표시로 바꿀지는 사용자 의견 대기(현재 항상 표시).

## 연결
- 허브: [[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]
- 짝: [[Komelia-2026-11-07-1-case]]
- 이전:
- 다음:
