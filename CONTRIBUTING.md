# Contributing

## Setup

1. Install Android Studio (Ladybug or newer) with SDK Platform 35.
2. Open the repository folder, let Gradle sync.
3. Run the JVM test suites before you start: `./gradlew test`.

## Ground rules

- **Logic belongs in `:core`.** If it can be computed without `Context`, it goes
  there and gets a unit test. `:app` should stay thin enough to read in one
  sitting.
- **Keep the maths exact.** A faster algorithm is welcome only with a test that
  pins it against the slow, obviously-correct one on a dense range.
- **No new dependencies without a reason.** Every library is code that runs
  in-process. Say what it buys in the pull request.
- **No permissions.** If a change seems to need one, open an issue first: it is a
  product decision, not an implementation detail.
- **User-visible strings go in `res/values/strings.xml`**, never inline in Kotlin,
  so they stay translatable.

## Before opening a pull request

```bash
./gradlew :core:test :app:testDebugUnitTest :cli:test
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
```

All three must pass, and CI runs the same commands.

## Style

Kotlin official code style, which is the Android Studio default. 4-space
indent, trailing commas in multi-line argument lists,
`camelCase` for functions and `PascalCase` for types. Public API in `:core` gets a
KDoc comment explaining *why*, not just *what*.

## Commit messages

Short imperative subject line ("Bound the factor search budget"), blank line,
then the reasoning. Reference an issue number when there is one.
