# MS43 Multimap вЂ” FAQ & HOWTO (English)
**Firmware:** Siemens MS43, SW `MS430069` В· BMW E46 M54B25 (EU4, LHD) В· 512 KB flash
**System:** MODE 0 = STOCK В· MODE 1 = STAGE1 В· MODE 2 = STAGE3 вЂ” switchable on a parked car, no laptop needed.
**Version of this doc:** 2026-07-31 (matches image `MS430069_multimap_m11_all_cs_ok.bin` + XDF `MS430069_multimap_M2.xdf`, rev. v3)

> вљ пёЏ **Legal / safety notice.** ECU tuning can damage the engine, the catalyst, void warranties and break emissions laws. Everything below is a *starting point* for a healthy engine on the specified fuel вЂ” not a finished tune. Always validate with logging (lambda + knock), prefer a dyno. You flash at your own risk.

---

## 0. Package вЂ” what is what

| File | Purpose |
|---|---|
| `MS430069_multimap_m11_all_cs_ok.bin` | вњ… **The file you flash the first time.** Multimap switcher + MIL blinker, checksum already fixed (WinOLS). MD5 `56af213ec8a7c01ac2bdad305fbc6f69`. Slot 1 / Slot 2 in it are byte-identical **copies of stock** вЂ” the engine will run stock in every mode until you tune them. |
| `MS430069_multimap_M2.xdf` | вњ… **The TunerPro definiton you use for tuning.** Contains the full stock map set plus 66 new tables in folders **В«Slot 1 (STAGE1)В»** and **В«Slot 2 (STAGE3)В»** that point into the switchable map slots. |
| `multimap_map_names.md` | Glossary: plain-English table titles в†” original BMW/DAMOS names (for cross-referencing tuning guides that use BMW names). |
| `MS430069_stock.bin` | Original untouched stock binary вЂ” your ultimate rescue. Keep a copy offline. |
| `MS430069_512K.xdf` | Original community XDF (BASEOFFSET 0x70000) вЂ” **do NOT use it for tuning this multimap image** (see FAQ Q7). It is kept for reference only. |

Tooling you need:
- **MS4X Flasher** (full flash read/write, auto checksum) вЂ” https://ms4x.net
- **TunerPro RT / TunerPro** вЂ” map editing
- **WinOLS** *(optional)* вЂ” only if you don't use MS4X Flasher for checksum
- Any logger that can read MS43 over K-line (INPA / MS4X logging / custom ADX) вЂ” for knock & lambda validation

---

## 1. PART A вЂ” First flash (do this once)

1. **Read and save your current flash** with MS4X Flasher в†’ keep the dump forever.
2. **Verify the file** (optional but recommended): MD5 of `MS430069_multimap_m11_all_cs_ok.bin` must be
   `56af213ec8a7c01ac2bdad305fbc6f69` (any MD5 tool).
3. **Flash** `MS430069_multimap_m11_all_cs_ok.bin` with MS4X Flasher (it recalculates checksums automatically вЂ” the file already has them, so this is a double safety).
4. Ignition ON в†’ engine behaves exactly like stock (mode 0). If the car runs fine вЂ” continue.

---

## 2. PART B вЂ” Switching maps on the car

```
STOCK (0) в”Ђв”Ђв–є STAGE1 (1) в”Ђв”Ђв–є STAGE3 (2) в”Ђв”Ђв–є STOCK (0) в”Ђв”Ђв–є вЂ¦
```

