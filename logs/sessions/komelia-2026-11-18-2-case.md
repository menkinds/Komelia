---
type: "case"
project: "komelia"
date: "2026-11-18"
session: 2
summary: "PR #34 병합·v0.2.22 릴리스(토큰 기능 첫 컴파일), Java4Shell은 AVX-512 CPU 한정 → DS723+ 해당 없음, 패치 확인 예약 끔"
tool: "Claude Code"
model: "claude-opus-5-5, claude-haiku-5-5 (서브에이전트)"
tokens: "응답 수 58 · 입력 132 · 캐시 생성 108,399 · 캐시 읽기 7,517,503 · 출력 35,582 · 합계 7,661,616 / 응답 수 2 · 입력 4 · 캐시 생성 9,540 · 캐시 읽기 4,239 · 출력 1,028 · 합계 14,811"
skills: ""
free_llm: ""
applied: "예 (menkinds/komelia)"
tags: ["case", "komelia"]
hub: "[[gbrain-notes/bossard/project-logs/Komelia/AI_CASE_LOG.md|AI_CASE_LOG]]"
pair: "[[komelia-2026-11-18-2-handoff]]"
prev: "[[komelia-2026-11-18-1-case]]"
next: "[[komelia-2026-11-28-1-case]]"
gbrain: "전송됨"
---

# 2026-11-18 — PR #34 병합·v0.2.22 릴리스(토큰 기능 첫 컴파일), Java4Shell은 AVX-512 CPU 한정 → DS723+ 해당 없음, 패치 확인 예약 끔

- 목표/요청: PAGES 릴리스 빌드 다시 돌리기, 오라클 JDK로 Komga 빌드 가능 여부 확인.
- 도구: Claude Code
- 모델: claude-opus-5-5, claude-haiku-5-5 (서브에이전트)
- 토큰 사용량: 응답 수 58 · 입력 132 · 캐시 생성 108,399 · 캐시 읽기 7,517,503 · 출력 35,582 · 합계 7,661,616 / 응답 수 2 · 입력 4 · 캐시 생성 9,540 · 캐시 읽기 4,239 · 출력 1,028 · 합계 14,811
- 핵심 프롬프트(원문):
  > 릴리즈 빌드 다시해줘
  > 응 오라클 JDK로 빌드되는지 알아봐줘
  > ds723+
- 진행 내용:
  - PR #34 병합 → android-release #31 성공, v0.2.22. #33 토큰 코드 첫 컴파일 확인.
  - 오라클 JDK 조사 중 NVD·oss-security에서 취약점이 AVX-512 VBMI x86-64 CPU 한정이고 `-XX:-UseBASE64Intrinsics`로 완화된다는 사실 확인.
  - DS723+(Ryzen R1600, Zen 1)는 해당 없음 → Komga 재빌드 불필요, Temurin 확인 예약 작업 끔.
- 막힌 점 · 실패와 원인: 앞선 조사(기사·배포판 버전만 봄)에서 CPU 조건을 놓쳐, 필요 없었을 대응(Access 토큰, 예약 확인)을 먼저 진행함. 1차 자료(oss-security·NVD)를 처음부터 봤으면 바로 결론이 났을 것.
- 결과물: 최근 커밋: 9a9d705 Merge pull request #34 from menkinds/claude/yna-news-article-n49q04; 585f613 docs: mark 2026-11-18 session files as sent to GBrain; 5f85bb5 docs: 2026-11-18 session handoff (CF Access token, CI fix); 795ae15 ci: skip desktop-only onnxruntime submodule in android-release; 48ee22a Merge pull request #33 from menkinds/claude/yna-news-article-n49q04; f15d234 PAGES: send Cloudflare Access service token headers; ef3660c Merge pull request #32 from menkinds/ccr-8c8be647-367lxl; 28bc6d3 리더: 하단 책 진행 막대(설정 저장, V21) 추가, 자동 넘기기 측정값을 왼쪽 아래로 옮기고 남은 초 표시 / 변경 파일: README_CLAUDE.md

## 연결
- 허브: [[gbrain-notes/bossard/project-logs/Komelia/AI_CASE_LOG.md|AI_CASE_LOG]]
- 짝: [[komelia-2026-11-18-2-handoff]]
- 이전: [[komelia-2026-11-18-1-case]]
- 다음: [[komelia-2026-11-28-1-case]]
