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
Every phone update has a slightly different internal memory map. If Shark Neo says **"Unsupported Device"**, it just means the app doesn't have the map for your specific phone update yet.

By feeding Shark Neo your phone's official **`boot.img`** file, the app reads the map, unlocks support, and **adds your phone profile automatically in just a few seconds!**

---

### Step 1: Get the `boot.img` for your phone

You need the official stock `boot.img` (or `init_boot.img`) that matches your current software:

* **For Motorola Phones (Easiest Method)**:
  1. On a computer, install Motorola's free official tool: **Lenovo Rescue and Smart Assistant (LMSA)**.
  2. Connect your phone and let LMSA download the official firmware for your phone.
  3. Once downloaded, open your computer's files and go to:
     `C:\ProgramData\LMSA\Download\RomFiles\`
  4. Open your phone's folder and find the file named **`boot.img`** (or **`init_boot.img`** on Android 13+).
* **For Other Phones (Pixel, OnePlus, Xiaomi, Realme)**:
  * Download your phone's official stock update/ROM from the manufacturer's website.
  * Extract the zip on your computer to find **`boot.img`** or **`init_boot.img`** (use [payload-dumper-go](https://github.com/ssut/payload-dumper-go) if the archive has `payload.bin`).

---

### Step 2: Copy the file to your phone
* Send or copy the **`boot.img`** (or `init_boot.img`) file to your phone's **Downloads** folder.

---

### Step 3: Let Shark Neo Read It (1-Click)

1. Open **Shark Neo**.
2. Tap the **Extras** tab at the bottom.
3. Scroll down and tap **Parse Boot Image**.
4. Your phone's file manager will open. **Select your `boot.img` file**.
5. Wait a few seconds while Shark Neo scans it.
6. A success popup will appear showing your kernel version and extracted symbols.
7. Tap **Close** (or tap **Submit Profile to Group** to help other users with the same phone).

🎉 **That's it!** Shark Neo automatically saves and activates your device profile. 
When you return to the **Home** tab, your phone will now show as **Supported**, and you can proceed with the jailbreak!

---

### 📋 Managing Loaded Profiles
Under **Extras** $\rightarrow$ **Manage GhostLock Profiles**, you can:
- Search through all loaded profiles by model or kernel version.
- Tap any profile to view its full technical offsets table.
- Copy profile JSON or delete obsolete profiles whenever you want.
