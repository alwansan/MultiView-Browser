# Security / الأمان

**Report sensitive security issues privately to the repository owner rather than posting exploit details, credentials or logs with private sessions in a public issue.**

- MultiView is a native Android WebView application, not a security-hardened independent browser engine.
- Normal panes share the standard WebView cookie profile; this app does **not** provide per-pane private accounts or a strong incognito isolation guarantee.
- The experimental website-darkening feature uses page JavaScript/CSS injection to alter appearance. It should not be treated as a content security feature.
- HTTPS content and WebView security are subject to the installed Android System WebView version.
- App signing keys must remain private in GitHub Actions secrets; see [safe signing setup](docs/SIGNING_SETUP_AR.md).
- Never upload cookies, authentication headers, session URLs, user files or personal chats in issues, artifacts or screenshots.
- Android may reclaim WebView processes and destroy unsubmitted web-page state, even with an opt-in foreground service.

If you believe a public security report can be safely discussed without sensitive data, provide minimal reproduction steps and the build SHA only.
