---
type: "handoff"
project: "komelia"
date: "2026-11-18"
session: 2
summary: "PR #34 병합·v0.2.22 릴리스(토큰 기능 첫 컴파일), Java4Shell은 AVX-512 CPU 한정 → DS723+ 해당 없음, 패치 확인 예약 끔"
tags: ["handoff", "komelia"]
hub: "[[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]"
pair: "[[komelia-2026-11-18-2-case]]"
prev: "[[komelia-2026-11-18-1-handoff]]"
next: ""
gbrain: "미전송"
---

# 2026-11-18 — PR #34 병합·v0.2.22 릴리스(토큰 기능 첫 컴파일), Java4Shell은 AVX-512 CPU 한정 → DS723+ 해당 없음, 패치 확인 예약 끔

## 한 일
- Temurin 정기 확인(11-18 14:13 KST): Temurin 25 최신은 그대로 25.0.5+8, Corretto 25.0.5·Zulu 25.0.5.0.3도 번호 변화 없음.
- PR #34(onnxruntime 서브모듈 건너뛰기 + 첫 세션 기록) 생성·병합(main 9a9d705). 워크플로 파일 변경이 push 조건에 포함돼 android-release #31이 자동 실행 → 성공, 릴리스 v0.2.22(11-18 19:20 KST). #33 토큰 코드 컴파일 처음 확인.
- Cloudflare Access Bypass 질문: Bypass는 인증 없이 통과시켜 목적에 안 맞음. 웹을 안 쓰면 Service Auth 정책 하나만 두면 된다고 안내.
- 오라클 JDK 빌드 가능성 조사 중 NVD·오라클 `cspunov2026`·oss-security(2026-11-17) 확인:
  - 영향 버전: Oracle Java SE 25.0.5.0.3 등까지. `download.oracle.com/java/25/latest`의 파일은 10-27 수정이라 무료 배포판에 패치가 들어갔는지는 미확인(아카이브 안 버전은 읽지 못함).
  - 발생 조건: AVX-512 VBMI 지원 x86-64 CPU(Intel Xeon Ice Lake 이후, AMD Zen 4 이후)만. 확인 명령은 `/proc/cpuinfo`의 avx512f·avx512bw·avx512vbmi·bmi2 네 개가 모두 있어야 함.
  - 완화: `-XX:-UseBASE64Intrinsics`(또는 x86 전용 `-XX:UseAVX=2`).
- 사용자 NAS는 DS723+(AMD Ryzen R1600, Zen 1, AVX-512 없음) → 해당 없음으로 안내. CPU 플래그 명령 결과는 사용자가 아직 알려 주지 않음.
- 사용자 결정에 따라 Temurin 확인 예약 작업 끔(trig_018tzDr3WJoX3Efc9bwBPAiM, enabled=false, 지우지 않음).

## 결정과 이유
- PAGES CF Access 토큰 기능은 유지: 비워 두면 기존 동작과 같고, 나중에 Access를 걸 때 바로 씀.
- Komga 재빌드·오라클 JDK 전환·원본 Komga 이슈 작성 안 함: 사용자 NAS CPU가 취약 조건에 해당하지 않음.

## 막힌 점
- 오라클 JDK 무료 다운로드 파일의 실제 버전 확인 실패(스트림 일부만 받아 `release` 파일을 못 찾음).

## 다음에 할 일
- 사용자가 NAS에서 CPU 플래그 명령을 실행해 해당 없음을 확정하면 끝.
- Komga를 Access 뒤에 둘 경우: Service Token + Service Auth 정책 → PAGES v0.2.22 로그인 화면에 ID·시크릿 → 목록·이미지·자동 로그인 확인.

## 연결
- 허브: [[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]
- 짝: [[komelia-2026-11-18-2-case]]
- 이전: [[komelia-2026-11-18-1-handoff]]
- 다음:
