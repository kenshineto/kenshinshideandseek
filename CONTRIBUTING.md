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

### Testing

Please make sure your changes build/function. After opening your PR, automated tests will be run to make sure it builds, but it does not
guarantee correct functionality.

### Rebasing

This project uses rebasing to upstream changes instead of merges or squashed merges. Please make sure to rebase your changes as you continue
to develop your PR. Thanks!
