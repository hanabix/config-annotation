package com.wacai.config.annotation

import java.io.{File, PrintWriter}

import com.typesafe.config.ConfigFactory
import com.typesafe.config.impl.ConfigImplUtil

import concurrent.duration._
import reflect.macros.whitebox

class Macro(val c: whitebox.Context) {

  import Macro._
  import c.universe._
  import Flag._

  lazy val outputDir = {
    val f = new File(c.settings
      .find(_.startsWith(OutputDirSettings))
      .map(_.substring(OutputDirSettings.length))
      .getOrElse(DefaultOutputDir))

    if (!f.exists()) f.mkdirs()

    f
  }

  def impl(annottees: c.Expr[Any]*): c.Expr[Any] = {

    val result = annottees.map(_.tree).toList match {
      case (ClassDef(mods, name, a, Template(parents, s, body))) :: Nil if mods.hasFlag(DEFAULTPARAM | TRAIT) =>

        implicit val out = new PrintWriter(new File(outputDir, s"$name.conf"))

        try {
          node(0)(s"$name") {
            val imports = q"import scala.collection.JavaConversions._"

            val conf = if (parents exists configurable) {
              q"private val _config = config"
            } else {
              q"private val _config = ${reify(CONFIG).tree}"
            }

            ClassDef(mods, name, a, Template(parents, s, imports :: conf :: body.map {
              case Initialized(vd @ ValDef(_, _, _, rhs)) => generate(vd, s"$name", 1)
              case t                                      => t
            }))
          }

        } finally out.close()


      case _ =>
        c.abort(c.enclosingPosition, "Annotation is only supported on trait")
    }

    c.Expr[Any](result)
  }


  lazy val seconds = reify(SECONDS) tree

  def tpe(t: Tree): Type = c.typecheck(t).tpe

  def is[T: TypeTag](t: Type) = t <:< typeOf[T]

  def duration(t: Tree) = q"scala.concurrent.duration.Duration($t, $seconds)"

  def configurable(t: Tree): Boolean = is[Configurable](tpe(q"0.asInstanceOf[$t]"))

  def generate(cd: ClassDef, owner: String, level: Int)(implicit out: PrintWriter): ClassDef = cd match {
    case ClassDef(m, name, a, Template(p, s, body)) =>
      ClassDef(m, name, a, Template(p, s, body map {
        case Initialized(vd) => generate(vd, s"$owner", level + 1)
        case d               => d
      }))

    case _ =>
      c.abort(cd.pos, "A anonymous class definition should be here")

  }

  def generate(vd: ValDef, owner: String, level: Int)(implicit out: PrintWriter): ValDef = vd match {
    case ValDef(mods, _, _, _) if mods.hasFlag(DEFERRED) =>
      c.abort(vd.pos, "value should be initialized")

    case ValDef(mods, _, _, _) if mods.hasFlag(MUTABLE) =>
      c.abort(vd.pos, "var should be val")

    case ValDef(mods, name, tpt, Block((cd: ClassDef) :: Nil, expr)) =>
      node(level)(s"$name") {
        ValDef(mods, name, tpt, Block(generate(cd, s"$owner.$name", level) :: Nil, expr))
      }

    case ValDef(mods, name, tpt, rhs) =>
      try {
        val e = c.eval(c.Expr[Any](Block(q"import scala.concurrent.duration._" :: Nil, rhs)))
        val t = c.typecheck(rhs).tpe

        leaf(level)(s"$name = ${value(t, e)}")
        ValDef(mods, name, tpt, get(t, s"$owner.$name"))
      } catch {
        case e: IllegalStateException => c.abort(vd.pos, e.getMessage)
        case _: Throwable             => vd
      }

    case _ =>
      c.abort(vd.pos, "Unexpect value definition")

  }

