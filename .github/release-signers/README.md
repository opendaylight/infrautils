<!--
SPDX-License-Identifier: Apache-2.0
SPDX-FileCopyrightText: 2026 The Linux Foundation
-->

# Release signers

`.github/workflows/maven-release-on-tag.yaml` releases only from tags
signed by a key in this directory, read from the default branch.

To become a release signer, a committer adds an ASCII-armoured public
key, one file per person, named after their Gerrit username:

```bash
gpg --armor --export <key-id> > .github/release-signers/<username>.asc
```

and submits it for review like any other change. Removing a file
revokes that signer for every branch once it merges.

## Releasing

Tag a merged commit whose root POM is `X.Y.Z-SNAPSHOT`, then push the
tag to Gerrit:

```bash
git tag -s vX.Y.Z -m "infrautils vX.Y.Z" <merged-sha>
git push gerrit vX.Y.Z
```

Gerrit replicates the tag to GitHub, and the workflow validates, signs,
stages and releases it. Bump the branch to the next `-SNAPSHOT` with a
normal Gerrit change afterwards.
