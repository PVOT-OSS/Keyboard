<!--
SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: Apache-2.0
-->

# ADR: Stay a Single-Module Fork

- Status: **Accepted**
- Date: 2026-11-27

## Context

Keyboard is a fork of LineageOS LatinIME. Upstream is one large app (Java, C++, XML) whose classes
call each other directly. The app template splits apps into `:app`, `:core:model` and `:core:data`.
Moving PVOT code out of `:app` would separate it from the upstream classes it hooks into and make
every upstream merge harder.

## Decision

Keep one `:app` module (plus the `:tools:make-keyboard-text` generator). Align only the build logic,
quality checks, CI, REUSE and PVOT-added Kotlin with the template. Upstream files keep their style;
Spotless and detekt only see Kotlin, which is all PVOT code.

## Outcomes

### Positive Outcomes

- Upstream merges stay mechanical.
- The build, checks and CI match every other PVOT app.

### Negative Outcomes

- No module boundaries enforce layering inside PVOT code; it relies on packages and review.
