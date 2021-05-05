Releasing notes
===============

- change all versions from -SNAPSHOT to current one
- commit and tag the change at this stage
- dry run:
  - `sbt -Dsbt.supershell=false clean ^plugin/publishLocalSigned )`
- release the plugin:
  -  prerequisite: @ktoso needs to grant your oss.sontaype.org publish permissions by means of a request to [OSSRH-1324](https://issues.sonatype.org/browse/OSSRH-1324).
  - `sbt -Dsbt.supershell=false clean ^plugin/publishSigned )`
  - Close/Release the staging repository on oss.sonatype.org. 
- If you encounter `gpg: signing failed: Inappropriate ioctl for device`, 
  try `export GPG_TTY=$(tty)` (hat tip: https://github.com/keybase/keybase-issues/issues/2798.

