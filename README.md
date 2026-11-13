# Prime Checker

A native Android app that tells you whether a whole number is prime — and, when
it is not, which prime divides it.

This repository used to be `prime_number_checker`: one 60-line Python script that
ran in Termux. It is now a proper Android project. The number theory was lifted
out of the script into a pure Kotlin module (`:core`), which the Compose app
(`:app`) and an optional terminal front end (`:cli`) both call. The maths is
therefore written once, tested once, and cannot drift between the two.

<img src="docs/play_store_icon.png" alt="Prime Checker icon" width="96" height="96">

## What it does

- Type a number, tap **Check**, get a verdict: *97 is prime* / *91 is not prime —
  divisible by 7*.
- Accepts the full signed 64-bit range: −9,223,372,036,854,775,808 to
  9,223,372,036,854,775,807 (19 digits).
- Fully offline. No permissions, no network, no analytics, no accounts.
- Play-ready: signed App Bundle pipeline, store listing copy and artwork included.
- Works with system dark mode, dynamic colour (Android 12+), font scaling and
  screen readers.

## Requirements

| Tool | Version |
| --- | --- |
| Android Studio | A version that supports AGP 9 — Android Studio offers to update itself if yours is older |
| JDK | 17 or newer (CI uses 21; Android Studio's bundled JBR is fine) |
| Android SDK | Platform 36, Build-Tools 36.0.0 |
| Device / emulator | Android 7.0 (API 24) or newer, including Android 13 / API 33 |

Gradle 9.7.1 arrives via the wrapper, so you do not need Gradle installed.

## Open and run

1. **File → Open** in Android Studio and choose the repository folder. Do not use
   *New Project* — this is an existing project.
2. Wait for the first Gradle sync. It downloads Gradle 8.9, the Android Gradle
   Plugin and the AndroidX/Compose libraries, so it needs network access once.
3. Pick a device or emulator and press **Run** (Shift+F10).

The first sync also creates `local.properties` with your SDK path. That file is
git-ignored on purpose: it is machine-specific.

### From the command line

```bash
./gradlew :app:assembleDebug          # build a debug APK
./gradlew :app:installDebug           # build and install on a connected device
./gradlew :app:assembleRelease        # minified release APK (needs signing, see below)
```

The debug APK lands in `app/build/outputs/apk/debug/app-debug.apk`. Its
application id is `io.github.marcuspage5715.primechecker.debug`, so it can be
installed alongside a release build.

### The terminal front end

The original behaviour is still available, now as a thin wrapper over `:core`:

```bash
./gradlew :cli:run --args="97"
# 97 is prime

./gradlew :cli:installDist
./cli/build/install/prime-checker/bin/prime-checker     # interactive mode
```

Exit codes are unchanged from the Python original: `0` prime, `1` not prime,
`2` bad input. Running it inside Termux requires a JVM (`pkg install openjdk-17`),
which is heavier than the Python script was — the app is the intended interface
on a phone now.

## Installing on your phone without a PC

You do not need Play, a keystore or `adb` to run this on your own device.

**Easiest:** grab `prime-checker-debug.apk` from the
[Releases page](https://github.com/marcuspage5715/prime_number_app/releases). It is
attached to the `dev-build` release and downloads straight to a phone, no GitHub
login needed. The repository archive contains only source — an APK is a build
output, so it is never committed.

Alternatively CI builds a debug APK on every push; download it from the run's
`apks` artifact on the Actions tab. Either way, on the phone:

1. Open the `.apk` from your browser or files app.
2. Android asks whether to allow installs from that app; allow it once.
3. Play Protect may warn about an unknown developer. Choose **Install anyway**.

Two things worth knowing:

- It is a **debug** build, signed with a throwaway key that CI regenerates each
  run. Installing a newer build over an older one therefore fails with
  `INSTALL_FAILED_UPDATE_INCOMPATIBLE`: uninstall the old one first. The app
  stores nothing, so nothing is lost.
- The application id ends in `.debug`, so it can sit next to a future release
  build without a conflict.

When you want installable-by-you *release* builds, generate an upload key and add
the four signing secrets described in [PLAY_STORE.md](PLAY_STORE.md); that part is
only needed for distribution, not for your own phone.

## Project layout

```
app/        Android front end: Compose UI, ViewModel, theme, resources
core/       Pure Kotlin/JVM: parsing, primality, factor search  (no Android)
cli/        Optional terminal front end, reusing :core
docs/       Architecture notes, privacy policy, store artwork and screenshots
PLAY_STORE.md  Play Console submission guide
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the module graph, data flow
and the reasoning behind the main decisions.

## How the maths changed (and why)

The Python script divided `n` by every `6k ± 1` candidate up to `√n`. Exact, and
instant for small inputs — but a 19-digit number needs up to ~1.5 billion
divisions. On a phone that is a frozen UI and an ANR dialog, so the port had to
change while keeping the answers identical.

| | Python CLI | This app |
| --- | --- | --- |
| Primality | trial division to √n | deterministic Miller–Rabin |
| Worst case | ~10⁹ iterations | 12 modular exponentiations |
| 19-digit number | minutes, UI would freeze | milliseconds |
| Factor of a composite | not reported | smallest factor, budgeted search |
| Exactness | exact | exact (see below) |

**Deterministic, not probabilistic.** Miller–Rabin is usually described as
probabilistic. With the witness set {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37}
it is *proven* deterministic for every n < 3.3 × 10²⁴, which is ~350,000 times
larger than the largest 64-bit integer. Every value this app accepts therefore
gets a proof, not a probability. (That is also why the app does not use
`BigInteger.isProbablePrime`, which is honest about being probabilistic.)

**Bounded factor search.** A composite near 2⁶³ can have its smallest factor near
3 × 10⁹, so the factor search stops at 1,000,000 candidates' worth of divisors and
the UI says *"No prime factor up to 1,000,000"* rather than pretending. Primality
itself is never budgeted — it is always exact.

**Overflow safety.** Modular multiplication runs through `BigInteger`, so no
intermediate value can silently wrap a 64-bit integer. `isqrt` uses
division-only correction after a floating-point seed, so it is exact at the top
of the range.

## Testing

```bash
./gradlew :core:test :app:testDebugUnitTest :cli:test   # JVM, no device needed
./gradlew :app:recordRoborazziDebug                     # regenerate store screenshots
./gradlew :app:connectedDebugAndroidTest                # needs a device/emulator
./gradlew :app:lintDebug                                # Android lint
```

| Suite | Covers |
| --- | --- |
| `core/…/PrimalityTest` | every value −20…20,000 against the original algorithm, Carmichael numbers, strong pseudoprimes, perfect powers, 2⁶³ boundaries |
| `core/…/FactorSearchTest` | smallest factor −10…20,000 against naive division, budget limits, cancellation, exact `isqrt` |
| `core/…/NumberParserTest` | signs, whitespace, non-ASCII digits, 20-digit and 1,000,000-character input |
| `core/…/PrimeCheckerTest` | verdict mapping for primes, composites, values below 2 |
| `app/…/PrimeCheckerViewModelTest` | state transitions, truncation, clearing, error paths |
| `app/androidTest/…` | the screen and the real activity, typed into and tapped |
| `app/…/PrimeCheckerScreenshotTest` | renders the real UI on the JVM (Roborazzi + Robolectric) into 1080 x 1920 PNGs — the Play Store screenshots in `docs/screenshots/`, and a layout regression check |

The two algorithms are checked against each other on every small input, which is
what makes the port trustworthy: if either implementation broke, the dense-range
tests fail immediately.

## Signing and releasing

Passwords never belong in the repository. Copy the template and fill it in:

```bash
cp keystore.properties.example keystore.properties   # git-ignored
./gradlew :app:bundleRelease                          # app/build/outputs/bundle/release/
```

Without `keystore.properties` the release build still assembles — it is simply
unsigned, which is what the CI workflow does.

To let CI sign and publish instead, add four repository secrets
(`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) and push a
tag:

```bash
git tag v1.0.0 && git push origin v1.0.0
```

`.github/workflows/release.yml` then runs the tests and lint, builds a signed AAB
and APK, verifies both signatures with `jarsigner`, and attaches them plus the R8
mapping file to a GitHub release. The signing material is written to the runner's
temp directory and deleted in an `always()` step.

## Publishing to Google Play

[PLAY_STORE.md](PLAY_STORE.md) has the whole submission: store listing copy ready
to paste, the Data safety answers, content rating, privacy policy hosting on
GitHub Pages, the upload-key flow, and the closed-testing requirement for new
personal developer accounts. `docs/` holds the artwork Play asks for.

## Security posture

Short version: the app asks the user for no capabilities at all, so there is
nothing to protect.

One honest caveat, verified by decoding the built APK rather than the source
manifest: AndroidX Core injects a *signature-level* permission named
`<applicationId>.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. It protects AndroidX's
own internal broadcast receivers, is granted automatically to the app itself, is
never shown to the user, and cannot be requested by any other app. It is not a
capability — there is still no `INTERNET`, no storage, no contacts, no location.
The debug build additionally includes Compose's `PreviewActivity` from
`ui-tooling`, which is `debugImplementation` only and absent from release.

| Decision | Reason |
| --- | --- |
| No `INTERNET` permission, and no permission the user is ever asked for | The app cannot leak what it cannot send. The source manifest declares none; the merged manifest carries only one signature-level permission AndroidX Core declares for itself (see below). |
| No analytics, ads or crash SDKs | No third-party runtime, no data collection, nothing to disclose. |
| `android:allowBackup="false"` | A calculator's input history is not worth syncing to a cloud account. |
| Only one exported component (the launcher activity) | Nothing else is reachable from other apps. |
| Input length bounded before parsing | A pasted megabyte of digits costs nothing, and there is no parsing path an attacker can lengthen. |
| No reflection, no dynamic code loading, no `WebView` | Nothing to inject into; R8 can shrink the whole app. |
| Release build minified and resource-shrunk | Smaller binary, less surface. |
| Dependencies pinned exactly in `libs.versions.toml` | No `+` version ranges, no surprise upgrades; Dependabot proposes changes instead. |
| Signing secrets outside the repo | `keystore.properties` and `*.jks` are git-ignored. |
| Long computations are cancellable and off the main thread | No ANR; a superseded check is dropped rather than published. |

Full detail, including the threat model and how to report a problem:
[SECURITY.md](SECURITY.md).

## Version matrix

| Component | Version |
| --- | --- |
| Android Gradle Plugin | 9.3.2 (built-in Kotlin) |
| Gradle | 9.7.1 |
| Kotlin | 2.3.0 |
| Compose BOM | 2025.09.01 |
| compileSdk / targetSdk | 36 |
| minSdk | 24 (Android 7.0) |

`targetSdk` is 36 because Play has refused new apps below API 36 since
31 August 2026. It does not affect which devices can install the app: `minSdk`
24 plus a target of 36 still installs and runs normally on an Android 13 (API 33)
device. The target level is about which platform behaviours the app opts into.

## Not included (deliberately)

History and saved checks, favourites, prime-factorisation of the whole number,
and batch mode. Each would need storage or a wider UI, and none is needed to
answer "is this prime". The `:core` API is small enough that adding them later
is a UI change, not a rewrite.

## License

No licence has been chosen yet, so all rights are reserved by default. Add a
`LICENSE` file before publishing or accepting contributions.
