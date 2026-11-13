# Security policy

## Supported versions

| Version | Supported |
| --- | --- |
| 1.0.x | Yes |
| < 1.0 | No |

## Reporting a vulnerability

Please do not open a public issue for a security problem. Use GitHub's private
vulnerability reporting (**Security → Report a vulnerability** on this
repository) or email the maintainer. Include the app version, Android version,
device, and the steps that reproduce the issue. You can expect an
acknowledgement within a week.

## Threat model

The app is a single-screen calculator with no accounts, no storage and no
network access. That makes most of the usual mobile attack surface simply
absent, and the model below is deliberately narrow.

**What an attacker could try**

| Attempt | Outcome |
| --- | --- |
| Intercept or tamper with traffic | Nothing to intercept: the app has no `INTERNET` permission, so the OS refuses sockets at the kernel boundary. |
| Steal data at rest | Nothing is stored. There is no database, no preferences file, no cache of user input, and backups are disabled. |
| Read input from another app | The only exported component is the launcher activity, which takes no intent extras. Input stays in memory. |
| Freeze the app with a hostile input | Input is length-bounded before parsing (19 digits) and the factor search is budgeted and cancellable; a superseded computation is dropped. |
| Install a tampered update | Standard Android signature enforcement applies; use Play App Signing and keep the upload key off any machine that does not need it. |
| Exploit a dependency | The runtime dependency set is AndroidX/Compose only, pinned exactly, and updated via Dependabot and review. |

**What is explicitly out of scope**

- Physical access to an unlocked device.
- A rooted or otherwise compromised OS.
- Side-channel timing analysis of the primality test. It is not a secret-bearing
  operation: there is no key, and the input is the user's own.

## Design decisions that matter for security

1. **Zero permissions.** The manifest contains no `<uses-permission>` element. If
   a future change needs one, treat it as a security review item, not a detail.
2. **No third-party runtime SDKs.** No analytics, ads, crash reporting or
   attribution libraries. Each one would add code that runs in-process and
   collects something.
3. **Input treated as hostile.** Bounded length, ASCII digits only, explicit
   range check, no `eval`-style parsing, and no exceptions used for control flow
   in the UI.
4. **Exact arithmetic over fast arithmetic.** Modular multiplication uses
   `BigInteger` rather than 64-bit tricks, so there is no silent overflow that
   could make a composite look prime — a correctness bug that would also be a
   trust bug.
5. **Release hardening.** `isMinifyEnabled`, `isShrinkResources`, no debug
   suffix, no logging of user input, and R8 keep rules kept to a minimum (none
   are needed today).
6. **Secrets stay out of git.** `keystore.properties`, `*.jks`, `local.properties`
   and IDE files are git-ignored; CI builds unsigned artifacts.
7. **Reproducible dependencies.** Versions are pinned in
   `gradle/libs.versions.toml`; no dynamic versions anywhere.

## Supply chain

- The Gradle wrapper jar is the official file from the Gradle project, tag
  `v8.9.0`, and its SHA-256 is recorded in `CHECKSUMS.txt` at the repository
  root. Verify it with `sha256sum -c CHECKSUMS.txt` before trusting a fresh
  clone on a new machine; CI runs the same check on every push.
- Dependencies resolve from `google()` and `mavenCentral()` only; the project
  declares `RepositoriesMode.FAIL_ON_PROJECT_REPOS` so a module cannot quietly
  add its own repository.
- `.github/dependabot.yml` proposes weekly updates for Gradle and GitHub Actions
  so pinned versions do not become stale versions.

## Reviewing a change

Before merging anything that touches `app/src/main/AndroidManifest.xml`,
`app/build.gradle.kts`, `gradle/libs.versions.toml` or the `dependencies` blocks,
check that: no permission was added, no new repository was added, no version
range was introduced, and `./gradlew :app:lintDebug` still passes.
