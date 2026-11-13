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

### Changed

- Primality is now proven in milliseconds for 19-digit inputs, instead of taking
  minutes of trial division on a phone.
- Numbers below 2 are explained in the UI ("Prime numbers start at 2") rather
  than only being given an exit code.

### Security

- The app declares no permissions at all, including no `INTERNET`.
- Backups disabled; no storage of user input; no analytics or third-party SDKs.
- Release builds are minified and resource-shrunk; signing secrets are read from
  an untracked `keystore.properties`.
