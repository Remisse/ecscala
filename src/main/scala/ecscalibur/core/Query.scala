package ecscalibur.core

import ecscalibur.core.component.{ComponentType, ComponentId, Component, CSeq}
import ecscalibur.core.component.tpe.*
import izumi.reflect.Tag
import ecscalibur.core.Entity
import CSeq.Extensions.*
import ecscalibur.util.array.*
import ecscalibur.core.archetype.{ArchetypeManager, Signature}
import Signature.Extensions.*
import ecscalibur.core.archetype.Archetypes.Archetype
import ecscalibur.core.component.Rw

object queries:
  opaque type Query = () => Unit

  private[core] inline def make(q: () => Unit): Query = q

  extension (q: Query)
    inline def apply: Unit = q()

inline def query(using ArchetypeManager): QueryBuilder = new QueryBuilderImpl()

import ecscalibur.core.queries.Query
trait QueryBuilder:
  infix def withNone(types: ComponentType*): QueryBuilder

  infix def withAny(types: ComponentType*): QueryBuilder

  infix def withAll[C0 <: Component: Tag](f: (Entity, C0) => Unit): Query
  infix def withAll[C0 <: Component: Tag, C1 <: Component: Tag](f: (Entity, C0, C1) => Unit): Query
  infix def withAll[C0 <: Component: Tag, C1 <: Component: Tag, C2 <: Component: Tag](
      f: (Entity, C0, C1, C2) => Unit
  ): Query
  infix def withAll[
      C0 <: Component: Tag,
      C1 <: Component: Tag,
      C2 <: Component: Tag,
      C3 <: Component: Tag
  ](f: (Entity, C0, C1, C2, C3) => Unit): Query
  infix def withAll[
      C0 <: Component: Tag,
      C1 <: Component: Tag,
      C2 <: Component: Tag,
      C3 <: Component: Tag,
      C4 <: Component: Tag
  ](f: (Entity, C0, C1, C2, C3, C4) => Unit): Query
  infix def withAll[
      C0 <: Component: Tag,
      C1 <: Component: Tag,
      C2 <: Component: Tag,
      C3 <: Component: Tag,
      C4 <: Component: Tag,
      C5 <: Component: Tag
  ](f: (Entity, C0, C1, C2, C3, C4, C5) => Unit): Query
  infix def withAll[
      C0 <: Component: Tag,
      C1 <: Component: Tag,
      C2 <: Component: Tag,
      C3 <: Component: Tag,
      C4 <: Component: Tag,
      C5 <: Component: Tag,
      C6 <: Component: Tag
  ](f: (Entity, C0, C1, C2, C3, C4, C5, C6) => Unit): Query

