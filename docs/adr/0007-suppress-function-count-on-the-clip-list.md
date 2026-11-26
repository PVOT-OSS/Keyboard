<!--
SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: Apache-2.0
-->

# ADR: Suppress the Function Count on the Clip List

- Status: **Accepted**
- Date: 2026-11-27

## Context

detekt's `TooManyFunctions` (11 per class) flags `ClipboardHistory` (one list of clips; every edit
works on the same private items) and `ClipboardHistoryManager` (the synchronized facade upstream
Java calls).

## Decision

Suppress `TooManyFunctions` on these two classes with a one-line reason. No other suppressions and
no detekt baseline.

## Outcomes

### Positive Outcomes

- The clip list stays one cohesive class.

### Negative Outcomes

- Two documented exceptions to the template's detekt rules.
