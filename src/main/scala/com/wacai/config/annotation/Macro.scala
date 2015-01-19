package com.wacai.config.annotation

import com.typesafe.config.ConfigFactory

import annotation.tailrec
import concurrent.duration.Duration
import reflect.macros.whitebox

object Macro {

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def tpe(tpt: Tree) = c.typecheck(q"0.asInstanceOf[$tpt]").tpe

    lazy val confType: Type = c.prefix.tree match {
      case q"new $_[$tpt]()" => tpe(tpt)
      case _                 => c.abort(c.enclosingPosition, "Invalid definition")
    }

    def get(t: Type, conf: Tree, path: String): Tree = t match {
      case _ if t <:< typeOf[Boolean]  => q"$conf.getBoolean($path)"
      case _ if t <:< typeOf[Int]      => q"$conf.getInt($path)"
      case _ if t <:< typeOf[Long]     => q"$conf.getBytes($path)"
      case _ if t <:< typeOf[String]   => q"$conf.getString($path)"
      case _ if t <:< typeOf[Double]   => q"$conf.getDouble($path)"
      case _ if t <:< typeOf[Duration] => q"scala.concurrent.duration.Duration($conf.getDuration($path, SECONDS), SECONDS)"
      case _                           => c.abort(c.enclosingPosition, s"Unsupported type: $t")
    }

    def valDefs(conf: Tree) = confType.members collect {
      case ms: MethodSymbol if ms.isAbstract && ms.paramLists.isEmpty => ms
    } map { m =>
      val owner = m.owner.name.toString.toList
      val name = m.name.toString.toList
      q"val ${m.name}:${m.returnType} = ${get(m.returnType, conf, path(owner, name))}"
    } toList

    def modify(body: List[Tree], parents: List[Tree]): List[Tree] = {
      if (parents.exists(t => tpe(t) <:< typeOf[Configurable])) {
        q"val _config = config" :: valDefs(q"this._config") ::: body
      } else {
        valDefs(reify(config) tree) ::: body
      }
    }

    val result = annottees.map(_.tree).toList match {
      case ModuleDef(mods, name, Template(parents, self, body)) :: Nil =>
        ModuleDef(mods, name, Template(parents ++ List(q"$confType"), self, modify(body, parents)))

      case ClassDef(mods, name, params, Template(parents, self, body)) :: _ =>
        parents foreach println
        ClassDef(mods, name, params, Template(parents ++ List(q"$confType"), self, modify(body, parents)))

      case _ =>
        c.abort(c.enclosingPosition, "Annotation is only supported on field")
    }

    c.Expr[Any](result)
  }

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

  lazy val config = ConfigFactory.load()

}

