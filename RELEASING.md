Releasing notes
===============

Releases are published to Maven Central by the `Release` GitHub Actions workflow,
using [sbt-ci-release](https://github.com/sbt/sbt-ci-release).
The version comes from the git tag.

- Check that CI on `main` is green.
- Tag the release commit and push the tag:
  - `git tag -a v0.4.9 -m "v0.4.9"`
  - `git push origin v0.4.9`
- The workflow publishes the plugin for sbt 1 and sbt 2 and releases it on Sonatype Central.
- Publish the GitHub release draft created by Release Drafter.

The workflow needs these repository or organization secrets:
`PGP_SECRET`, `PGP_PASSPHRASE`, `SONATYPE_USERNAME` and `SONATYPE_PASSWORD`.
See the sbt-ci-release README for how to create them.
