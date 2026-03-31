# Better First Person (BFP)

**Better First Person (BFP)** is an advanced client-side framework for Minecraft that introduces fluid, procedural first-person animations and dynamic camera mechanics. Engineered under the GBXTeam architecture, BFP aims to provide a Triple-A cinematic experience by replacing static viewmodels with a fully responsive, physics-based rendering system.

## Core Architecture

- **Procedural Kinematics**: Implements a dedicated `RigSystem` and `PoseManager` to seamlessly interpolate between player states, ensuring weapons and tools respond organically to momentum, gravity, and player velocity.
- **Cinematic Camera Engineering**: Introduces a non-linear, cubic inverse-square falloff system for explosion shockwaves, delivering highly realistic, distance-aware camera trauma.
- **Organic AFK Sway**: Integrates a background cinematic idle handler that organically drifts the viewport during inactivity through desynchronized sine wave patterns, simulating a professional drone or director camera.
- **Modular Integration**: Fully configurable in-game parameters, allowing precise calibration of animation blending rates, camera shake sharpness, and base offsets.

## Installation & Dependencies

BFP is designed as a standalone Fabric client modification. 

**Required:**
- Fabric Loader & Fabric API

**Recommended:**
- `ModMenu` and `Cloth Config API` / `YetAnotherConfigLib` (YACL) to access the integrated configuration interface.

## System Compatibility

The BFP rendering engine is optimized for high compatibility with modern performance enhancements and rendering pipelines.

> **Technical Note**: The procedural algorithms operating within `BFPGameRendererMixin` and `FirstPersonMovementFlows` may conflict with resource packs or mods that forcefully override hardcoded viewmodel translation matrices. Modifiers like Sodium and Iris are fully supported.

## Licensing 

**Custom Open License**
The source code provided in this repository is free for use, modification, and distribution. 
Requirement: You must explicitly credit "BFP (Better First Person)" within your project's description or credits section if you utilize this logic.

---
*Developed and maintained by GBXTeam.*
