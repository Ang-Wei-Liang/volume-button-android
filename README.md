# 📱 Android Volume Button Listener (Screen Off Support)

This Android app demonstrates how to **listen to volume button presses** (Volume Up / Volume Down) **even when the screen is off**, using a **foreground service** and a `MediaSessionCompat`. A key drawback is that the up button cannot be detected when volume is at 100% and the down button cannot be detected when the volume is at 0%, since there needs to be a change in volume.

---

## ✨ Features

- 🔊 Detect Volume Up / Volume Down presses
- 🔒 Works when the screen is locked
- 📡 Runs as a foreground service
- 🔁 Supports single, double, long press detection
- 🛠️ Jetpack Compose UI

---

## 📸 Demo

<table>
  <tr>
    <td align="center">
      <img src="https://i.ibb.co/DPPp8Tnx/Simple-Service.jpg" alt=" Application Simple Demo" width="300"/><br/>
       Application Simple Demo
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://i.ibb.co/5hx6w4zz/Screenshot-2025-06-02-020327.png" alt="Android Studio Logs" width="1000"/><br/>
      The logs in android studio
    </td>
  </tr>
</table>

---