class QueryBuilderImpl(using am: ArchetypeManager) extends QueryBuilder:
  private var selected: Signature = Signature.Nil
  private var _none: Signature = Signature.Nil
  private var _any: Signature = Signature.Nil

  private inline def multipleCallsErrorMsg(methodName: String) =
    s"Called '$methodName' multiple times."

  override infix def withNone(types: ComponentType*): QueryBuilder =
    ensureFirstCallToNone
    _none = Signature(types*)
    this

  override infix def withAny(types: ComponentType*): QueryBuilder =
    ensureFirstCallToAny
    _any = Signature(types*)
    this

  override infix def withAll[C0 <: Component: Tag](f: (Entity, C0) => Unit): Query =
    val trueIds = Array(idRw[C0])
    selected = trueIds.toSignature
    queries.make(() => am.iterate(matches, selected): (e, components, arch) =>
      f(e, findOfType[C0](trueIds(0))(components, arch, e)))

  override infix def withAll[C0 <: Component: Tag, C1 <: Component: Tag](
      f: (Entity, C0, C1) => Unit
  ): Query =
    val trueIds = Array(idRw[C0], idRw[C1])
    selected = trueIds.toSignature
    queries.make(() => am.iterate(matches, selected): (e, components, arch) =>
      f(
        e,
        findOfType[C0](trueIds(0))(components, arch, e),
        findOfType[C1](trueIds(1))(components, arch, e),
      ))

  override infix def withAll[C0 <: Component: Tag, C1 <: Component: Tag, C2 <: Component: Tag](
      f: (Entity, C0, C1, C2) => Unit
  ): Query =
    val trueIds = Array(idRw[C0], idRw[C1], idRw[C2])
    selected = trueIds.toSignature
    queries.make(() => am.iterate(matches, selected): (e, components, arch) =>
      f(
        e,
        findOfType[C0](trueIds(0))(components, arch, e),
        findOfType[C1](trueIds(1))(components, arch, e),
        findOfType[C2](trueIds(2))(components, arch, e),
      ))

  override infix def withAll[
      C0 <: Component: Tag,
      C1 <: Component: Tag,
      C2 <: Component: Tag,
      C3 <: Component: Tag
  ](f: (Entity, C0, C1, C2, C3) => Unit): Query =
    val trueIds = Array(idRw[C0], idRw[C1], idRw[C2], idRw[C3])
    selected = trueIds.toSignature
    queries.make(() => am.iterate(matches, selected): (e, components, arch) =>
      f(
        e,
        findOfType[C0](trueIds(0))(components, arch, e),
        findOfType[C1](trueIds(1))(components, arch, e),
        findOfType[C2](trueIds(2))(components, arch, e),
        findOfType[C3](trueIds(3))(components, arch, e),
      ))

  override infix def withAll[
      C0 <: Component: Tag,
      C1 <: Component: Tag,
      C2 <: Component: Tag,
      C3 <: Component: Tag,
      C4 <: Component: Tag
  ](f: (Entity, C0, C1, C2, C3, C4) => Unit): Query =
    val trueIds = Array(idRw[C0], idRw[C1], idRw[C2], idRw[C3], idRw[C4])
    selected = trueIds.toSignature
    queries.make(() => am.iterate(matches, selected): (e, components, arch) =>
      f(
        e,
        findOfType[C0](trueIds(0))(components, arch, e),
        findOfType[C1](trueIds(1))(components, arch, e),
        findOfType[C2](trueIds(2))(components, arch, e),
        findOfType[C3](trueIds(3))(components, arch, e),
        findOfType[C4](trueIds(4))(components, arch, e),
      ))

  override infix def withAll[
      C0 <: Component: Tag,
      C1 <: Component: Tag,
      C2 <: Component: Tag,
      C3 <: Component: Tag,
      C4 <: Component: Tag,
      C5 <: Component: Tag
  ](f: (Entity, C0, C1, C2, C3, C4, C5) => Unit): Query =
    val trueIds = Array(idRw[C0], idRw[C1], idRw[C2], idRw[C3], idRw[C4], idRw[C5])
    selected = trueIds.toSignature
    queries.make(() => am.iterate(matches, selected): (e, components, arch) =>
      f(
        e,
        findOfType[C0](trueIds(0))(components, arch, e),
        findOfType[C1](trueIds(1))(components, arch, e),
        findOfType[C2](trueIds(2))(components, arch, e),
        findOfType[C3](trueIds(3))(components, arch, e),
        findOfType[C4](trueIds(4))(components, arch, e),
        findOfType[C5](trueIds(5))(components, arch, e),
      ))

  override infix def withAll[
      C0 <: Component: Tag,
      C1 <: Component: Tag,
      C2 <: Component: Tag,
      C3 <: Component: Tag,
      C4 <: Component: Tag,
      C5 <: Component: Tag,
      C6 <: Component: Tag
  ](f: (Entity, C0, C1, C2, C3, C4, C5, C6) => Unit): Query =
    val trueIds = Array(idRw[C0], idRw[C1], idRw[C2], idRw[C3], idRw[C4], idRw[C5], idRw[C6])
    selected = trueIds.toSignature
    queries.make(() => am.iterate(matches, selected): (e, components, arch) =>
      f(
        e,
        findOfType[C0](trueIds(0))(components, arch, e),
        findOfType[C1](trueIds(1))(components, arch, e),
        findOfType[C2](trueIds(2))(components, arch, e),
        findOfType[C3](trueIds(3))(components, arch, e),
        findOfType[C4](trueIds(4))(components, arch, e),
        findOfType[C5](trueIds(5))(components, arch, e),
        findOfType[C6](trueIds(6))(components, arch, e),
      ))

  private inline def matches(s: Signature): Boolean =
    (selected.isNil || s.containsAll(selected)) &&
      (_none.isNil || !s.containsAny(_none)) &&
      (_any.isNil || s.containsAny(_any))

  private inline def ensureFirstCallToNone: Unit =
    require(_none.isNil, multipleCallsErrorMsg("none"))

  private inline def ensureFirstCallToAny: Unit =
    require(_any.isNil, multipleCallsErrorMsg("any"))

  private inline def findOfType[C <: Component: Tag](idrw: ComponentId)(components: CSeq, arch: Archetype, e: Entity): C =
    val c = components.underlying.aFindUnsafe(_.typeId == idrw)
    (if id0K[C] == ~Rw then Rw(c)(arch, e) else c).asInstanceOf[C]
