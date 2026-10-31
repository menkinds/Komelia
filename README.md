# Komelia - Komga media client

### Downloads:

- Latest prebuilt release is available at https://github.com/Snd-R/Komelia/releases
- Google Play Store https://play.google.com/store/apps/details?id=io.github.snd_r.komelia
- F-Droid https://f-droid.org/packages/io.github.snd_r.komelia/
- AUR package https://aur.archlinux.org/packages/komelia

## Screenshots

<details>
  <summary>Mobile</summary>
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" alt="Komelia" width="270">  
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" alt="Komelia" width="270">  
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" alt="Komelia" width="270">  
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" alt="Komelia" width="270">  
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" alt="Komelia" width="270">  
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" alt="Komelia" width="270">  
</details>

<details>
  <summary>Tablet</summary>
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/1.jpg" alt="Komelia" width="400" height="640">  
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/2.jpg" alt="Komelia" width="400" height="640">  
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/3.jpg" alt="Komelia" width="400" height="640">  
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/4.jpg" alt="Komelia" width="400" height="640">  
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/5.jpg" alt="Komelia" width="400" height="640">  
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/6.jpg" alt="Komelia" width="400" height="640">  
</details>

<details>
  <summary>Desktop</summary>
   <img src="/screenshots/1.jpg" alt="Komelia" width="1280">  
   <img src="/screenshots/2.jpg" alt="Komelia" width="1280">  
   <img src="/screenshots/3.jpg" alt="Komelia" width="1280">  
   <img src="/screenshots/4.jpg" alt="Komelia" width="1280">  
   <img src="/screenshots/5.jpg" alt="Komelia" width="1280">  
</details>

## Translations
You can help translate this project to your language by using service provided by [Weblate](https://hosted.weblate.org/engage/komelia/)

[![Translation status](https://hosted.weblate.org/widget/komelia/horizontal-auto.svg)](https://hosted.weblate.org/engage/komelia/)

## Build instructions
Make sure you download all git submodules\
`git clone --recurse-submodules https://github.com/Snd-R/Komelia` \
if you already cloned repository without recurse command run\
`git submodule update --init --recursive`

Requires jdk 17 or higher\
Android and JVM targets require C and C++ compiler for native libraries and Node.js for epub readers build.\
Recommended way to build is by using docker images that contain all required build dependencies.\
If you want to build with system toolchain and dependencies try running:\
`./gradlew komeliaBuildNonJvmDependencies` (Linux Only)

## Desktop App
Replace <*platform*> placeholder with your target platform. \
Available platforms include: `linux-x86_64`, `windows-x86_64`

- `docker build -t komelia-build-<platfrom> . -f ./cmake/<paltform>.Dockerfile `
- `docker run -v .:/build komelia-build-<paltform>`
- `./gradlew <platform>_copyJniLibs`
- `./gradlew buildEpubReaders`

Then choose your packaging option:
- `./gradlew :desktopRun` to launch desktop app
- `./gradlew :desktopJar` output in `./komelia-app/desktopApp/build/compose/jars`
- `./gradlew :desktopDeb` output in `./komelia-app/desktopApp/build/compose/binaries`
- `./gradlew :desktopMsi` output in `./komelia-app/desktopApp/build/compose/binaries`

## Android App
Replace <*arch*> placeholder with your target architecture.\
Available architectures include:  `aarch64`, `armv7a`, `x86_64`, `x86`

- `docker build -t komelia-build-android . -f ./cmake/android.Dockerfile `
- `docker run -v .:/build komelia-build-android <arch>`
- `./gradlew <arch>_copyJniLibs`
- `./gradlew buildEpubReaders`

Then choose app build option:

- `./gradlew :androidDebug` output in `./komelia-app/androidApp/build/outputs/apk/debug`
- `./gradlew :androidRelease` output in `./komelia-app/androidApp/build/outputs/apk/release`


## Komf Wasm WebUI
run `./gradlew :komfWebUI` output will be in `./build/komf-webui`

## Komf Wasm Extension
for chrome `./gradlew :komfExtensionChrome` \
for firefox `./gradlew :komfExtensionFirefox` \
output archive will be in `./komelia-komf-extension/app/build/distributions`
## 포크 사용법

이 포크(menkinds/komelia)는 Android APK만 빌드합니다. GitHub Actions가 서명된 APK를 만들어 Releases에 올립니다.

### 1. 키스토어 만들기 (최초 1회)
```
keytool -genkeypair -v -keystore komelia.jks -alias komelia -keyalg RSA -keysize 4096 -validity 36500
base64 -w0 komelia.jks > komelia.jks.base64
```
`komelia.jks`는 잃어버리면 같은 앱으로 업데이트할 수 없으니 따로 백업하세요. 저장소에 올리지 마세요.

### 2. GitHub Secrets 4개 등록
저장소 Settings → Secrets and variables → Actions → New repository secret

| 이름 | 값 |
|---|---|
| `KOMELIA_KEYSTORE_BASE64` | `komelia.jks.base64` 파일 내용 |
| `KOMELIA_KEYSTORE_PASSWORD` | 키스토어 비밀번호 |
| `KOMELIA_KEY_ALIAS` | `komelia` (위 명령의 alias) |
| `KOMELIA_KEY_PASSWORD` | 키 비밀번호 |

### 3. 릴리스 만들기
```
git tag v0.20.0-1
git push origin v0.20.0-1
```
Actions의 "Android release"가 끝나면 Releases에 APK와 SHA-256이 올라옵니다(네이티브 빌드로 오래 걸림). Actions 탭에서 수동 실행(workflow_dispatch)하면 Release 없이 artifact로만 받습니다.

### 4. 폰에 설치
Releases에서 APK를 받아 열고, "출처를 알 수 없는 앱 설치"를 허용합니다. applicationId는 원본과 같습니다. 원본 Komelia(다른 서명)가 깔려 있으면 먼저 지워야 설치됩니다(앱 데이터 삭제됨).

### 5. 업데이트 알림 (Obtainium)
Obtainium에서 "앱 추가" → `https://github.com/menkinds/komelia` 입력. 새 릴리스가 올라오면 알려 줍니다.

### 업스트림 동기화
매주 월요일 `upstream-sync` 워크플로가 Snd-R/Komelia 최신 릴리스를 `upstream-sync/<태그>` 브랜치로 합쳐 main으로 PR을 엽니다. 충돌이 있으면 PR 대신 충돌 파일 목록이 담긴 이슈가 열립니다. Settings → Actions → General에서 "Allow GitHub Actions to create and approve pull requests"를 켜야 합니다.
