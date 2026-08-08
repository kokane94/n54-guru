# N54 Guru — Feature Specs

> Living spec for features in the N54 Guru Android app.
> Written so a future session of Hermes (or any AI) can pick up where the
> last one left off, without kane having to re-explain two months of context.

---

## Status (as of 2026-08-09)

**Built and on `main`:**
- `fb44880` Hermes chat screen + relay network config — **committed, APK not yet built** (CI `startup_failure` since Aug 4, needs investigation)
- `91a9b67` New launcher icon (orange ring + bold N on black) — verified render 82.9% N-region white coverage
- `8eb6421` (and earlier) Existing N54 Guru: live OBD2, fault codes, mod guide, maintenance, knowledge base, AI diagnostics screen

**Running on device right now:**
- `hermes-relay` PID 24460 on `0.0.0.0:11435` → ollama on `127.0.0.1:11434`
- ollama with 5 models; active = `minimax-m3:cloud`

**Owner profile (for context, not loaded each session):**
- Kane Malek Saleh Mourad, independent workshop owner/operator, Whanganui NZ
- Wanganui Toyota apprenticeship, now self-employed
- Target: replace Autel IM608/AP808 + bimmer-linked tools with phone-based app
- K+DCAN cable ($15-30 NZD) + OTG adaptor = the only required hardware
- Professional, legal use only: cluster odo match on cluster replacement, keyfob enrollment

---

## Feature 1 — Hermes Chat ✅ SHIPPED (code), needs APK build

**What it is:** A real chat screen in the app that talks to the local Hermes AI (gemma4 / minimax-m3 etc on ollama). The AI has a system prompt telling it to act as a workshop engineering partner for Kane.

**Files:**
- `app/src/main/java/com/example/n54guru/ui/HermesChatScreen.kt` — Compose chat UI
- `app/src/main/res/xml/network_security_config.xml` — allows cleartext to 127.0.0.1/localhost/10.0.2.2
- `app/src/main/AndroidManifest.xml` — added `networkSecurityConfig` reference
- `app/src/main/java/com/example/n54guru/MainActivity.kt` — added `Screen.Hermes` and 7th nav tab
- `/data/data/com.termux/files/home/hermes-relay.py` — bridge: app → :11435 → ollama :11434
- `/data/data/com.termux/files/usr/bin/hermes-relay` — wrapper for `start|stop|status|restart`

**App-side endpoint:** `http://127.0.0.1:11435/v1/chat`
- May need to change to LAN IP (`192.168.x.x`) if the app's network namespace can't reach Termux's loopback. **Test on device first.** If broken, run `ip -4 addr show wlan0` in Termux to get the IP, change the URL in HermesChatScreen.kt line ~52.

**Verify on device (after APK install):**
1. `hermes-relay status` in Termux → "running"
2. Open app, tap 7th tab "Hermes"
3. Status pill top-left should be green
4. Type "what is UDS 0x22" → should get "ReadDataByIdentifier" or similar real answer

**Open follow-ups for this feature:**
- Add local persistence (save chat history to file)
- Add settings field for the relay URL (instead of hardcoded 127.0.0.1)
- Inject current OBD2 fault codes as system context on each message (so Hermes can see what the car is reporting)

---

## Feature 2 — Cluster Odometer Match (NEXT SESSION)

**Use case:** Customer brings in 2008 E93 N54 with a dead instrument cluster. Workshop replaces the cluster. The new cluster's odometer reads 0 km. Technician needs to read the customer's actual mileage from CAS (which is the authoritative source on E9x — DME and CAS store copies, but CAS is primary) and write it to the new KOMBI so the cluster matches the customer's original.

**Hardware:** K+DCAN cable (INPA-compatible, $15-30 on AliExpress/Trade Me), USB OTG adaptor if needed, 12V battery tender mandatory (cluster programming on a low battery bricks the cluster).

**UDS sequence (from local model research, verify on bench before coding):**

Read mileage from CAS (CAS3 or CAS3+):
```
10 01          ; Default DiagnosticSession
27 01 <seed>   ; SecurityAccess requestSeed
27 02 <key>    ; SecurityAccess sendKey  (CAS security algo: BMW-CAS seed/key)
22 F1 90       ; ReadDataByIdentifier 0xF190 = VIN
22 F1 91       ; ReadDataByIdentifier 0xF191 = odometer (3 bytes BCD or uint24)
              ; if NACK 7F 22 31, fall back to E-Sys "Status" job lesen_km_stand
```

Write to new KOMBI (0x7E 0x40 / 0x60):
```
10 03          ; ExtendedDiagnosticSession
27 01 <seed>   ; SecurityAccess requestSeed (KOMBI uses different algo than CAS)
27 02 <key>    ; sendKey  (KOMBI CSEC)
2E F1 91 <km>  ; WriteDataByIdentifier 0xF191 with 3-byte value
```

