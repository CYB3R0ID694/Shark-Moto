# OPPO PKJ110（Find X8 Ultra）6.6.118 CVE-2026-98260 适配

与 `rust-t61269` 同源、同一物理链路，只有 per-build 常量不同。
**不改动 6.12.69 / 6.18.21 / V2505A / 6.6.127 既有入口**，新增
`PKJ110_66118` 编译期分支和独立 `libk66118.so`。

这是 **android15-6.6** KMI（6.6.118），不是 6.12 的 android16-6/android16-5。
与 6.6.127 同属 android15-8 GKI，但常量按本镜像重新恢复。

## 状态

2026-11-10 真机验证通过：OPPO Find X8 Ultra（PKJ110，系统指纹
`OPPO/PKJ110/OP5DD3L1:17/AP3A.240617.008/V.4aaf009-2462cbf-2485c5e:user/`），
KernelSU 管理器 3.3.1-27-gdb6263d3（32707-6）。全程 3.5 秒，模块以 LKM 加载，
`/proc/modules` 为 `kernelsu 184320 1 - Live`。

真机日志关键行：

```
PLANT_CRED_OK
LATE_LOAD_RC=0
MODULE_LOADED
[4/4] manager setup
UNLOAD_GUARD_GONE
KSUD_INSTALL_RC=0
ANTIROOT_STOP ... stopped
SU_ID_RC=0: uid=0(root) gid=0(root) context=u:r:ksu:s0
Enforcing
kernelsu 184320 1 - Live
SCREENSHOT_STACK_OK=com.oplus.exsystemservice / com.oplus.screenshot
DONE
```

## OTA 来源与提取

- 全量包：`component-ota/26/10/08/0e2c7d2ace89457d8129cd7ee52943d0.zip`
  （`gauss-compota-c-cn.allawnfs.com`，9,881,994,061 字节；签名链接有时效）
- ZIP 为 **ZIP64**（EOCD 的 `cd_offset=0xffffffff`，须读 ZIP64 EOCD locator/record）；
  5 个条目：`payload.bin`（method 0 = stored）、`payload_properties.txt`、
  `care_map.pb`、`META-INF/com/android/metadata{,.pb}`
- 只按 HTTP Range 读取：ZIP 尾部 4 MiB（EOCD/ZIP64/CD）、payload 头 16 MiB
  （header + manifest）、`boot` 的 data 段 10,895,493 字节
- `payload.bin`：payload `CrAU` v2，manifest 358,593 字节，metadata 签名 267 字节，
  data blob 起点 = `zip_local_header(61) + 24 + 358593 + 267 = 358945`
- manifest：partitions = `DeltaArchiveManifest` 字段 13；partition_name = 1；
  operations = 8；op 的 type/data_offset/data_length = 1/2/3；
  **dst_extents 是字段 6**（本 OTA 的 vendor 扩展，不是标准字段 4）；
  block_size = 4096
- `boot` 共 48 个 op（REPLACE_XZ ×18、ZERO ×29、REPLACE_BZ ×1），data 段位于
  blob `1574031600..1584927093`；按 dst_extents 重建出 100,663,296 字节镜像
  （其余为 ZERO 填充）
- `boot.img`：100,663,296 字节，SHA-256
  `990fd4cb473eb19b9a38847af64f066a29c02aa11bda1abb34813a68ec08dbba`
  - header v4，`header_size=1584`，`kernel_size=37,001,728`，`ramdisk_size=0`
  - 内核 Image 在 `boot.img[0x1000:]`，SHA-256
    `ee5ee448d2985eeeb7a8b5ad2e1efc34eeb410a8beae0ab484504d739124d613`
  - release：`6.6.118-android15-8-g9bc34d5b0c79-abogki537459655-4k`
    （LLD 18.0.0，kleaf@build-host）

## kallsyms 布局注意（本镜像与 6.6.127 不同）

`recover.py` 恢复本镜像时遇到两处差异，已修进工具：

- `kallsyms_offsets` 末位有一个 0 哨兵，单调性校验从 `bad == 0` 放宽到 `bad <= 4`
- **名字表比地址表多一条表头**：`names[i]` 实际配对 `offsets[i-1]`（shift = +1）。
  用 `__ksymtab_*` 的 PREL32 value/name 自洽性自动定标（本镜像 shift=+1，
  6.6.127 复核 shift=+0，常量与原 README 一致）

## 常量

```c
#define TARGET_KERNEL_RELEASE      "6.6.118-android15-8-g9bc34d5b0c79-abogki537459655-4k"

/* do_timer_create+0xa8 -> site */
#define STATIC_TIMER_SITE          0xffffffc0801a4dfcULL
#define TIMER_SITE_LOW21           0x1a4dfcULL

/* copy_process+0x140 -> site */
#define COPY_PROCESS_LOW21         0x0ab944ULL

/* kallsyms 链接期地址 */
#define SELINUX_STATE_STATIC       0xffffffc08239c620ULL
#define PANIC_ON_WARN_STATIC       0xffffffc08214b2ccULL
#define ROOT_USER_STATIC           0xffffffc08216f6e0ULL
#define INIT_USER_NS_STATIC        0xffffffc08216f450ULL
#define INIT_UCOUNTS_STATIC        0xffffffc0821711b0ULL
#define INIT_GROUPS_STATIC         0xffffffc082170c40ULL
```

BTF（镜像自带）与 6.6.127 同构：`k_itimer` 0x108、`cred` 0xb8、
`task_struct` 0x12c0（`cred`@0x820）、`selinux_state` 0x80、`tracepoint.funcs`@0x48。
数据符号自检：`root_user.usage=1 / uid=0`、`panic_on_warn=0`。

## 加载路径

ColorOS 侧不走 6.6.127 的 `hooks_defend` 门（无 kread/白名单步骤），
沿用通用 `ksud late-load --kmi android15-6.6 --allow-shell --package-name <pkg>`；
`finish_manager` 继续按 OPPO 配方处理 `oplus_secure_guard_new` 与 `riskdetect`。

## 目标

- 机型：OPPO Find X8 Ultra（PKJ110），ColorOS 17
- 内核：`6.6.118-android15-8-g9bc34d5b0c79-abogki537459655-4k`
- KMI：`android15-6.6`
- 产物：`libk66118.so`（Cargo feature `pkj110_66118`），
  App 下拉项 `CVE-2026-98260-ColorOS-6.6.118`
