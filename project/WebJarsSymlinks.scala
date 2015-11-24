import java.io.File
import java.nio.file.Files

import com.github.os72.protocjar.Protoc
import com.trueaccord.scalapb.ScalaPbPlugin
import com.trueaccord.scalapb.ScalaPbPlugin.{protobufConfig, runProtoc}
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.pipeline.Pipeline
import org.apache.commons.io.FileUtils
import org.json4s.JsonAST.JObject
import org.json4s.native.JsonMethods
import sbt.Keys._
import sbt._
import sbtprotobuf.ProtobufPlugin

import scala.annotation.tailrec

object WebJarsSymlinks extends AutoPlugin {

  val symlinks = TaskKey[Pipeline.Stage]("symlinks", "Rename ill-named extracted webjars directories.")

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    pipelineStages += symlinks,

    symlinks := Def.task[Pipeline.Stage] { mappings =>
      var versions = Map.empty[String, VersionNumber]
      var renamed = Map.empty[String, String]

      for (mapping <- mappings if mapping._1.getName == "bower.json") {
        JsonMethods.parse(org.json4s.FileInput(mapping._1)) match {
          case bowerObj: JObject =>
            val targetName = bowerObj.values("name").asInstanceOf[String]
            val version = VersionNumber(bowerObj.values.getOrElse("version", "0").asInstanceOf[String])

            val source = mapping._1.getParentFile
            val target = source.getParentFile / targetName

            val previousVersion = versions.get(targetName)

            if (source != target && previousVersion.forall(versionLessThan(version))) {
              renamed += ((source.getName, targetName))
              versions += ((targetName, version))
              target.delete()
              Files.createSymbolicLink(target.toPath, source.toPath)
            }

          case invalid =>
            sys.error("Expected JSON object, but got " + invalid)
        }
      }

      mappings.map {
        case (file, name) =>
          val newName = renamed.foldLeft(name.split(File.separatorChar).toList) {
            case (pathComponents, (sourceName, targetName)) =>
              pathComponents match {
                case prefix :: `sourceName` :: rest =>
                  prefix :: targetName :: rest
                case unchanged =>
                  unchanged
              }
          }.mkString(File.separator)

          (file, newName)
      }.map(flip).toMap.toSeq.map(flip)
    }.value,

    runProtoc in protobufConfig := Def.task { (args: Seq[String]) =>
      FileUtils.deleteDirectory((sourceManaged in Compile).value / "compiled_protobuf" / "models")
      Protoc.runProtoc(args.toArray)
    }.value
  )

  /**
   * Returns whether b is less than a, so b < a.
   */
  private def versionLessThan(a: VersionNumber)(b: VersionNumber): Boolean = {
    @tailrec
    def less(found: Seq[Boolean]): Boolean = {
      found match {
        case Nil               => false
        case head +: _ if head => true
        case _ +: tail         => less(tail)
      }
    }

    less(b.numbers.zipAll(a.numbers, 0L, 0L).map { case (b, a) => b < a })
  }

  private def flip[A, B](pair: (A, B)): (B, A) = (pair._2, pair._1)

}
