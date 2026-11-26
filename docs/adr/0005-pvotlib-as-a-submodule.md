<!--
SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: Apache-2.0
-->

# ADR: PvotLib as a Submodule

- Status: **Accepted**
- Date: 2026-11-27

## Context

The template consumes PvotLib 1.0.0 from its Maven repository. Keyboard uses components added after
1.0.0 (`PvotSubScreen`, `PvotChoiceSheet`, `PvotSwitch`).

## Decision

Keep the `PvotLib` git submodule with `includeBuild` and dependency substitution until a PvotLib
release contains those components, then switch to Maven like the template. AGP stays on the
version PvotLib builds with (9.5.0), because an included build can't mix AGP versions.

## Outcomes

### Positive Outcomes

- Keyboard can use PvotLib changes before they are released.

### Negative Outcomes

- CI must check out submodules; AGP lags the template until the switch.
