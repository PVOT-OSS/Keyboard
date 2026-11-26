<!--
SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: Apache-2.0
-->

# ADR: Create View Models Without Hilt

- Status: **Accepted**
- Date: 2026-11-27

## Context

The template uses Hilt. Keyboard has two view models (settings and setup) and no other injection
needs; Hilt would also need an `@HiltAndroidApp` application class around upstream code.

## Decision

Create view models by hand with `viewModel { }` and `viewModelFactory { }`, passing repositories
built from the application context. Repositories sit behind interfaces so tests use fakes.

## Outcomes

### Positive Outcomes

- No annotation processing, no generated code, no Application change in a fork.

### Negative Outcomes

- Factories are written by hand; revisit if the app grows many more screens.
