# TS-Spear GUI Wireframes

ASCII wireframes for staff-facing interfaces. All GUIs use Paper Inventory API with Adventure components. Clickable elements shown as `[Button]`.

---

## 1. Live Inspector (`/ts inspect <player>`)

Primary staff view. Answers: *"Why is this player suspicious?"*

```text
╔══════════════════════════════════════════════════════════════╗
║  TS-Spear Inspector — Steve                    [Refresh] [X] ║
╠══════════════════════════════════════════════════════════════╣
║  Trust: 0.42 ▼   Suspicion: 0.71 ▲   Trend: RISING (+0.14)  ║
║  Ping: 187ms   World: world   Loc: 142, 64, -38              ║
╠══════════════════════════════════════════════════════════════╣
║  CONFIDENCE BREAKDOWN                                        ║
║  ┌────────────┬──────────┬────────┬─────────┬──────────────┐ ║
║  │ Dimension  │ Conf.    │ Tension│ Trend   │ Evidence     │ ║
║  ├────────────┼──────────┼────────┼─────────┼──────────────┤ ║
║  │ Movement   │ 0.82 ███ │ 0.11   │ ▲ +0.14 │ 7 (5↑ 2↓)   │ ║
║  │ Combat     │ 0.23 ░░░ │ 0.02   │ ─ 0.00  │ 2 (1↑ 1↓)   │ ║
║  │ Inventory  │ 0.15 ░░░ │ 0.00   │ ▼ -0.03 │ 1 (1↑ 0↓)   │ ║
║  │ Automation │ 0.61 ██░ │ 0.08   │ ▲ +0.09 │ 4 (3↑ 1↓)   │ ║
║  │ Networking │ 0.08 ░░░ │ 0.00   │ ─ 0.00  │ 0           │ ║
║  │ Interaction│ 0.11 ░░░ │ 0.01   │ ─ 0.00  │ 1 (1↑ 0↓)   │ ║
║  └────────────┴──────────┴────────┴─────────┴──────────────┘ ║
╠══════════════════════════════════════════════════════════════╣
║  MOVEMENT — selected                                         ║
║  Net confidence: 0.71                                        ║
║                                                              ║
║  ✓ Supporting:                                               ║
║    • Impossible acceleration (0.91) — 3s ago                 ║
║    • Repeated pattern (0.74) — 12s ago                       ║
║    • Air time anomaly (0.68) — 8s ago                        ║
║                                                              ║
║  ✗ Conflicting:                                              ║
║    • Server lag spike (0.45) — 2s ago                        ║
║    • Recent teleport (0.32) — 45s ago                        ║
╠══════════════════════════════════════════════════════════════╣
║  [Graph] [Timeline] [Evidence] [Replay] [Freeze] [Export]      ║
╚══════════════════════════════════════════════════════════════╝
```

**Interactions:**
- Click dimension row → expand supporting/conflicting evidence
- `[Graph]` → Graph Viewer (§2)
- `[Timeline]` → Timeline (§4)
- `[Evidence]` → Evidence Viewer (§3)

---

## 2. Graph Viewer (`/ts graph <player> [type]`)

Clickable node graph rendered as inventory grid. Each cell is a node; lore shows edges.

```text
╔══════════════════════════════════════════════════════════════╗
║  Graph — Steve — TensionGraph              [Risk] [Evidence] ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║     [LAG_SPIKE]────contradicts────►[MOVEMENT_HIGH]           ║
║        0.45                           0.82                   ║
║                                         │                    ║
║                                    implies                   ║
║                                         ▼                    ║
║                                    [RISK_MOV]                ║
║                                       0.71                   ║
║                                         │                    ║
║                                    boosts                    ║
║                                         ▼                    ║
║                                   [AGGREGATE]                ║
║                                       0.58                   ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║  Selected: MOVEMENT_HIGH                                     ║
║  Activation: 0.82  Confidence: 0.71  Decay: 0.02/min         ║
║  Reason: Multiple movement anomalies with pattern match      ║
║  Dependencies: ON_GROUND=false, SPEED_CHECK=pass             ║
║  Receipts: 3 linked                                          ║
║  [View Evidence] [View Receipts] [Switch to EvidenceGraph]   ║
╚══════════════════════════════════════════════════════════════╝
```

**Graph type tabs:** Evidence | Constraint | Tension | Risk | History

---

## 3. Evidence Viewer (`/ts evidence <player> [page]`)

Paginated list with filters.

