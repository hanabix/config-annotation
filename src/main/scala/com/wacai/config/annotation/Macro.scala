package com.wacai.config.annotation

import java.io.{PrintWriter, File}

import com.typesafe.config.{Config, ConfigFactory}

import annotation.tailrec
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
      case (ClassDef(mods, name, a, Template(parent, s, body))) :: Nil if mods.hasFlag(DEFAULTPARAM | TRAIT) =>

        implicit val out = new PrintWriter(new File(outputDir, s"$name"))

        def newBody(implicit conf: Tree): List[Tree] = body.map {
          case Initialized(vd @ ValDef(_, _, _, rhs)) if !is[Config](tpe(rhs)) => generate(vd, s"$name", 1)
          case t                                                               => t
        }

        try {
          node(0)(s"$name") {
            val nb = if (body exists configRefDef) {
              q"private val _config = config" :: newBody(q"_config")
            } else {
              newBody(reify(CONFIG).tree)
            }

            ClassDef(mods, name, a, Template(parent, s, nb))
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

  def configRefDef(t: Tree): Boolean = t match {
    case DefDef(mods, TermName("config"), _, _, _, rhs) => is[Config](tpe(rhs))
    case _                                              => false
  }

  def generate(cd: ClassDef, owner: String, level: Int)(implicit conf: Tree, out: PrintWriter): ClassDef = cd match {
    case ClassDef(m, name, a, Template(p, s, body)) =>
      ClassDef(m, name, a, Template(p, s, body map {
        case Initialized(vd) => generate(vd, s"$owner", level + 1)
        case d               => d
      }))

    case _ =>
      c.abort(cd.pos, "A anonymous class definition should be here")

  }

  def generate(vd: ValDef, owner: String, level: Int)(implicit conf: Tree, out: PrintWriter): ValDef = vd match {
    case ValDef(mods, _, _, _) if mods.hasFlag(DEFERRED) =>
      c.abort(vd.pos, "value should be initialized")

    case ValDef(mods, _, _, _) if mods.hasFlag(MUTABLE) =>
      c.abort(vd.pos, "var should be val")

    case ValDef(mods, name, tpt, Block((cd: ClassDef) :: Nil, expr)) =>
      node(level)(s"$name") {
        ValDef(mods, name, tpt, Block(generate(cd, s"$owner.$name", level) :: Nil, expr))
      }


    case ValDef(mods, name, tpt, rhs) =>
      val e = c.eval(c.Expr[Any](Block(q"import scala.concurrent.duration._" :: Nil, rhs)))
      leaf(level)(e match {
        case l: Long     => s"$name = ${bytes(l)}"
        case d: Duration => s"$name = ${time(d)}"
        case v           => s"$name = $v"
      })

      ValDef(mods, name, tpt, get(c.typecheck(rhs).tpe, conf, s"$owner.$name"))

    case _ =>
      c.abort(vd.pos, "Unexpect value definition")

  }

  def get(t: Type, conf: Tree, path: String): Tree = t match {
    case _ if is[Boolean](t)  => q"$conf.getBoolean($path)"
    case _ if is[Int](t)      => q"$conf.getInt($path)"
    case _ if is[Long](t)     => q"$conf.getBytes($path)"
    case _ if is[String](t)   => q"$conf.getString($path)"
    case _ if is[Double](t)   => q"$conf.getDouble($path)"
    case _ if is[Duration](t) => duration(q"$conf.getDuration($path, $seconds)")
    case _                    => c.abort(c.enclosingPosition, s"Unsupported type: $t")
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

  def path(c: List[Char], n: List[Char]): String = {
    @tailrec
    def uncapitalized(o: List[Char], d: Char = '.', r: List[Char] = Nil): String = (o, r) match {
      case (Nil, l)                          => l.reverse mkString ""
      case (h :: t, _) if !h.isLetterOrDigit => uncapitalized(t, d, r)
      case (h :: t, Nil)                     => uncapitalized(t, d, (if (h.isUpper) h.toLower else h) :: r)
      case (h :: t, _)                       => uncapitalized(t, d, if (h.isUpper) h.toLower :: d :: r else h :: r)
    }

    s"${uncapitalized(c, '_')}.${uncapitalized(n)}"
  }

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
