<!--
SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: Apache-2.0
-->

# ADR: One View Model for the Settings

- Status: **Accepted**
- Date: 2026-11-27

## Context

The settings are eleven small screens (Preferences, Appearance, Theme and others) that all read the
same preferences snapshot and share the same dialogs (choice sheet, slider, confirmation).

## Decision

One `SettingsViewModel` exposes `StateFlow<UiState<SettingsSnapshot>>`, takes
`onEvent(SettingsEvent)` (grouped into `Write` and `Action`) and sends `SettingsEffect`s through a
channel. The setup wizard has its own view model.

## Outcomes

### Positive Outcomes

- One snapshot, one effect channel, no duplicated loading across screens.

### Negative Outcomes

- Differs from the template's one view model per screen.