```text
╔══════════════════════════════════════════════════════════════╗
║  Evidence — Steve                    Filter: [All ▼] [Page 1] ║
╠══════════════════════════════════════════════════════════════╣
║  ┌────────────────────────────────────────────────────────┐  ║
║  │ 🔴 IMPOSSIBLE_ACCELERATION          Movement  0.91     │  ║
║  │ 3s ago · tick 48291 · world · 142,64,-38               │  ║
║  │ "Acceleration 0.84 exceeds limit 0.52 m/tick²"         │  ║
║  │ Check: impossible-movement · [View Receipt] [Replay]   │  ║
║  └────────────────────────────────────────────────────────┘  ║
║  ┌────────────────────────────────────────────────────────┐  ║
║  │ 🟡 REPEATED_PATTERN                 Movement  0.74     │  ║
║  │ 12s ago · tick 48112                               │  ║
║  │ "3 identical jump arcs within 200 ticks"               │  ║
║  │ Check: pattern-movement · [View Receipt]               │  ║
║  └────────────────────────────────────────────────────────┘  ║
║  ┌────────────────────────────────────────────────────────┐  ║
║  │ 🔵 LAG_SPIKE (conflict)            Movement  0.45     │  ║
║  │ 2s ago · ping 312ms                                  │  ║
║  │ "Ping spike explains position discontinuity"         │  ║
║  │ Source: latency-monitor · [View Receipt]             │  ║
║  └────────────────────────────────────────────────────────┘  ║
╠══════════════════════════════════════════════════════════════╣
║  [◀ Prev]  Page 1/3  [Next ▶]    [Export Page] [Back]        ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 4. Timeline (`/ts timeline <player>`)

Chronological heatmap of events and confidence changes.

```text
╔══════════════════════════════════════════════════════════════╗
║  Timeline — Steve                        Window: [-60s, now] ║
╠══════════════════════════════════════════════════════════════╣
║  Time    Event                    Movement  Combat  Alert    ║
║  ─────────────────────────────────────────────────────────── ║
║  -58s    ························   0.12    0.20             ║
║  -45s    [TELEPORT] conflict        0.12    0.20             ║
║  -30s    ························   0.18    0.22             ║
║  -12s    [PATTERN] support          0.54    0.22             ║
║   -8s    [AIR_TIME] support         0.68    0.22             ║
║   -3s    [ACCEL] support            0.82    0.22    ⚠ ALERT  ║
║   -2s    [LAG] conflict             0.71    0.22             ║
║   now    ■ current                  0.71    0.23             ║
╠══════════════════════════════════════════════════════════════╣
║  Heatmap legend: ░ low ▒ medium ▓ high confidence            ║
║  [Jump to Replay -3s] [Expand -12s to -3s] [Back]           ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 5. Replay Controls (`/ts replay <player> [time]`)

```text
╔══════════════════════════════════════════════════════════════╗
║  Replay — Steve — Alert trigger (-3s)         [Fullscreen] ║
╠══════════════════════════════════════════════════════════════╣
║  ┌─────────────────────────────────────────────────────────┐ ║
║  │                                                         │ ║
║  │            [3D movement trace — top-down]               │ ║
║  │            · · · ● player                               │ ║
║  │            ───────── path                               │ ║
║  │            ▲ velocity vector                            │ ║
║  │                                                         │ ║
║  └─────────────────────────────────────────────────────────┘ ║
║  Tick: 48291 / 48391    Speed: 0.84    OnGround: false      ║
║  Yaw: 142°  Pitch: -12°   Health: 18.5   Ping: 187ms        ║
╠══════════════════════════════════════════════════════════════╣
║  [⏮ -10s] [⏪ -1s] [◀ Prev] [▶ Play] [Next ▶] [⏩ +1s] [⏭ +10s] ║
║  Speed: [0.25x] [0.5x] [1x] [2x]                             ║
║  Layers: [✓ Movement] [✓ Hits] [✓ Packets] [ Inventory]       ║
║  [Link Evidence] [Export Clip] [Back to Inspector]           ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 6. Trust / Suspicion Dashboard (`/ts trust` / `/ts suspicion`)

Server-wide overview for senior staff.

```text
╔══════════════════════════════════════════════════════════════╗
║  TS-Spear Dashboard — 487 players online       [Alerts: ON]  ║
╠══════════════════════════════════════════════════════════════╣
║  TOP SUSPICION (trending up)                                 ║
║  ┌──────────┬──────────┬────────┬─────────┬─────────────────┐║
║  │ Player   │ Suspicion│ Delta  │ Top Dim │ Action          │║
║  ├──────────┼──────────┼────────┼─────────┼─────────────────┤║
║  │ Steve    │ 0.71 ▲   │ +0.14  │ Movement│ [Inspect]       │║
║  │ Alex     │ 0.58 ▲   │ +0.08  │ Combat  │ [Inspect]       │║
║  │ Notch    │ 0.45 ─   │ +0.01  │ Auto    │ [Inspect]       │║
║  └──────────┴──────────┴────────┴─────────┴─────────────────┘║
╠══════════════════════════════════════════════════════════════╣
║  RECENT ALERTS (last 5 min): 3    Checks active: 24/28       ║
║  Storage queue: 12 pending    Worker pool: 4/8 busy          ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 7. Check Management (`/ts checks`)

```text
╔══════════════════════════════════════════════════════════════╗
║  Check Registry                              [Reload Config] ║
╠══════════════════════════════════════════════════════════════╣
║  Movement                                                    ║
║  [✓] impossible-movement   Movement   last: 3s ago   0.12ms  ║
║  [✓] fly-check             Movement   last: 45s ago 0.08ms ║
║  [✓] speed-check           Movement   last: 1s ago  0.05ms  ║
║  [ ] jesus-check           Movement   DISABLED               ║
║  Combat                                                      ║
║  [✓] reach-check           Combat     last: 12s ago 0.15ms  ║
║  [✓] killaura-check        Combat     last: 8s ago  0.22ms  ║
╠══════════════════════════════════════════════════════════════╣
║  Selected: impossible-movement                               ║
║  Enabled: true   Threshold: 0.52   Pool hits: 847/min        ║
║  [Toggle] [Edit Config] [View Evidence Produced]             ║
╚══════════════════════════════════════════════════════════════╝
```

---

## Design Notes

- **Color coding:** Red = supporting suspicion, Blue = conflicting, Yellow = neutral/system
- **No punishment buttons** in default GUI — staff actions (kick/ban) are out of scope; freeze is inspection-only
- **All numbers show 2 decimal places** for confidence; never "flagged: yes"
- **Pagination:** 5 items per GUI page (inventory slot constraint)
- **Async load:** GUIs show loading state; never block main thread