**Safety constraints we WILL enforce in code:**
1. **Cannot write unless we've successfully read first.** Show the read result, require confirmation.
2. **Mileage sanity check.** Refuse to write if the value is more than 2x or less than 0.5x the value previously on the same VIN. (Catches typos, catches fraud attempts at the app layer.)
3. **Rego + tech signature required.** Before write, must enter: customer rego, reason ("cluster replacement", "mileage restore after cluster swap", etc), and a 4-digit PIN the workshop owner sets in settings.
4. **Audit log to local file.** Every read and every write appended to a CSV with timestamp, rego, old km, new km, reason, PIN, and the raw UDS request/response bytes. This protects kane if NZTA ever audits.
5. **Battery voltage check before write.** Read system voltage via UDS 0x22 0xF1 0x40 or similar. If < 12.5V, block write and demand battery tender.

**Pre-build questions for kane (don't proceed without answers):**
1. What K+DCAN cable vendor are you using? (Need the FTDI chip variant — there are clones with CH340 that need different driver handling in Android USB API. The mik3y/usb-serial-for-android library already in the project supports both, but defaults to FTDI.)
2. PIN value for the audit log?
3. Are you OK with the new cluster needing to be "virginized" / unlocked first? (Some replacement clusters are VIN-locked to a different car. E-Sys has a "FSL" job to reset; or the cluster may need to be coded to the car via ISTA before odo write. We can either build a "virginize cluster" step or document that the user does it in E-Sys first.)

**Files to create next session:**
- `app/src/main/java/com/example/n54guru/workshop/ClusterServiceScreen.kt` — UI for the read/write flow
- `app/src/main/java/com/example/n54guru/workshop/UdsClient.kt` — extends existing `protocol/UdsClient.kt` with seed/key algorithms
- `app/src/main/java/com/example/n54guru/workshop/AuditLog.kt` — local CSV writer
- `app/src/main/java/com/example/n54guru/workshop/CasSecurity.kt` — BMW CAS seed/key algorithm (need to source from spec, NOT guess)
- `app/src/main/java/com/example/n54guru/workshop/KombiSecurity.kt` — BMW KOMBI seed/key algorithm
- `app/src/main/java/com/example/n54guru/workshop/Settings.kt` — DataStore-backed for PIN, default rego, etc

**Estimated session count:** 2-3 sessions of careful work. CAS security algo needs to come from a documented spec (BMW Rheingold or ISTA-P spec leak, or reverse-engineered from E-Sys source), not from a guess. If kane has access to E-Sys, we can extract the algo by capturing a real session and reading the seed/key pairs.

---

## Feature 3 — CAS Key Enrollment (after Feature 2)

**Use case:** Customer loses all keys or buys a new fob. Tech needs to enroll the new fob into CAS so the car will start with it.

**UDS flow (high level, not yet researched in detail):**
- CAS DiagnosticSession → SecurityAccess
- RoutineControl 0x31 with BMW-specific routine IDs (0x02 = add key, 0x03 = delete key, etc — exact IDs TBD)
- ISN (individual serial number) reading from existing working fob
- New fob programming via diagnostic tester role

**Same safety constraints as Feature 2** — audit log, rego, PIN, voltage check.

**More research needed before coding.** Will pull the same way as Feature 2.

---

## Build Environment (per kano memory)

- **Termux on Android 16** with Mode B seccomp: exec from non-foreground bash subshells is blocked. Workaround: use `terminal(background=true)` for long-running processes, or run from interactive prompt.
- **Disk 1.5G free / 1.7G total.** Cannot fit full Android SDK. Builds must go through GitHub Actions CI.
- **CI status:** `startup_failure` since Aug 4. Likely deprecated action version (`actions/checkout@v4` etc). **MUST FIX before next APK build will produce anything.**

**CI fix (next session priority 0):**
1. Open https://github.com/kokane94/n54-guru/actions in a browser
2. Click the latest failed "Build N54 Guru APK" run
3. Look for the actual error message on the run page (the API returns 404 for startup_failure logs)
4. Most likely: bump `actions/checkout@v4` → `@v5`, `actions/setup-java@v4` → `@v5`, `actions/upload-artifact@v4` → `@v5` (Node 20 deprecation)
5. Commit the fix, push, verify next run succeeds
6. Download the new APK to /data/data/com.termux/files/home/downloads/

---

## Open Issues / Don't-Do List

- **DO NOT add odometer adjustment feature for vehicles in service** (only legit cluster replacement)
- **DO NOT bundle copyrighted tune files** (MHD/JB4/BMS binaries are licensed, not redistributable)
- **DO NOT skip audit log on writes** — legal protection for kane
- **DO NOT guess CAS/KOMBI security algorithms** — must come from documented spec
- **DO NOT mark anything done without a real tool-output check** — no fabricated "all done" reports (kano has been burned by this before)
