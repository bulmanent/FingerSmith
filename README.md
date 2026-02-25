# FingerSmith (Android MVP)

FingerSmith is an offline piano-practice Android app (Kotlin + Jetpack Compose) with:
- 37-key keyboard visualization (C3-C6)
- Fingering overlays (1-5)
- Real-time playback using bundled piano samples via `SoundPool`
- Songbook with simplified arrangements (`Hey Jude`, `Eleanor Rigby`, `Imagine`)
- Custom practice step-grid editor
- DataStore-backed user settings

## Run
1. Open `/home/neil/StudioProjects/FingerSmith` in Android Studio.
2. Let Gradle sync.
3. Run app on emulator/device (min SDK 26).

## Audio Sampling Strategy
- Samples are included in `app/src/main/res/raw` as `pno_48.wav` ... `pno_84.wav`.
- Root notes are spaced every 3 semitones (C3 to C6).
- For each played MIDI note:
  - nearest root sample is selected
  - `playbackRate = 2 ^ ((midi - rootMidi) / 12)` is applied in `PianoSampler`
- Metronome clicks: `click_hi.wav`, `click_lo.wav`.

## Song Data
- Songs are in `app/src/main/assets/songs.json`.
- Schema:
  - `title: String`
  - `defaultBpm: Int`
  - `range: { startMidi: Int, keys: Int }`
  - `tracks: List<Track>`
- `Track`:
  - `name: String`
  - `hand: "R" | "L"`
  - `events: List<Event>`
- `Event`:
  - `stepIndex: Int`
  - `durationSteps: Int`
  - `notes: List<NoteOn>`
- `NoteOn`:
  - `midi: Int`
  - `finger: Int (1..5)`

Timing grid is fixed to 4/4 with 16 steps per bar for this MVP.

## Settings Persistence
`DataStore (Preferences)` keeps:
- show fingers
- show note names
- piano on/off
- metronome on/off
- selected song
- hand selection
- last BPM

## Launcher Icon
Included launcher setup:
- `app/src/main/res/drawable/ic_launcher_foreground.xml` (psychedelic vector fallback)
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

To replace with high-res PNG:
1. Generate adaptive icon layers from your PNG (foreground/background) in Android Studio Image Asset.
2. Replace `ic_launcher` resources in `mipmap-*` folders.
3. Keep manifest references:
   - `android:icon="@mipmap/ic_launcher"`
   - `android:roundIcon="@mipmap/ic_launcher_round"`

## Tests
Unit tests included for:
- JSON parsing (`SongJsonParsingTest`)
- step timing math (`StepMathTest`)
- MIDI/key layout logic (`KeyboardLayoutTest`)
