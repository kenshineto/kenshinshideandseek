## Contributing Guidelines

Thanks for contributing to this project, PRs are always welcome.

### Pull requests

Make sure to open PRs against the `main` branch, as that is where development takes place.

Also make sure to use the PR template. If you remove the template and don't have a good reason for it, your PR will be closed.

If your PR was closed for not being a necessary feature, please do not resubmit the PR.

### Style Guide

This project uses [ktlint](https://pinterest.github.io/ktlint/latest/) for formatting and [detekt](https://detekt.dev/) for static code analysis.

Run `gradle lint` to check for formatting errors and run static code analysis.

Run `gradle format` to format all the source files.

Please make sure there are no formatting or code analysis warnings/errors before making your PR.

### Code Guidelines

This is a list of some rules that you should probably follow, though it's not exhaustive

- Please limit nesting of your code where possible. Prefer early returns over nested if blocks.
- Limit the amount of logic added to the platform modules, as logic is prefered in the core module to prevent code duplication.
- Try to make code work with concurrency where necessary to prevent race conditions.
- Avoid `!!` (kotlins ignore nullable operator) unless absolutely necessary
- No Java code, Kotlin only

### Compatibility

The Bukkit platform of the plugin MUST
- Work on Spigot and Paper
- Work on all versions since 1.8

The Fabric platform only supports the latest version.

### Testing

Please make sure your changes build/function. After opening your PR, automated tests will be run to make sure it builds, but it does not
guarantee correct functionality.

### Rebasing

This project uses rebasing to upstream changes instead of merges or squashed merges. Please make sure to rebase your changes as you continue
to develop your PR. Thanks!
