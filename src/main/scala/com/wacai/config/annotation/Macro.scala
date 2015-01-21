package com.wacai.config.annotation

import com.typesafe.config.ConfigFactory

import annotation.tailrec
import concurrent.duration._
import reflect.macros.whitebox

object Macro {

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def tpe(tpt: Tree) = c.typecheck(q"0.asInstanceOf[$tpt]").tpe

    lazy val confType: Tree = c.prefix.tree match {
      case q"new $_[$tpt]()" => tpt
      case _                 => c.abort(c.enclosingPosition, "Invalid definition")
    }

    lazy val seconds = reify(SECONDS) tree

    def duration(t: Tree) = q"scala.concurrent.duration.Duration($t, $seconds)"

    def get(t: Type, conf: Tree, path: String): Tree = t match {
      case _ if t <:< typeOf[Boolean]  => q"$conf.getBoolean($path)"
      case _ if t <:< typeOf[Int]      => q"$conf.getInt($path)"
      case _ if t <:< typeOf[Long]     => q"$conf.getBytes($path)"
      case _ if t <:< typeOf[String]   => q"$conf.getString($path)"
      case _ if t <:< typeOf[Double]   => q"$conf.getDouble($path)"
      case _ if t <:< typeOf[Duration] => duration(q"$conf.getDuration($path, $seconds)")
      case _                           => c.abort(c.enclosingPosition, s"Unsupported type: $t")
    }

    def valDefs(conf: Tree) = tpe(confType).members collect {
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
        ModuleDef(mods, name, Template(parents +? q"$confType", self, modify(body, parents)))

      case ClassDef(mods, name, params, Template(parents, self, body)) :: Nil =>
        ClassDef(mods, name, params, Template(parents +? q"$confType", self, modify(body, parents)))

      case ClassDef(mods, name, params, Template(parents, self, body)) :: obj :: Nil =>
        val cls = ClassDef(mods, name, params, Template(parents +? q"$confType", self, modify(body, parents)))
        q"..${cls :: obj :: Nil}"

      case _ =>
        c.abort(c.enclosingPosition, "Annotation is only supported on class or module class")
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

  implicit class AppendIfAbsent[E](list: List[E]) {
    def +?(e: E): List[E] = {
      // TODO improve ugly equals by toString
      if (list.exists{i => i.toString == e.toString}) list else list ::: List(e)
    }
  }

}

