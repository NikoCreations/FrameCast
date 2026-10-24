# FrameCast

**Turn your Minecraft walls into screens.**

FrameCast is a client-side Fabric mod that lets you display images and videos on walls in Minecraft. Select an area on a wall, configure your media, and turn part of your world into a screen.

FrameCast is currently in **Beta**. Some bugs, crashes, and compatibility issues may still occur.

## Features

- **Image and video display** — bring your own media into Minecraft.
- **Custom wall screens** — select two corners of a vertical wall to define the screen area.
- **Brightness control** — adjust the screen brightness with an in-game command.
- **Client-side rendering** — designed to display media on your own Minecraft client.
- **Local media files** — use supported media stored on your computer.

Video playback may require FFmpeg and FFprobe, depending on your configuration and the FrameCast version you are using.

## Requirements

Before installing FrameCast, make sure you have:

- Minecraft Java Edition **26.3**.
- Fabric Loader compatible with Minecraft 26.3.
- Any dependencies listed on the download page for your FrameCast version.
- A compatible Java runtime, as required by your Minecraft installation.
- FFmpeg and FFprobe if required for video playback.

Always download the FrameCast file that matches your Minecraft version.

## Installation

### CurseForge

1. Download the FrameCast `.jar` file from the CurseForge project page.
2. Install Fabric Loader for Minecraft 26.3 if you have not already done so.
3. Open your Minecraft game directory.
4. Open the `mods` folder, or create it if it does not exist.
5. Move the downloaded FrameCast `.jar` file into that folder.
6. Install any additional dependencies listed on the project page.
7. Launch Minecraft using your Fabric installation.

Make sure you download the actual mod `.jar` file, not the source code or a ZIP archive of the repository.

### Modrinth App

FrameCast is **not currently available through the Modrinth App**. Support may be added in the future.

For now, download FrameCast from CurseForge and install it manually using the instructions above.

### Manual installation

The manual installation process is similar on all three major desktop operating systems:

1. Install Fabric Loader for Minecraft 26.3.
2. Download the correct FrameCast `.jar` file.
3. Locate your Minecraft game directory.
4. Open or create the `mods` folder.
5. Place the FrameCast `.jar` file inside it.
6. Install any required dependencies.
7. Launch Minecraft with the Fabric profile.

If you use a custom launcher or a separate game instance, install the mod in that instance's game directory rather than the default Minecraft folder.

## Finding your Minecraft folder

### macOS

The default Minecraft directory is:

`~/Library/Application Support/minecraft`

To open it in Finder:

1. Open Finder.
2. Press `Command + Shift + G`.
3. Enter the path above.
4. Press Enter.

You can also open the directory using Terminal:

```bash
open "$HOME/Library/Application Support/minecraft"
```

To create the `mods` folder and open it:

```bash
mkdir -p "$HOME/Library/Application Support/minecraft/mods"
open "$HOME/Library/Application Support/minecraft/mods"
```

Move the downloaded FrameCast `.jar` file into this folder.

If your launcher uses a custom game directory, use that directory instead.

### Windows

The default Minecraft directory is:

`%APPDATA%\.minecraft`

To open it:

1. Press `Win + R`.
2. Type `%APPDATA%\.minecraft`.
3. Press Enter.
4. Open the `mods` folder, or create it if necessary.

Alternatively, open PowerShell and run:

```powershell
$mods = Join-Path $env:APPDATA ".minecraft\mods"
New-Item -ItemType Directory -Force -Path $mods
explorer $mods
```

Move the downloaded FrameCast `.jar` file into the folder that opens.

If you use a launcher with a separate game directory, use the folder configured for that instance.

### Linux

The default Minecraft directory is:

`~/.minecraft`

Open a terminal and run:

```bash
mkdir -p "$HOME/.minecraft/mods"
xdg-open "$HOME/.minecraft/mods"
```

Move the FrameCast `.jar` file into the `mods` folder.

If `xdg-open` is unavailable, open the directory manually using your file manager.

Custom launchers may use different game directories.

## Setting up video playback

FrameCast may require **FFmpeg** and **FFprobe** for video playback. These are separate tools and are not included with Minecraft or Fabric.

If your version requires them, install them using the instructions for your operating system.

### macOS

If you already have Homebrew installed, open Terminal and run:

```bash
brew install ffmpeg
```

Check that both tools are available:

```bash
ffmpeg -version
ffprobe -version
```

If the commands are not found, make sure Homebrew's executable directory is available in your shell's PATH.

### Windows