1. Engine **OFF**, ignition **ON** (KL15). Switching is impossible while the engine runs вЂ” by design.
2. Press the accelerator **past 50% and HOLD it** (don't release early вЂ” the counter resets).
3. After **5 seconds** the mode advances one step. Keep holding to advance further (every +5 s = one step).
4. **Check Engine (MIL) confirms with a blink code** immediately after each step:

| MIL blinks (0.2 s ON / 0.2 s OFF) | Mode |
|---|---|
| в—Џ (one) | 0 вЂ” STOCK |
| в—Џ в—Џ (two) | 1 вЂ” STAGE1 |
| в—Џ в—Џ в—Џ (three) | 2 вЂ” STAGE3 |

5. Release the pedal. Start the engine and drive.

**Rules to remember**
- The series is played **once per switch only** (no reminders at key-on).
- No DTCs are stored by the blink routine вЂ” it's a clean CAN-bit animation, the same technique MS43X custom firmware uses.
- The real MIL state is masked for the duration of the series (в‰¤1.2 s). If MIL stays ON **after** the series вЂ” that's a genuine fault, read DTCs.
- **Cutting power/ignition resets the mode to STOCK.** Re-select after every ignition cycle (persistence is on the roadmap, not implemented).

### 2.1 Verify with live data (optional, ADX/K-line)

| RAM cell | Meaning |
|---|---|
| `0xF5FA` | mode word: `00 00` STOCK В· `01 01` STAGE1 В· `02 02` STAGE3 |
| `0xF5FC` | pedal-hold counter (counts to 50 = 5 s, then switches) |
| `0xF5FE` | blinker state: `0x81/0x82/0x83` during a series |
| `0xF5FF` | blink tick 0в†’20 |
| `0xE34A` | pedal position word, `0x4000` = 100 %, switch threshold `0x2000` |
| `0x807F` | engine-rpm byte (0 = engine off) |

---

## 3. PART C вЂ” Editing Slot 1 / Slot 2 in TunerPro

### 3.1 Setup

1. Open TunerPro в†’ **Load XDF:** `MS430069_multimap_M2.xdf`.
2. **Load BIN:** `MS430069_multimap_m11_all_cs_ok.bin` (must be 524 288 bytes).
3. In the table tree you will now see two extra folders: **В«Slot 1 (STAGE1)В»** and **В«Slot 2 (STAGE3)В»**. Together they contain 66 tables (33 maps Г— 2 slots). Titles end with `[SLOT 1]` / `[SLOT 2]`.

### 3.2 The golden rule

> **Only ever edit tables inside В«Slot 1 (STAGE1)В» and В«Slot 2 (STAGE3)В».**
> Every other table in the XDF addresses the *original* calibration area = your **MODE 0 (STOCK)**.
> Editing originals destroys your safe stock fallback and does *not* tune STAGE1/STAGE3.

Also: **do not edit axis (x/y) values.** Axes of slot tables are *linked* to the shared stock axis definitions. Changing them would silently affect stock tables too. Edit only the **Z cells** (the values inside the grid).

### 3.3 Units you type вЂ” no hex needed

TunerPro shows real engineering units, just type them:

| Table | Grid | Units shown | Change = |
|---|---|---|---|
| Ignition maps (all) | В°CRK (crank degrees) | `0.375В°` per raw step | e.g. +2.0 (В°) |
| VANOS intake maps | В°CRK | `+` = more intake advance | e.g. +2.0 |
| VANOS exhaust maps | В°CRK | **negative** values shown (e.g. в€’30.0) | less negative = more retard |
| Lambda - catalyst protection | О» (dimensionless) | e.g. `0.900` | 0.001 steps |
| Injection basic time | ms | | leave alone (see below) |
| Throttle/PWM/target opening | В° or % | | pedal-feel shaping |
| Torque model maps | Nm / В°PVS | | limits & requests |

### 3.4 STAGE 1 вЂ” recommended starter tune
*Target: stock internals, RON98/95+, intake + exhaust, street-driven.*

| # | Table (folder В«Slot 1В») | What to do | How much |
|---|---|---|---|
| 1 | **Ignition RON98 - part load** (20Г—16, В°CRK) | Advance only the **high-load cells** (top ~30 % of load axis) above ~2500 rpm. No changes in idle/cruise cells. | **+1.0 вЂ¦ +2.0 В°**, smooth transitions between cells (avoid jumps > 1вЂ“1.5 В°) |
| 2 | **Ignition optimal (TCO1)** and **(TCO2)** (16Г—12) | These cap the combustion optimum. Raise only top-load rows, matching your RON98 change. | **+0.75 вЂ¦ +1.0 В°** |
| 3 | **Lambda - catalyst protection** (10Г—12) | Only in the highest 2 columns (max load & rpm, where stock already adds enrichment): make the full-load mixture slightly richer. | О» **0.88вЂ“0.90** in those cells, elsewhere 100 % stock |
| 4 | **VANOS intake - full load (TCO1/TCO2)** | Intake advance at WOT moves torque curve earlier. Sweep on a dyno; street-safe start: | **+2 В°** across the FL row, verify with logs |
| 5 | **VANOS exhaust - full load (TCO1/TCO2)** | Less negative = more retard (less overlap). With stock exhaust keep stock or small retard; with headers test both directions. | start **В±0..2 В°**, dyno only |
| 6 | **Throttle - target opening (pedal)** *(optional)* | Sportier pedal: raise mid-pedal target angles slightly. | **+5 вЂ¦ +10 %** of cell value in the mid range, keep idle/WOT rows stock |

**Do NOT touch in STAGE 1:** Ignition RON91 *(safety fallback on bad fuel вЂ” leave stock)*, Ignition - basic map, cold-engine ignition, torque reserve maps, all torque model/limit maps, VANOS idle & part-load, Intake manifold volume (VIM), Calculated load (CLC), Injection basic time *(it's lambda-driven вЂ” basic time edits fight the O2 feedback)*, Injection pedal limitation, throttle PWM.

### 3.5 STAGE 2 (slot 2, labelled STAGE3) вЂ” guidance
*Slot 2 is meant for hardware builds: headers/cams/free exhaust, maybe raised load.* Treat it as your dyno slot:

- Ignition RON98 + optimal: **+2 вЂ¦ +4 В°** in the top area, **knock logging mandatory**, step 1 В° at a time.
- Lambda CP in top cells: **0.85вЂ“0.87** (protects pistons/catalyst under sustained load).
- VANOS FL intake/exhaust: full **dyno sweep В±5 В°** when cams are changed вЂ” there is no safe blanket number.
- **Torque max - correction**: raise only if the hardware genuinely exceeds stock torque; steps of **+10вЂ“20 Nm**, log for knock/misses.
- **CLC / Intake manifold volume (VIM)**: recalibrate only when intake hardware (throttle body, manifold, MAF path) actually changed вЂ” otherwise leave stock.

### 3.6 Golden workflow after every edit

```
edit Slot tables в†’ File в–ё Save Bin AsвЂ¦  (new name!) в†’ checksum в†’ flash в†’
KL15, switch to the tuned mode (see Part B) в†’ MIL blink confirms в†’
road test with logging (knock, lambda) в†’ iterate
```

1. **Save** as a new file, e.g. `MS430069_mm_stage1_v1.bin` (keep the base image untouched).
2. **Checksum.** If you write with **MS4X Flasher вЂ” it recalculates automatically**. If you use any other method (boot-mode writers etc.), fix checksums in **WinOLS** (the two checksum fields in this 512 K file live at `0x6FDAE..0x6FDB0` and `0x6FDE0..0x6FDE1`).
3. **Flash**, then perform the mode switch вЂ” MIL blinks confirm the mode.
4. **Validate on the first drive:** idle quality, full-throttle pull in 3rd gear with knock logging, lambda at WOT в‰€ commanded, no hesitation. If in doubt вЂ” switch back to mode 0 (STOCK).

### 3.7 Tuning hygiene

- One logical change per flash (ignition **or** VANOS **or** lambda) вЂ” otherwise you can't attribute knock.
- Log: knock retard per cylinder (INPA / MS4X logging), lambda, AFR, IAT, ignition timing actually delivered.
- Never let adjacent ignition cells differ by more than ~2вЂ“3 В° вЂ” the ECU interpolates; cliffs make weird artifacts.
- Cold-engine ignition maps are not in the slot set вЂ” cold behavior stays stock: good, keep it that way.
- Record the MD5 + change log of every flashed file (e.g. in a `changelog.md` next to the bins).

---

## 4. FAQ

**Q1. I flashed the base image and switched to STAGE1/STAGE3. Why did nothing change?**
Because slots 1 and 2 ship as byte-identical copies of stock вЂ” the first image proves the switcher safely. Real gains appear only after you tune Slot 1 / Slot 2 in TunerPro (Part C) and flash the tuned file.

**Q2. Can I switch modes while driving?**
No. The trigger checks the engine-off flag (rpm byte `0x807F` = 0). At any rpm the pedal hold does nothing. Switching while driving would be dangerous and is hard-blocked by design.

**Q3. Does the MIL blink write fault codes?**
No. The routine animates the shared cluster status byte directly in the CAN stream (same technique as MS43X custom firmware) and touches nothing in DTC memory.

**Q4. The mode resets when I turn the key off?!**
Yes вЂ” intentional. Every power cycle starts in MODE 0 (STOCK). Mode persistence (EEPROM-mirror) is a roadmap option, deliberately excluded for safety: if a tuned mode ever causes trouble, a key cycle always brings back stock.

**Q5. How do I know which mode I'm in without re-switching?**
At the moment: only via live data `0xF5FA` (00 00 / 01 01 / 02 02), or simply re-do the pedal hold вЂ” MIL will blink the new mode. A key-on reminder was explicitly declined in the requirements (blinker only at switch events).

**Q6. TunerPro shows nonsense in Slot tables: 72.0 everywhere / 255 / 65535.**
You opened a file whose free-flash area is `0xFF` (a bin that never got the slots written вЂ” e.g. the plain stock file or a different download). Math check: `0.375 Г— 255 в€’ 23.625 = 72.0`. Open **`MS430069_multimap_m11_all_cs_ok.bin`** вЂ” values will be sane (verified: intake cam в€’105.0В°, TPS cells like 2.000).

**Q7. Can I use my old community XDF (`MS430069_512K.xdf`) with this multimap bin?**
Don't mix. The M2 XDF uses BASEOFFSET **0** (all addresses physical), the community XDF uses BASEOFFSET **0x70000**. Loading the wrong pair shifts every address вЂ” you'd corrupt maps. One XDF в†” this package only.

**Q8. What happens if I edit original tables (outside Slot folders)?**
Mode 0 stops being stock. STAGE1/STAGE3 won't see your changes (they read their own copies). Rule: edits go to Slot folders only.

**Q9. Do I need WinOLS at all?**
No, if you flash with MS4X Flasher (auto checksum). WinOLS is only the fallback for fixing checksums after edits when using other flashing methods.

**Q10. How much timing can M54B25 take on 98 RON stock-ish?**
Community experience: +1вЂ¦2 В° in the high-load region of the RON98 map is the safe street zone; beyond that it's a dyno/knock-log decision, not a forum number. Follow В§3.4 and always log knock.

**Q11. Will richer WOT lambda (0.88вЂ“0.90) hurt the catalyst?**
Extra fuel burns cooler and is actually *more* protective for the catalyst at sustained high load (that's literally what the catalyst-protection map does in stock trim). It only costs a bit of fuel economy at WOT.

**Q12. Can I raise the rev limiter with this package?**
No вЂ” the rev-limit maps are deliberately **not** in the 33-map switchable set. Stage-1 usage keeps factory rev limits.

**Q13. Something feels wrong after tuning вЂ” what do I do?**
Switch to MODE 0 (STOCK) вЂ” if it persists, reflash your rescue stock dump (`MS430069_stock.bin`). Then review the last change against your logs.

**Q14. Can bricks happen?**
Full-flash writers boot-mode capable (MS4X Flasher) recover even a broken flash. Keep your stock dump safe. Never interrupt a write.

**Q15. Idle / cruise driveability after timing edits?**
Don't advance low-load cells вЂ” idle quality comes from the idle and low-load region; the +1..2 В° belongs to high load only, per В§3.4.

**Q16. Smoothness of edited maps?**
Select whole cell ranges and use TunerPro's *Increment by value* (`+` tool) so changes are uniform; then hand-smooth the boundary cells.

**Q17. Why do VANOS exhaust tables show negative values (e.g. в€’30.0 В°)?**
Because of the conversion law `в€’0.375В·X в€’ 60`: raw data increases в†’ value goes more negative. Type the displayed units exactly; don't try to outsmart the sign.

**Q18. Can I add more maps to the switchable set?**
Technically the free-flash area has ~9.8 KB left (в‰€ up to 50 maps) вЂ” possible as a future extension, requires a new dispatcher build + full re-verification. Not a TunerPro-only change.

**Q19. Does this work on other M54 variants (M54B22/B30)?**
The dispatcher/switcher is the same concept, but the base binary must be the **MS430069** M54B25 file. For other MS43 variants the free-area offsets and map addresses differ вЂ” would need their own build.

**Q20. Emissions/inspection?**
Stage 1 changes (small timing advance, WOT lambda slightly richer at load) don't touch the OBD monitors or DTC logic вЂ” the ECU runs stock diagnostics. Local laws still apply; you're responsible for road-legality.

---

## 5. Quick cheat sheet

```
FLASH BASE : MS4X Flasher в†’ MS430069_multimap_m11_all_cs_ok.bin  (MD5 56af213eвЂ¦c6f69)
SWITCH     : key ON, engine OFF в†’ pedal >50 % hold 5 s в†’ MIL в—Џ/в—Џв—Џ/в—Џв—Џв—Џ
TUNE       : TunerPro + MS430069_multimap_M2.xdf в†’ edit ONLY В«Slot 1В»/В«Slot 2В» tables
SAVE       : new filename each version
CHECKSUM   : MS4X Flasher auto вњ“ (fallback: WinOLS, fields 0x6FDAE.. / 0x6FDE0..)
FLASH      : в†’ switch to the tuned slot в†’ log knock & lambda on first pull
RESCUE     : MODE 0 = always stock В·  worst case: reflash MS430069_stock.bin
```

*Questions that appear during tuning: log first, then ask вЂ” В«knock? lambda? which cells?В» is the standard triage.*
