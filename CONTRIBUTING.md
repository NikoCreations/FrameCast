# Contributing to FrameCast

Thank you for your interest in contributing to FrameCast.

FrameCast is a Minecraft Fabric mod that allows players to display images and videos on in-game walls. Contributions are welcome, including bug reports, code improvements, documentation updates, compatibility fixes, and feature proposals.

This document explains how to contribute to the project.

## Before You Start

Before submitting a contribution:

- Read the project's `README.md` to understand its functionality and requirements.
- Review the `LICENSE` file and its terms.
- Check existing issues and pull requests to avoid duplicate work.
- Make sure your proposed changes align with FrameCast's goals.

For significant changes, open an issue or discussion first to describe your proposal and receive feedback.

## Ways to Contribute

### Reporting Bugs

If you encounter a bug, open an issue at the [FrameCast issue tracker](https://github.com/NikoCreations/FrameCast/issues).

Include as much relevant information as possible:

- A clear and descriptive title.
- The FrameCast version affected.
- Your Minecraft version.
- Your Fabric Loader version.
- Your Java version, when relevant.
- Your operating system.
- Steps to reproduce the issue.
- Expected and actual behavior.
- Relevant logs, crash reports, screenshots, or error messages.

Check existing issues before creating a new report.

Do not include passwords, access tokens, private keys, or other sensitive information.

**For potential security vulnerabilities, follow `SECURITY.md` instead of opening a public issue.**

### Suggesting Features

Feature proposals should explain:

- The problem the feature would solve.
- How the proposed feature should work.
- Why it would benefit FrameCast users.
- Any relevant performance or compatibility considerations.

Feature suggestions are welcome, but implementation is not guaranteed. The project maintainer makes the final decision on priorities and scope.

### Improving Documentation

Contributions to documentation are welcome, including improvements to installation instructions, troubleshooting guides, the README, and developer documentation.

Documentation must accurately describe verified functionality. Do not claim support for untested Minecraft versions, loaders, operating systems, or features.

### Contributing Code

Useful code contributions include:

- Bug fixes.
- Rendering and playback improvements.
- Performance optimizations.
- Minecraft and Fabric compatibility fixes.
- Error handling and stability improvements.
- Code quality improvements.
- Automated tests and developer tooling.

Keep changes focused and avoid unrelated refactoring.

## Development Setup

To work on FrameCast locally:

1. Install Git.
2. Install a compatible Java Development Kit (JDK).
3. Clone the official repository.
4. Open the project in a compatible Java IDE.
5. Use the Gradle wrapper included in the repository to build the project.

Clone the repository using:

```bash
git clone https://github.com/NikoCreations/FrameCast.git
cd FrameCast
```

Use the Java, Minecraft, Fabric Loader, and other dependency versions specified by the current project configuration.

### Building the Project

On macOS or Linux:

```bash
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

Run the relevant tests and validation tasks available in the project before submitting changes.

Do not commit generated build output, local IDE settings, crash reports, or temporary files.

## Code Guidelines

When contributing code:

- Follow the existing project structure and coding conventions.
- Use clear, descriptive names for classes, methods, and variables.
- Keep code readable, maintainable, and focused.
- Handle errors appropriately.
- Avoid unnecessary dependencies.
- Preserve existing functionality unless a change intentionally modifies it.
- Maintain compatibility with the project's declared Minecraft and Fabric targets unless the contribution explicitly updates them.
- Avoid logging sensitive information or unnecessary private file paths.
- Do not claim that a feature works without testing it.

FrameCast should remain reliable, efficient, and maintainable.

## Testing Your Changes

Before submitting a pull request:

1. Build the project successfully.
2. Run available automated tests and validation tasks.
3. Test affected functionality in Minecraft whenever possible.
4. Review relevant logs for errors.
5. Check that existing functionality has not been unintentionally broken.
6. Document any limitations or testing that could not be completed.

For changes involving media playback, rendering, or file access, test the relevant scenarios whenever your environment allows it.

If you have not tested a change in-game, state that clearly in your pull request.

## Submitting a Pull Request

To contribute code:

1. Fork [NikoCreations/FrameCast](https://github.com/NikoCreations/FrameCast).
2. Create a separate branch for your changes.
3. Implement your changes.
4. Test your work.
5. Commit your changes with a clear message.
6. Push your branch to your fork.
7. Open a pull request against the official repository.

Every pull request should include:

- A concise title.
- A description of the problem or improvement.
- A summary of the changes.
- Related issue references, if applicable.
- Tests performed and their results.
- Known limitations or outstanding concerns.

Keep pull requests focused and easy to review. Large contributions may need to be divided into smaller changes.

The maintainer may request revisions, additional testing, or clarification before accepting a contribution.

Submitting a pull request does not guarantee acceptance or inclusion in a release.

## Commit Messages

Use concise commit messages that describe the change.

Examples:

- `Fix video playback initialization`
- `Improve rendering stability`
- `Update installation documentation`
- `Handle invalid media files`
- `Improve compatibility checks`

Avoid vague messages such as `changes`, `stuff`, or `fix`.

## Dependencies and Licensing

Before introducing a dependency, explain why it is needed and consider its maintenance status, compatibility, security, and license.

All dependencies must be compatible with the project's distribution terms.

Do not submit code, assets, or other material that you do not have permission to contribute.

By submitting a contribution, you confirm that you have the necessary rights to submit it under the project's applicable licensing terms.

Unless a separate written agreement applies, contributors retain ownership of their contributions and grant NikoCreations the permissions specified in the project's `LICENSE` and any applicable contribution terms.

Contributions are subject to review and acceptance by the project maintainer.

## Community Standards

All contributors are expected to communicate respectfully and constructively.

- Treat other contributors with respect.
- Focus criticism on the work, not the individual.
- Provide useful and actionable feedback.
- Accept constructive criticism.
- Avoid harassment, discrimination, and personal attacks.

The maintainer may close discussions or reject contributions that do not meet these expectations.

## Releases and Versioning

Release timing, version numbers, compatibility declarations, and publication decisions are managed by the project maintainer.

A merged contribution is not guaranteed to appear in the next release.

Do not present unofficial builds or forks as official FrameCast releases.

For supported versions and security-related updates, refer to `SECURITY.md`.

## Questions

If you are unsure whether a proposed change is appropriate, open an issue or discussion in the [official FrameCast repository](https://github.com/NikoCreations/FrameCast) before beginning substantial work.

For security-related concerns, follow the reporting procedure described in `SECURITY.md`.

Thank you for helping improve FrameCast.

**FrameCast is maintained by NikoCreations.**

Official repository: https://github.com/NikoCreations/FrameCast
