---
type: "handoff"
project: "Komelia"
date: "2026-11-08"
session: 2
summary: "자동 넘기기: 그림 양을 선(경계) 픽셀로 측정, 측정값 화면 표시, PR #31 병합, v0.2.20 릴리스"
tags: ["handoff", "Komelia"]
hub: "[[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]"
pair: "[[Komelia-2026-11-08-2-case]]"
prev: "[[Komelia-2026-11-08-1-handoff]]"
next: ""
gbrain: "미전송"
---

# 2026-11-08 — 자동 넘기기: 그림 양을 선(경계) 픽셀로 측정, 측정값 화면 표시, PR #31 병합, v0.2.20 릴리스

## 한 일
- 사용자가 "1%당 100ms가 맞는 수치냐"고 물음 → 근거 없는 어림값(기본은 300)이고, 검은 비율은 어두움을 재서 선화 위주 그림과 어긋난다고 답함. 사용자가 "1(측정값 표시) 3(측정 방식 변경)"을 고름.
- `EdgeRatio.percent`: 밝기가 오른쪽·아래 이웃과 48 이상 차이 나는 픽셀 %. 흰 바탕·먹칠 면은 테두리만 셈. 샘플 폭 300→400px. 시간 계산의 그림 기준을 이것으로 교체(검은 %는 표시용으로만 계산).
- `AutoTurnMeasurementOverlay`: 리더 왼쪽 위 `선 ○% · 검은 ○% · 글자 ○ → ○초`(측정 전 "측정 중…"). 설정 스위치 "측정값 화면에 표시"는 `TextAwareAutoTurnState.showMeasurements`(메모리만, 저장 안 함).
- 페이지별 캐시를 `Deferred<IntArray?>`로 바꿈(글자 [n], 그림 [선, 검은]). 캐시 키 "DRAWING".
- 테스트 3개 추가(평평한 면 0%, 선, 먹칠 테두리) → 17/17 통과. komelia-ui JVM 컴파일 통과. PR #31(앞선 /save 정리 커밋 2개 포함) 생성·병합 → v0.2.20 릴리스 성공.

## 결정과 이유
- DB 컬럼과 설정 이름(perInkPercentMs)은 그대로 두고 뜻만 "선 1%당"으로 바꿈: 마이그레이션 없이 바꾸려고.
- 측정값 표시 스위치는 저장하지 않음: 보정할 때만 잠깐 쓰는 디버그 기능.
- 임계값 48, 샘플 폭 400, 기본 300ms는 실측 없이 정한 값(추정).

## 막힌 점
- 이 환경에 실제 만화 이미지가 없어 선 % 실측 불가. 사용자 실기기 측정값이 필요.
- GBrain에 README_CLAUDE.md는 "본문이 너무 깁니다"로 지난번에 못 올림.

## 다음에 할 일
- 사용자가 v0.2.20에서 본 선 %(요츠바랑, 글 많은 책)를 받아 1%당 ms 기본값과 임계값 조정.

## 연결
- 허브: [[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]
- 짝: [[Komelia-2026-11-08-2-case]]
- 이전: [[Komelia-2026-11-08-1-handoff]]
- 다음:
