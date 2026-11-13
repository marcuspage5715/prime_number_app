# Changelog

All notable changes to this project. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-11-14

The Python Termux script became an Android app.

### Added

- Native Android app (Kotlin + Jetpack Compose, Material 3) with a single screen:
  type a number, tap Check, read the verdict.
- Smallest-prime-factor reporting for composite numbers.
- `:core` module: pure Kotlin number theory shared by every front end.
- Deterministic Miller–Rabin primality test, exact for the whole 64-bit range.
- Bounded, cancellable factor search with an explicit "no factor up to N" result.
- Strict input parser: ASCII digits, optional sign, length-bounded before parsing.
- `:cli` module preserving the original terminal behaviour and exit codes
  (0 prime / 1 not prime / 2 bad input).
- Unit tests for `:core`, `:app` and `:cli`; Compose UI and activity tests.
- GitHub Actions CI (tests, lint, debug APK) and Dependabot for Gradle and Actions.
- `SECURITY.md`, `CONTRIBUTING.md`, architecture notes and launcher artwork.
- Play-ready release pipeline: `.github/workflows/release.yml` builds a signed
  App Bundle on a `v*` tag, verifies the signature and attaches it to a GitHub
  release.
- `PLAY_STORE.md`: listing copy, Data safety answers, content rating, upload-key
  flow and the closed-testing requirement for new personal developer accounts.
- Privacy policy (`docs/privacy-policy.md`, ready for GitHub Pages) and Play
  artwork: 512 px icon, 1024 x 500 feature graphic, and five 1080 x 1920
  screenshots in `docs/screenshots/`.
- Roborazzi screenshot tests: the real UI is rendered on the JVM, so store
  screenshots and layout checks need no device or emulator.

### Changed

- Primality is now proven in milliseconds for 19-digit inputs, instead of taking
  minutes of trial division on a phone.
- Targets API 36 with AGP 9.3.2, Gradle 9.7.1, Kotlin 2.3.0 and Compose BOM
  2025.09.01. Play refuses new apps below API 36 since 2026-08-31; `minSdk` stays
  24, so API 33 devices are unaffected.
- AGP 9's built-in Kotlin support replaces the `kotlin-android` plugin.
- Numbers below 2 are explained in the UI ("Prime numbers start at 2") rather
  than only being given an exit code.

### Fixed

- A 19-digit result wrapped mid-digit-group ("...693,95" / "1 is prime"); the
  verdict headline now steps down a type size with its length.
- On invalid input the field's helper text was painted in the error colour while
  the real message sat in the card, so the screen appeared to complain about the
  wrong thing.

### Security

- The app declares no permissions at all, including no `INTERNET`.
- Backups disabled; no storage of user input; no analytics or third-party SDKs.
- Release builds are minified and resource-shrunk; signing secrets are read from
  an untracked `keystore.properties`.
