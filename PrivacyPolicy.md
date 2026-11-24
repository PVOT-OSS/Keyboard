<!--
SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: Apache-2.0
-->

# Pvot Keyboard privacy policy

Pvot Keyboard has no internet permission. Nothing you type, and nothing the keyboard learns,
is sent anywhere by the app. There are no ads, analytics, trackers or third-party services.

## What stays on your device

- **What you type** is processed on your device to show suggestions and corrections.
- **Learned words**: when "Personalised suggestions" is on, words and phrases you type are saved
  in the app's private storage to improve suggestions. You can delete them with Settings, Text
  correction, Clear learned words.
- **Blocked words** and your settings are saved in the app's private storage.
- **Personal dictionary**: words you add are stored in Android's personal dictionary, which other
  keyboards and spell checkers on your device can also read.
- **Clipboard**: with "Clipboard suggestions" on, the keyboard reads text you copied recently to
  offer it as a suggestion. Clips that apps mark as sensitive are shown masked. Clipboard text is
  not saved by the keyboard.
- **Contacts**: with "Suggest Contact names" on and the contacts permission granted, contact names
  are used for suggestions and spell checking. They are not saved outside the app's dictionary.

## Backups and files

- Android's device backup may include the app's settings, blocked words and learned words, and
  restore them on a new device. This backup is handled by Android and your backup provider.
- Export personal dictionary writes your personal dictionary to a file you choose. Import reads a
  file you choose. The app only accesses files you pick.

## Permissions

| permission | why |
|---|---|
| `READ_CONTACTS` | suggest contact names (optional, asked when you turn the setting on) |
| `READ_USER_DICTIONARY`, `WRITE_USER_DICTIONARY` | use, export and import your personal dictionary |
| `VIBRATE` | vibrate on key press |
| `RECEIVE_BOOT_COMPLETED` | apply the "show app icon" setting after the phone starts |
