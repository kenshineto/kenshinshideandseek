## Contributing Guidelines

Thanks for contributing to this project, PRs are always welcome.

### Pull requests

This project uses git squash and rebase to merge in PRs. This means when you working on your PR, don't bother trying to get all the commit names perfect.
Only worry about the commits once your PR is accepted and ready to merge. Once approved, `git squash` all the commits down into a single commit, give it a
decriptive name, and wait for it to be rebased into the repository by a maintainer. This helps keeps the git tree empty of endless "fix" or "i did a thing" commits.

When naming your PR, please prefix it with the what type of content the PR is fixing, and what modules it touches. The prefixes are

- `fix` - A bug fix
- `feat` - A new feature addition
- `chore` - General maintenance that isn't a large refactor
- `docs` - Update to the plugin documentation
- `refactor` - A refactor of part of the codebase
- `perf` - Performance fixes
- `test` - Unit test additions

The modules are: `core`, `bukkit`, `mod`, `fabric`, `neoforge`. If it doesnt touch a module, or most them, ommit it.

Make sure to open PRs against the `main` branch, as that is where development takes place, and also make sure to use the PR template.
If you remove the template and don't have a good reason for it, your PR will be closed.

If your PR was closed for not being a necessary feature, please do not resubmit the PR.

### AI Policy

Code generated in part or full with AI will not be approved in a PR.

### Style Guide

This project uses gradle plugins for code quality.

Run `gradle check` to check for formatting errors and run static code analysis.

Run `gradle format` to format all the source files.

Run `gradle coverage` to check code coverage

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
