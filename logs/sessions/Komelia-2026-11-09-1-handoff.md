---
type: "handoff"
project: "Komelia"
date: "2026-11-09"
session: 1
summary: "리더 하단 책 진행 막대(V21), 측정값 왼쪽 아래 + 남은 초, PR #32 병합, v0.2.21 릴리스"
tags: ["handoff", "Komelia"]
hub: "[[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]"
pair: "[[Komelia-2026-11-09-1-case]]"
prev: "[[Komelia-2026-11-08-2-handoff]]"
next: ""
gbrain: "전송됨"
---

# 2026-11-09 — 리더 하단 책 진행 막대(V21), 측정값 왼쪽 아래 + 남은 초, PR #32 병합, v0.2.21 릴리스

## 한 일
- 책 진행 막대: `ReaderProgressBar.kt`(BoxScope, 맨 아래 3dp, 반투명 검은 바탕 위 흰 선, 비율 = readProgressPage / currentBookPages.size). ReaderContent에서 `showProgressBar && !showSettingsMenu`일 때 표시(모든 리더 모드).
- 설정 저장: 시계(showClock) 경로를 그대로 복제 — ImageReaderSettingsRepository get/putShowProgressBar, Wrapper, ImageReaderSettings, Table `fork_show_progress_bar`, Exposed 읽기/쓰기, V21 마이그레이션, ReaderState(showProgressBar, load, onShowProgressBarChange), SettingsContent/SettingsSideMenu/BottomSheetSettingsOverlay 파라미터, CommonImageSettings 스위치("책 진행 막대 표시").
- 측정값 표시: TopStart → BottomStart, 배경 알파 0.5(시계와 같음). `AutoTurnMeasurement.turnAt`(TimeMark) 추가, 오버레이가 매초 남은 초를 갱신해 `→ 9.4초 (5초)`.
- JVM 컴파일(ui, sqlite)과 TextAwareAutoTurnTest 통과. PR #32(앞선 /save 정리 커밋 2개 포함) 병합 → v0.2.21 릴리스 성공(약 19분).

## 결정과 이유
- "전체화면일 때" = 탭 메뉴(showSettingsMenu)가 닫혀 있을 때로 해석(시계와 같은 조건).
- 흰 페이지에서 흰 선이 안 보이므로 반투명 검은 바탕을 깔았음(사용자 요청에 없던 추가, 보고함).
- 설정 문구 "Show clock"은 원래 영어 하드코딩이라 그대로 두고, 새 스위치만 문자열 리소스로 추가.

## 막힌 점
- 화면 모습 확인 수단 없음(실기기 필요). Android SDK 없음.

## 다음에 할 일
- 사용자 실기기 피드백: 막대 두께·위치, 측정값 위치, 선 % 실측값 → 1%당 ms 기본값 조정.

## 연결
- 허브: [[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]
- 짝: [[Komelia-2026-11-09-1-case]]
- 이전: [[Komelia-2026-11-08-2-handoff]]
- 다음:
