# SkyTab

### Astronomy. Intelligence. Control.

**SkyTab** is a futuristic Android command-center application designed to bring astronomy exploration, satellite tracking, and device control together through a unified interface.

Built with native Android technologies, SkyTab aims to provide an intuitive, immersive, and extensible platform for interacting with astronomical data and connected devices.

---

## Overview

SkyTab combines astronomy-focused tools with practical device-management capabilities in a single Android application.

The project is being developed with an emphasis on modularity, lightweight implementation, and an immersive user experience.

## Core Features

### 01 — Interactive Sky Map
Explore the night sky through an interactive astronomical visualization powered by Stellarium Web.

- Interactive sky exploration
- Web-based astronomical visualization
- Full-screen viewing experience

### 02 — Satellite Tracking
Access satellite visualization and tracking through dedicated tracking views.

Currently supported tracking categories:

- International Space Station (ISS)
- Hubble Space Telescope
- Starlink satellite trains
- GPS satellites
- Space stations

Satellite visualizations are powered by Azmth.

### 03 — PC Integration
SkyTab is designed to communicate with a Windows companion application over a local network.

Planned and developing capabilities include:

- PC connectivity monitoring
- Remote shutdown
- Remote sleep
- Device pairing and authentication

*The Windows companion currently operates as a separate project and is not included in this repository.*

### 04 — Futuristic Interface
The application features a dark, space-inspired interface designed around:

- Minimalist command-center layout
- Animated interface transitions
- Live digital clock
- Translucent visual elements
- Compact controls for mobile devices

---

## Technology Stack

| Component | Technology |
|---|---|
| Primary language | Kotlin |
| Android UI | XML |
| Development environment | Android Studio |
| Build system | Gradle Kotlin DSL |
| Astronomy visualization | Stellarium Web |
| Satellite visualization | Azmth |
| Web integration | Android WebView |
| PC companion | Python / Flask (separate project) |

## Architecture

SkyTab is being developed using a modular Android application structure.

```text
SkyTab
│
├── MainActivity
│   └── Main command-center interface
│
├── MainActivity2
│   └── Interactive Sky Map
│
├── SatPassActivity
│   └── Satellite tracking interface
│
└── Windows Companion
    └── Separate Python / Flask application
```

The Android application and Windows companion are maintained separately, allowing each component to evolve independently.

---

## Getting Started

### Requirements

- Android Studio
- Android SDK
- Kotlin support
- Gradle
- Android device or emulator

**Minimum Android version:** Android 6.0 (API 23), subject to device and feature compatibility.

### Setup

1. Clone the repository:

```bash
git clone https://github.com/Shlok-007-in/SkyTab.git
```

2. Open the cloned project in Android Studio.

3. Allow Gradle to synchronize the project.

4. Connect an Android device or launch an emulator.

5. Build and run the application.

---

## Project Status

**Active Development**

SkyTab is an evolving project. Its current implementation focuses on the core Android interface, interactive astronomy views, satellite visualization, and the foundations of Windows PC integration.

Future development will focus on:

- Improved astronomical visualization
- Expanded satellite tracking capabilities
- Persistent and secure device pairing
- Enhanced Windows companion integration
- Interface optimization across device sizes
- Additional command-center functionality

---

## Development Philosophy

SkyTab is built around three principles:

- **Exploration:** Make astronomical exploration accessible through a unified interface.
- **Integration:** Connect mobile functionality with desktop capabilities.
- **Extensibility:** Develop a modular foundation that can support future features.

---

## Author

**Shlok Avatade**

Developer and creator of SkyTab.

GitHub: [@Shlok-007-in](https://github.com/Shlok-007-in)

---

## License

License information will be added when the project licensing model is finalized.

---

<p align="center">
  <strong>SKYTAB</strong><br/>
  <em>One interface. A universe of possibilities.</em>
</p>