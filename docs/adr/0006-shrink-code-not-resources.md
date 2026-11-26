<!--
SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: Apache-2.0
-->

# ADR: Shrink Code, Not Resources

- Status: **Accepted**
- Date: 2026-11-27

## Context

Release builds use R8. The keyboard loads many resources by name at runtime (dictionaries, keyboard
layout sets, subtype names, spoken key descriptions, `!string/` references in layouts), which the
resource shrinker can't see.

## Decision

Enable R8 code shrinking with `-dontobfuscate` (native code looks classes up by name) and keep
resource shrinking off. Remove unused resources from the source instead, excluding anything that is
referenced from resources or looked up by name.

## Outcomes

### Positive Outcomes

- Release APK 82 MB to 54 MB with readable stack traces.

### Negative Outcomes

- Unused resources must be removed by hand.
