package com.wacai.config.annotation

import com.typesafe.config.ConfigFactory
import com.wacai.config.CrossVersionDefs._

import annotation.tailrec
import concurrent.duration.Duration

object Macro {

  def impl(c: CrossVersionContext)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    implicit val tn2s = (_: TermName).toString
    implicit val ident2t = (tpt: Ident) => c.typecheck(q"var x: $tpt = _; x").tpe

    val ref = {
      val tpe = typeOf[Configurable]
      val con = reify {config} tree

      q"(if(this.isInstanceOf[$tpe]) this.asInstanceOf[$tpe].config else $con)"
    }

    def path(name: String) = q"com.wacai.config.annotation.Macro.path(getClass,$name)"

    def getConfig(name: String, t: Type) = t match {
      case _ if t <:< typeOf[Boolean]  => q"$ref.getBoolean(${path(name)})"
      case _ if t <:< typeOf[Int]      => q"$ref.getInt(${path(name)})"
      case _ if t <:< typeOf[Long]     => q"$ref.getBytes(${path(name)})"
      case _ if t <:< typeOf[String]   => q"$ref.getString(${path(name)})"
      case _ if t <:< typeOf[Double]   => q"$ref.getDouble(${path(name)})"
      case _ if t <:< typeOf[Duration] => q"Duration($ref.getDuration(${path(name)}, SECONDS), SECONDS)"
      case _                           => c.abort(c.enclosingPosition, s"Unsupported type: $t")
    }


    c.Expr[Any](annottees.map(_.tree).toList match {
      case ValDef(mod, name, tpt: Ident, _) :: Nil =>
        ValDef(mod, name, tpt, getConfig(name, tpt))

      case ValDef(mod, name, tpt, expr) :: Nil =>
        val tpe = c.typecheck(expr).tpe match {
          case ConstantType(v) => v.tpe
          case t               => t
        }
        ValDef(mod, name, tpt, getConfig(name, tpe))

      case _ =>
        c.abort(c.enclosingPosition, "Annotation is only supported on field")
    })
  }

  lazy val config = ConfigFactory.load()

  def path(c: Class[_], n: String): String = {
    @tailrec
    def uncapitalized(o: List[Char], d: Char = '.', n: List[Char] = Nil): String = (o, n) match {
      case (Nil, l) =>
        l.reverse mkString ""

      case (h :: t, Nil) =>
        uncapitalized(t, d, (if (h.isUpper) h.toLower else h) :: n)

      case (h :: t, _) =>
        uncapitalized(t, d, if (h.isUpper) h.toLower :: d :: n else h :: n)

    }

    s"${uncapitalized(c.getSimpleName.toList, '_')}.${uncapitalized(n.toList)}"
  }


}