  def value(t: Type, a: Any) = t match {
    case _ if is[Long](t)           => bytes(a.asInstanceOf[Long])
    case _ if is[Duration](t)       => time(a.asInstanceOf[Duration])
    case _ if is[List[Long]](t)     => a.asInstanceOf[List[Long]].map(bytes).mkString("[", ", ", "]")
    case _ if is[List[Duration]](t) => a.asInstanceOf[List[Duration]].map(time).mkString("[", ", ", "]")
    case _ if is[List[_]](t)        => a.asInstanceOf[List[_]].map(e=>safeString(e.toString)).mkString("[", ", ", "]")
    case _ if is[Map[_,_]](t)       => a.asInstanceOf[Map[_,_]].map{case (k:Any,v:Any)=> k.toString + ": " + v.toString}.mkString("{ ", ", ", " }")
    case _                          => safeString(a.toString)
  }

  def quotationNeeded(input:String) = {
    List("$", "\"", "{", "}", "[", "]", ":", "=", ",", "+", "#", "`", "^", "?", "!", "@", "*", "&", "\\\\").exists{
      ch=> input.indexOf(ch) != -1
    }
  }

  def safeString(input:String) = {
    if (quotationNeeded(input)) ConfigImplUtil.renderJsonString(input) else input
  }

  def get(t: Type, path: String): Tree = t match {
    case _ if is[Boolean](t)        => q"_config.getBoolean($path)"
    case _ if is[Int](t)            => q"_config.getInt($path)"
    case _ if is[Long](t)           => q"_config.getBytes($path)"
    case _ if is[String](t)         => q"_config.getString($path)"
    case _ if is[Double](t)         => q"_config.getDouble($path)"
    case _ if is[Duration](t)       => duration(q"_config.getDuration($path, $seconds)")
    case _ if is[List[Boolean]](t)  => q"_config.getBooleanList($path).toList"
    case _ if is[List[Int]](t)      => q"_config.getIntList($path).toList"
    case _ if is[List[Long]](t)     => q"_config.getBytesList($path).toList"
    case _ if is[List[String]](t)   => q"_config.getStringList($path).toList"
    case _ if is[List[Double]](t)   => q"_config.getDoubleList($path).toList"
    case _ if is[List[Duration]](t) => q"_config.getDurationList($path, $seconds).toList.map {l => ${duration(q"l")} }"
    case _ if is[Map[String,String]](t) => q"_config.getObject($path).map{case(x,y)=>x.toString -> y.unwrapped.toString}.toMap[String,String]"
    case _                          => throw new IllegalStateException(s"Unsupported type: $t")
  }

  object Initialized {
    def unapply(t: Tree): Option[ValDef] = t match {
      case v @ ValDef(mods, _, _, _) if !mods.hasFlag(DEFERRED) => Some(v)
      case _                                                    => None
    }
  }

}

object Macro {

  val DefaultOutputDir  = "src/main/resources"
  val OutputDirSettings = "conf.output.dir="

  lazy val CONFIG = ConfigFactory.load()

  private val TAB = "  "

  def node[T](level: Int)(name: String)(f: => T)(implicit out: PrintWriter) = {
    out println s"${TAB * level}$name {"
    val r = f
    out println s"${TAB * level}}"
    r
  }

  def leaf(level: Int)(expr: String)(implicit out: PrintWriter) = {
    out.println(s"${TAB * level}$expr")
  }

  def bytes(l: Long): String = l match {
    case _ if l < 1024 || l % 1024 > 0                   => s"${l}B"
    case _ if l >= 1024 && l < 1024 * 1024               => s"${l / 1024}K"
    case _ if l >= 1024 * 1024 && l < 1024 * 1024 * 1024 => s"${l / (1024 * 1024)}M"
    case _                                               => s"${l / (1024 * 1024 * 1024)}G"
  }

  def time(d: Duration): String = d.unit match {
    case NANOSECONDS  => s"${d._1}ns"
    case MICROSECONDS => s"${d._1}us"
    case MILLISECONDS => s"${d._1}ms"
    case SECONDS      => s"${d._1}s"
    case MINUTES      => s"${d._1}m"
    case HOURS        => s"${d._1}h"
    case DAYS         => s"${d._1}d"
  }

}
