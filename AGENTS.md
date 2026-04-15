# Agent Instructions

## Android Device Install Safety

- Always install debug or release APKs to the primary user explicitly:
  `adb install --user 0 <apk>`
- Do not install APKs without `--user 0` on OPPO/ColorOS devices or any Android device with multiple users/profiles.
- Before diagnosing `INSTALL_FAILED_UPDATE_INCOMPATIBLE` or "signature conflict", check all users:
  `adb shell pm list users`
  `adb shell pm list packages -u | grep <package>`
  `adb shell dumpsys package <package>`
- OPPO/ColorOS may contain hidden users such as `system_clone` even when no visible clone space was created.
- If a package remains installed in a secondary/clone user, uninstall only that package for that user:
  `adb shell pm uninstall --user <user_id> <package>`
- Never remove an Android user/profile, such as `pm remove-user`, unless the user explicitly approves that exact action.
- Do not clear Google Play services, Play Store, app data, or device profiles without explicit user approval.

## Logging

- Any newly added log message must include the prefix `royrao: `.
- Do not log scanned QR/barcode content or other user data.

## Temporary Files

- Store generated, downloaded, or temporary artifacts under `._tmp/` at the project root.
