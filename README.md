# Hytale Example Plugin

> **⚠️ Warning: Early Access**    
> The game Hytale is in early access, and so is this project! Features may be
> incomplete, unstable, or change frequently. Please be patient and understanding as development
> continues.

More Magic Spells is a **work-in-progress Hytale mod** focused on adding advanced magical spells with strong gameplay impact and new mechanic, using Hytale’s ECS architecture.

The project is primarily experimental and technical, aiming to explore entity manipulation, animations, models, and audio systems through spell mechanics.

---

## Project Goals

The main objectives of this mod are:

- Add impactful magic spells beyond simple damage effects and projectile
- Experiment with **crowd control** and **environment-altering** spells
- Deepen understanding of Hytale’s **ECS**, components, and systems
- Explore model, animation, and audio manipulation at runtime

---

## Implemented Features

### 🪨 Stone Wall (Work In Progress)

`Stone Spellbook Primary Attack`

A spell that summon a wall for a few seconds that : 
- Come from the ground and get back to the ground at the end of the spell
- Have a collision that stop all entity and attacks
- Can or Cannot be destroy (To be decided)

![](/media/StoneWall.gif)

### 🗿 Petrification (Work In Progress)

`Stone Spellbook Secondary Attack`

A spell that temporarily turns an entity into stone, which for the duration of the spell : 
- Stop his movement and velocity
- Make them unable to make an action
- Suppresses his animations
- Turn his texture into a stone texture

![](/media/Petrification.gif)

### ⚡ Lightning Spear (Work In Progress)

`Lightning Staff Primary Attack`

A spell that temporarily create a lightning ball in front of the player : 
- Bring out a chain lightning bolt from the staff
- Make AOE Damage
- Use a lot of particule from particuler spawner file

*(GIF will come later)*

---

## Planned Spells

- Infinite Water Drop (Lauch a large quantity of water projectile in front of the player)
- Water laser (Lauch a Large water laser in front of the player to the next entity or block )
- Summon of lightning 

---

## Technical Overview

This mod is built using Hytale’s **Entity Component System (ECS)**.

Key technical aspects include:

- Custom components (e.g. `PetrifiedComponent`)
- Dedicated systems for time-based effects (e.g. `PetrifiedSystem`)
- Extensive use of `CommandBuffer` for safe component mutation
- Runtime replacement of `ModelComponent` and textures
- Interaction with `ActiveAnimationComponent` and `AnimationSet`
- Exploration of both `SoundUtil.playSoundEvent3d` and `AudioComponent`

The project prioritizes **correct ECS usage** over quick hacks.

---

## Known Issues

- Some animations (idle / breathing / attacks) can persist during petrification
- Audio attenuation behaves inconsistently depending on playback method
- AnimationSet replacement can cause unexpected animation states
- Visual transitions are not yet fully smooth (e.g. `Spawning of the StoneWall`)

These issues are known and investigated.

---

## Disclaimer

This mod uses **Hytale internal APIs** and decompiled sources for learning and experimentation purposes.

It is:
- Not affiliated with Hypixel Studios
- Not guaranteed to be stable
- Subject to breaking changes as Hytale evolves

---

## License

This project is provided as-is for educational and experimental purposes.  
License details may change as the project evolves.
