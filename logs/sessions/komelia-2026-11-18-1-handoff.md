---
type: "handoff"
project: "komelia"
date: "2026-11-18"
session: 1
summary: "PAGES Cloudflare Access 서비스 토큰 헤더(PR #33), 자바 취약점 대응 점검, CI onnxruntime 서브모듈 건너뛰기"
tags: ["handoff", "komelia"]
hub: "[[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]"
pair: "[[komelia-2026-11-18-1-case]]"
prev: "[[Komelia-2026-11-08-2-handoff]]"
next: ""
gbrain: "전송됨"
---

# 2026-11-18 — PAGES Cloudflare Access 서비스 토큰 헤더(PR #33), 자바 취약점 대응 점검, CI onnxruntime 서브모듈 건너뛰기

## 한 일
- 연합뉴스 기사(2026-11-18, 자바 고위험 취약점 CVE-2026-114938 'Java4Shell', OpenJDK Base64 디코더, CVSS 4.0 9.2) 요약. WebFetch는 yna.co.kr 차단, 사용자 허락 후 curl로 받음.
- 영향 점검: Cloudflare Worker `freellmapi-pwa`(JS)와 skills 저장소 도커 템플릿(python)은 자바 없음. Komga(`gotson/komga:latest` 1.30.1, 11-10 빌드)는 자바 25 JRE 포함, 패치 전 빌드.
- menkinds/komga 포크 확인: `komga/docker/Dockerfile.tpl` 10·24행 `COPY --from=eclipse-temurin:25-jre`, master 푸시 시 GHCR 발행. Temurin 25 최신은 25.0.5+8(10-22)로 패치 전. Corretto·Zulu·SapMachine·Liberica도 11-18 기준 패치 버전 없음(릴리스 노트까지는 미확인).
- 매일 14:13 KST에 Temurin 25 패치 확인하는 예약 작업 생성(trig_018tzDr3WJoX3Efc9bwBPAiM, 이 세션에 바인딩). 나오면 komga 포크 Dockerfile 버전 고정 커밋을 `claude/yna-news-article-n49q04`에 올리고 끔.
- PAGES(Komelia 포크)에 Cloudflare Access 서비스 토큰 헤더 기능 → PR #33 병합(main 48ee22a).
- android-release #30 실패: 체크아웃 중 `microsoft/onnxruntime` 서브모듈 "Repository not found". 코드 문제 아님. CI에서 onnxruntime 서브모듈을 건너뛰는 수정(795ae15) 작업 브랜치에 커밋.
- Komga 대체 서버(읽기 전용 최소판) 규모 산정: PAGES가 쓰는 API 113개 중 25개 구현 + 15개 빈 응답, 서버 약 3천 줄, 2~3주. 사용자가 보류.

## 결정과 이유
- 토큰은 저장한 서버 호스트로 가는 요청에만 보냄: 업데이트 확인 등 다른 호스트로 새지 않게.
- 저장소는 기존 쿠키와 같은 SharedPreferences(암호화 아님): 기존 방식과 맞춤.
- `SecretsRepository` 새 메서드는 기본 no-op: 데스크톱 구현을 건드리지 않음.
- Komga 재작성은 하지 않음: 취약점은 언어와 무관, 패치 후 Dockerfile 한 줄로 해결.
- `/api/*`만 Access에서 빼는 방식은 쓰지 않음: 취약점이 API 경로의 인증 헤더 처리에서 날 수 있음.

## 막힌 점
- 로컬 컴파일 불가: 세션 git 프록시가 Snd-R/microsoft 서브모듈을 막아 gradle 구성 단계 실패. 컴파일은 CI 몫.
- GitHub API(api.github.com)는 세션에서 403 → Komga 릴리스 노트 대신 Docker Hub 태그·레지스트리 메타데이터로 확인.

## 다음에 할 일
- 795ae15를 main에 합치고 android-release 수동 실행 → #33 컴파일 확인 후 릴리스.
- 실기기: Cloudflare Service Token 만들고 Access에 Service Auth 정책 추가 → PAGES 로그인 화면에 ID·시크릿 입력 → 목록·이미지·자동 로그인 확인.
- Temurin 25 패치 나오면(예약 작업이 알림) komga 포크 Dockerfile 고정 → master 반영(사용자 확인) → NAS 이미지 재생성 → `docker exec komga java -version`.
- 그때까지 Komga는 내려 두거나 내부망·VPN으로만.

## 연결
- 허브: [[gbrain-notes/bossard/project-logs/Komelia/README_CLAUDE.md|README_CLAUDE]]
- 짝: [[komelia-2026-11-18-1-case]]
- 이전: [[Komelia-2026-11-08-2-handoff]]
- 다음:
