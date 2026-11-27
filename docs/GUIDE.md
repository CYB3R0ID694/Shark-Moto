# 📱 Shark Neo: Simple Beginner's Guide

This guide explains how to use **Kernel Diagnostic** and how to **add support for an unsupported device using `boot.img`** in Shark Neo.

---

## 🔍 Guide 1: How & When to Use "Kernel Diagnostic"

### What is it?
Think of **Kernel Diagnostic** like an **X-ray / ID card** for your phone. It checks your phone model, exact software version, and tells the app whether your system is ready.

### When should you use it?
* ⚠️ **Your phone says "Unsupported Device" or "Unsupported Kernel"** on the home screen.
* ❌ **The jailbreak fails or reboots your phone**, and you want to see what went wrong.
* 💬 **You are asking for help on Telegram or GitHub**, and the developers ask for your device information.

---

### Step-by-Step: How to Use It

1. **Start Shizuku**:
   * Open the **Shizuku** app first and make sure it says *"Shizuku is running"*.
   * Open **Shark Neo** and make sure the top status box is green (authorized).
2. **Open Kernel Diagnostic**:
   * Tap the **Extras** tab at the bottom of the screen.
   * Scroll down and tap **Kernel Diagnostic** *(or tap the "Unsupported" banner directly on your home screen)*.
3. **Run the Check**:
   * Tap the **Run Diagnostic** button at the top.
   * Wait 3–5 seconds while it scans your phone.
4. **Share Your Results**:
   * Tap **Copy Diagnostic** to copy the full report to your clipboard so you can paste it in chat.
   * Or tap **Submit Diagnostic to Group** to send it directly to our Telegram help group ([@sharkmotosupport](https://t.me/sharkmotosupport)).

---

<br>

---

## 🛠️ Guide 2: How to Add Your Phone Profile Using `boot.img`

### What is this and why is it needed?
Every phone update has a slightly different internal memory map. If Shark Neo says **"Unsupported Device"**, it just means the app doesn't have the profile for your specific firmware build yet.

By feeding Shark Neo your phone's official **`boot.img`** file, the app automatically decompresses the kernel, extracts all required memory offsets, and **activates full support for your device in seconds!**

---

### Step 1: Get the `boot.img` for your phone

> [!IMPORTANT]
> **Always use `boot.img` (NOT `init_boot.img`)!**
> On Android 13, 14, 15, 16, and 17, `init_boot.img` only contains the generic ramdisk. The actual Linux kernel binary always lives inside **`boot.img`**.

* **For Motorola Phones (Easiest Method)**:
  1. On a computer, install Motorola's free official tool: **Lenovo Rescue and Smart Assistant (LMSA)**.
  2. Connect your phone and let LMSA download the official firmware for your phone.
  3. Once downloaded, open your computer's files and navigate to:
     `C:\ProgramData\LMSA\Download\RomFiles\`
  4. Open your phone's firmware folder and locate **`boot.img`**.
* **For Xiaomi, Redmi, POCO, Realme, OnePlus, OPPO, Vivo, iQOO, or Pixel**:
  * Download the official Fastboot ROM, Recovery ROM, or full OTA update for your exact model and software build.
  * Extract the package on your computer:
    * If the ROM contains `.img` files directly: locate **`boot.img`**.
    * If the ROM contains `payload.bin`: extract it using [payload-dumper-go](https://github.com/ssut/payload-dumper-go) (`payload-dumper-go -p boot payload.bin`) to get **`boot.img`**.
    * You can also select the whole OTA `.zip` directly in Shark Neo if it contains `boot.img`.

---

### Step 2: Copy `boot.img` to your phone
* Transfer or copy the **`boot.img`** file to your phone's **Downloads** folder (or any accessible folder).

---

### Step 3: Let Shark Neo Parse It (1-Click)

1. Open **Shark Neo**.
2. Tap the **Extras** tab at the bottom.
3. Scroll down and tap **Parse Boot Image**.
4. The system file picker will open. **Select your `boot.img` file**.
5. Wait a few seconds while Shark Neo parses the image:
   - ✅ Automatically strips MediaTek (MTK) headers if on a Dimensity/Helio chip.
   - ✅ Unpacks LZ4 Frame, LZ4 Legacy, GZIP, or raw ARM64 kernel payloads across Snapdragon, MediaTek, Tensor, and Exynos.
   - ✅ Recovers kernel banner, timer call-sites, and security structures.
6. A **Parse Successful** dialog will appear displaying:
   - Kernel banner (e.g. `6.6.118-...` or `6.12.x`)
   - Number of recovered symbols
   - Detected device model name (editable)
7. Tap **Save Profile** (or **Copy JSON** if you wish to share it).

---

### What Happens Automatically:
* 💾 **Saved Locally**: Saved to your internal app storage and exported to `Download/Shark/profiles.json`.
* 📡 **Auto Telegram Upload**: Your profile and device specs are securely uploaded to the community bot so official support is added for everyone.
* 🚀 **Ready to Root**: Return to the **Home** tab — your device will now show as **Supported** with the matching engine selected! Tap **One-Click Root** to proceed.
