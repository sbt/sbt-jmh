Releasing notes
===============

- change all versions from -SNAPSHOT to current one
- commit and tag the change at this stage
- dry run:
  - `sbt -Dsbt.supershell=false clean ^plugin/publishLocalSigned )`
- release the plugin:
  - `sbt -Dsbt.supershell=false clean ^plugin/publishSigned )`
- If you encounter `gpg: signing failed: Inappropriate ioctl for device`, 
  try `export GPG_TTY=$(tty)` (hat tip: https://github.com/keybase/keybase-issues/issues/2798.

