package ecscalibur.core.component

import ecscalibur.core.component.tpe.*
import ecscalibur.util.array.*
import ecscalibur.error.IllegalTypeParameterException
import izumi.reflect.Tag

opaque type CSeq = Array[Component]

object CSeq:
  def empty = CSeq(Array.empty[Component])

  def apply(comps: Component*): CSeq = comps.toArray
  def apply(comps: Array[Component]): CSeq = comps
  def apply(comps: Iterable[Component]): CSeq = comps.toArray

  object Extensions:
    extension (l: CSeq)
      inline def apply(i: Int): Component = l(i)

      inline def update(i: Int, c: Component) = l.update(i, c)

      inline def underlying: Array[Component] = l

      inline def toTypes: Array[ComponentId] = l.aMap(_.typeId)

      def readonly[T <: Component: Tag]: T =
        val typeParamId = id0K[T]
        val idx = l.aIndexWhere(_.typeId == typeParamId)
        if idx == -1 then
          throw IllegalTypeParameterException(
            s"No component of class ${summon[Tag[T]]} found."
          )
        l(idx).asInstanceOf[T]

      def readwrite[T <: Component: Tag]: Rw[T] = l.readonly[T].asInstanceOf[Rw[T]]
