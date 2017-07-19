Releasing notes
===============

- change all versions from -SNAPSHOT to current one
- commit and tag the change at this stage
- first release the extras project, it is independent of Scala version
  - make sure it's closed on oss.sonatype.org
- release the plugin
  - remember to release with `^publishSigned` to include sbtCrossVersions 
