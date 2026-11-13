# Publishing to Google Play

Everything below is what the Play Console will ask for, in the order it asks for
it, with the answers this app should give. Nothing here needs new code — the
project is already built for it.

## Before you start

- A Google Play developer account (one-off US$25 fee).
- The app targets **API 36**. Play has refused new apps below API 36 since
  31 August 2026, so do not lower `targetSdk`.
- `minSdk` is 24, so the app installs on Android 7.0 and newer, including an
  API 33 device. The target level selects platform behaviours; it does not limit
  which devices can install the app.

## 1. Create the app

Play Console → **Create app**. Name `Prime Checker`, default language English,
type **App**, **Free**. Accept the developer programme policies and the US export
declaration.

## 2. Store listing

Copy and paste from here.

**App name** (30 characters max)

```
Prime Checker
```

**Short description** (80 characters max)

```
Is it prime? Check any whole number instantly - offline, no permissions.
```

**Full description** (4000 characters max)

```
Prime Checker answers one question, quickly and exactly: is this whole number prime?

Type a number, tap Check, and get a straight answer. When the number is not
prime, the app tells you the smallest prime that divides it, so the result is
something you can verify by hand.

WHAT IT DOES

- Checks any whole number up to 19 digits, the full signed 64-bit range.
- Reports the smallest prime factor of composite numbers.
- Explains the edge cases: numbers below 2 are not prime, and prime numbers
  start at 2.
- Shows how long the check took, usually a few milliseconds.

EXACT, NOT PROBABILISTIC

The app uses a deterministic Miller-Rabin test. With its fixed set of witnesses
the answer is a mathematical proof for every number the app accepts, not a
probability. There is no "probably prime" here.

PRIVATE BY DESIGN

- No permissions at all. Not even internet access.
- No accounts, no sign-in, no ads, no analytics, no trackers.
- Nothing you type is stored or sent anywhere. Close the app and it is gone.
- Works completely offline, in airplane mode, on a plane.

WHO IT IS FOR

Students checking homework, anyone curious whether a big number is prime, and
people who want a calculator that respects them.

SMALL AND HONEST

The whole app is a few hundred kilobytes. It does one thing and says so. No
subscription, no upsell, no feature you did not ask for.

Prime numbers start at 2. Everything else is divisible by something.
```

**App icon**: `docs/play_store_icon.png` (512 × 512).
**Feature graphic**: `docs/feature_graphic.png` (1024 × 500).
**Phone screenshots**: at least 2, up to 8. Capture them from a device or
emulator (see "Screenshots" below); each must be between 320 and 3840 px on a
side, with an aspect ratio between 16:9 and 9:16.

## 3. Data safety

Play requires a Data safety declaration for every app. For this one the answers
are short:

| Question | Answer |
| --- | --- |
| Does your app collect or share any of the required user data types? | **No** |
| Is all user data encrypted in transit? | Not applicable — no data leaves the device |
| Do you provide a way to request data deletion? | Not applicable — nothing is stored |
| Does your app use advertising or tracking? | **No** |

Result: "No data collected, no data shared".

## 4. Content rating

Run the IARC questionnaire. It is a utility with no user-generated content, no
violence, no sexual content, no profanity, no controlled substances, no gambling,
no ads and no user interaction. Expect **Everyone / PEGI 3**.

## 5. Target audience and other declarations

| Section | Answer |
| --- | --- |
| Target age group | **13+** (choosing under-13 pulls the app into the Families policy programme, which is extra work for a calculator) |
| Is this a news app? | No |
| Does it contain ads? | No |
| COVID-19 contact tracing or status app? | No |
| Government app? | No |
| Financial features? | No |
| Health features? | No |
| All functionality available without special access? | Yes — no login, no credentials to provide |

## 6. Privacy policy

Play requires a privacy policy URL for every app, even one that collects nothing.
`docs/privacy-policy.md` is written and ready. To publish it free on GitHub
Pages:

1. Push this repository (it is already on `main`).
2. Repository **Settings → Pages → Build and deployment → Source: Deploy from a
   branch**, branch `main`, folder `/docs`, then **Save**.
3. After a minute the policy is live at
   `https://marcuspage5715.github.io/prime_number_app/privacy-policy.html`.

Paste that URL into Play Console → **App content → Privacy policy**.

## 7. Signing and the upload bundle

Play distributes an **Android App Bundle (.aab)**, not an APK, and requires Play
App Signing (on by default for new apps). You keep an *upload key*; Google holds
the app signing key.

Create the upload key once, locally, and never commit it:

```bash
keytool -genkeypair -v \
  -keystore upload-keystore.jks -alias upload \
  -keyalg RSA -keysize 4096 -validity 10000
```

Then either build locally:

```bash
cp keystore.properties.example keystore.properties   # fill in, it is git-ignored
./gradlew :app:bundleRelease
# app/build/outputs/bundle/release/app-release.aab
```

or let CI build it. Add four repository secrets under **Settings → Secrets and
variables → Actions**:

| Secret | Value |
| --- | --- |
| `KEYSTORE_BASE64` | output of `base64 -w0 upload-keystore.jks` |
| `KEYSTORE_PASSWORD` | the store password |
| `KEY_ALIAS` | `upload` |
| `KEY_PASSWORD` | the key password |

Then push a tag:

```bash
git tag v1.0.0 && git push origin v1.0.0
```

`.github/workflows/release.yml` runs the tests and lint, builds a **signed** AAB
and APK, verifies both signatures with `jarsigner`, and attaches them plus the R8
`mapping.txt` to a GitHub release. Upload the `.aab` from there to Play Console.

## 8. Testing track before production

New **personal** developer accounts must run a closed test with at least **12
testers who opted in, for 14 continuous days**, before applying for production
access. Organisation accounts are exempt. Plan for it:

1. **Testing → Internal testing**: add yourself, install from the link, confirm
   the app works on your device.
2. **Testing → Closed testing**: add 12+ testers by email list, keep them opted
   in for the full 14 days.
3. **Production → Apply for access**, then roll out.

## 9. Every subsequent release

1. Bump `versionCode` (must increase; `versionName` is cosmetic) in
   `app/build.gradle.kts`.
2. Commit, then `git tag v1.0.1 && git push origin v1.0.1`.
3. Upload the new `.aab` to Play Console.

Keep `mapping.txt` from each release: Play Console accepts it to deobfuscate
crash reports, and CI attaches it to the GitHub release for you.

## Screenshots

Capture at least two, ideally four: an empty state, a prime result, a composite
result with its factor, and an invalid input. From a device:

```bash
adb shell screencap -p /sdcard/shot.png && adb pull /sdcard/shot.png
```

Play prefers 1080 × 1920 or larger. Dark mode screenshots are a nice touch, since
the app follows the system theme.

## Things that do not apply here

- **16 KB page size** (Play deadline 1 February 2027): only affects apps with
  native libraries. This app ships none.
- **Ads declaration, families policy, financial/health declarations**: not
  applicable, as answered above.
- **Data deletion requests**: nothing is stored, so there is nothing to delete.