1. Visit the official FFmpeg download page: https://ffmpeg.org/download.html
2. Choose a suitable Windows build.
3. Extract the downloaded archive.
4. Locate `ffmpeg.exe` and `ffprobe.exe`, usually inside the `bin` folder.
5. Add that folder to your PATH, or configure the executable paths if your FrameCast version supports it.

Open a new PowerShell window and run:

```powershell
ffmpeg -version
ffprobe -version
```

Both commands should display version information.

### Linux

Install FFmpeg using your distribution's package manager.

**Ubuntu / Debian:**

```bash
sudo apt update
sudo apt install ffmpeg
```

**Fedora:**

```bash
sudo dnf install ffmpeg
```

**Arch Linux:**

```bash
sudo pacman -S ffmpeg
```

Verify the installation:

```bash
ffmpeg -version
ffprobe -version
```

Package availability may depend on your distribution and enabled repositories.

## Using FrameCast

### 1. Prepare your media

Choose an image or video stored on your computer.

Make sure the file exists, is readable, and uses a format supported by your FrameCast version. Keep the file in a location that will not change unexpectedly, because moving or renaming it may invalidate the configured path.

### 2. Select a wall

1. Enter your Minecraft world.
2. Hold the selection tool used by FrameCast.
3. Click the first corner of a vertical wall.
4. Click the opposite corner on the same wall face.
5. Follow the messages displayed in chat.

Both corners must belong to the same vertical wall plane. Floors and ceilings cannot be selected.

If the selection is rejected, start again and make sure both clicks are on the same wall face.

### 3. Configure your media

Configure the media file and playback options using the settings supported by your installed FrameCast version.

For video playback, check whether FFmpeg and FFprobe are required and make sure they can be located by the mod.

If your screen does not display anything, verify the file path, media format, and any required media-tool configuration.

### 4. Adjust screen brightness

If your installed version includes the brightness command, use:

```text
/framecast bright <0-100>
```

Replace `<0-100>` with a number between 0 and 100.

For example:

```text
/framecast bright 100
```

sets the brightness to 100%, while:

```text
/framecast bright 50
```

sets it to 50%.

A value of `0` sets the minimum brightness, and `100` sets the maximum.

If the command is unavailable, check that you are using a FrameCast build that includes this feature.

## Troubleshooting

### FrameCast does not appear in Minecraft

- Make sure you installed the `.jar` file rather than a source-code archive.
- Confirm that you are using Fabric Loader.
- Check that your Minecraft version matches the FrameCast release.
- Verify that the file is in the correct instance's `mods` folder.
- Install any required dependencies.

### Images or videos do not appear

- Check that the media file exists at the configured path.
- Verify that the format is supported.
- Confirm that the wall selection is valid.
- Check the Minecraft log for errors.
- If you are using a video, verify the FFmpeg and FFprobe installation if required.

### Minecraft crashes

FrameCast is currently in Beta, so occasional crashes may occur.

If you experience a crash:

1. Close Minecraft.
2. Open the `logs` folder in your game directory.
3. Locate `latest.log` and any crash report generated for the incident.
4. Note what you were doing when the crash occurred.
5. Report the issue with your Minecraft version, FrameCast version, Fabric Loader version, operating system, reproduction steps, and relevant log excerpts.

Include enough information to help reproduce the problem. Avoid posting only that the game crashed.

## Compatibility

FrameCast is designed for Fabric and the Minecraft version specified on each release.

Compatibility with other mods, launchers, media formats, and game versions is not guaranteed unless explicitly stated.

FrameCast is designed as a client-side mod, so it is intended to render media on the player's own client without requiring every player on a multiplayer server to install it. Actual compatibility may depend on the features being used and the server environment.

## Contributing

Bug reports and pull requests are welcome.

When submitting a bug report, include:

- Minecraft version.
- FrameCast version.
- Fabric Loader version.
- Operating system.
- Steps to reproduce the issue.
- Relevant logs or crash reports.

Pull requests should explain the purpose of the change, describe the modifications, and include relevant testing information. Contributions are reviewed before they are merged into the original project.

## License

FrameCast is distributed under the **Community Attribution License (CAL) 1.0**.

Redistributed copies and forks must retain the original copyright notice and license and document their modifications in accordance with the license terms. Refer to the `LICENSE` file in the project repository for the complete terms.

## Disclaimer

FrameCast is an independent community project and is not affiliated with, endorsed by, or sponsored by Mojang Studios or Microsoft.

Minecraft is a trademark of Microsoft Corporation.

**FrameCast — Your world. Your screen.**
