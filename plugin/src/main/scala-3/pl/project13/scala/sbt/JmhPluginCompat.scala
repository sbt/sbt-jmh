/*
 * Copyright 2014 pl.project13.scala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.project13.scala.sbt

import java.nio.file.{ Path => NioPath }
import _root_.sbt.*
import xsbti.{ FileConverter, HashedVirtualFileRef }

private[sbt] object JmhPluginCompat {
  def toPath(a: File): NioPath =
    a.toPath

  def toAttributedFile(a: Attributed[HashedVirtualFileRef], converter: FileConverter): Attributed[File] =
    a.map(converter.toPath(_).toFile)
}
