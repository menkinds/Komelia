---
type: "handoff"
project: "Komelia"
date: "2026-11-08"
session: 1
summary: "자동 넘기기: 그림(흑백 비율)과 글자 수 중 더 긴 시간 사용, PR #30 병합, v0.2.19 릴리스"
tags: ["handoff", "Komelia"]
hub: "[[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]"
pair: "[[Komelia-2026-11-08-1-case]]"
prev: ""
next: ""
gbrain: "전송됨"
---

# 2026-11-08 — 자동 넘기기: 그림(흑백 비율)과 글자 수 중 더 긴 시간 사용, PR #30 병합, v0.2.19 릴리스

## 한 일
- 자동 넘기기에 흑백 비율(그림 양) 기준 추가. 페이지를 가로 300px로 줄여 밝기 128 미만 픽셀 %를 잼(`InkRatio.percent`).
- 처음엔 모드 선택(글자/그림)으로 만들었다가, 사용자 요청으로 자동 결정으로 바꿈: 시간 = clamp(기본 + max(글자 수×글자당 ms, 검은 %×1%당 ms), 최소, 최대). OCR이 없거나 실패하면 그림만.
- 설정 화면에 "검은 영역 1%당 ms"(0~1000, 기본 300) 슬라이더. 스위치 이름 "글자·그림 양에 맞춰 조절". OCR 안내 문구를 "그림 양만으로 계산합니다"로.
- DB 마이그레이션 V20 `fork_auto_turn_per_ink_percent_ms`.
- PR #30 생성·병합(main a3f6f2e). android-release 성공 → v0.2.19 APK.

## 결정과 이유
- 그림과 글자 중 더 긴 시간을 씀(사용자 지시): 글자가 적은 책(요츠바랑)이 너무 빨리 넘어가는 문제.
- 두 페이지 펼침은 비율을 더함(글자 수와 같은 방식).
- 기본 간격 안에 계산이 안 끝난 기준은 건너뜀(기존 OCR 동작과 같음).

## 막힌 점
- Maven Central 429로 Gradle 의존성 다운로드 실패 → Google 미러로 바꾸는 init 스크립트로 해결. 첫 시도에서 저장소를 추가해 Google Maven이 가려졌고, URL 교체만 하도록 고침.
- Android SDK가 없어 안드로이드 빌드는 CI(android-release)로 확인.

## 다음에 할 일
- 사용자가 v0.2.19로 요츠바랑 등을 넘겨 보고 1%당 ms 기본값 피드백 → 필요하면 기본값 조정.

## 연결
- 허브: [[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]
- 짝: [[Komelia-2026-11-08-1-case]]
- 이전:
- 다음:
