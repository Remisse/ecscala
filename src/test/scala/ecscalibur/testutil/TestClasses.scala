package ecscalibur.testutil

import ecscalibur.core.component.{Component, ComponentType}
import ecscalibur.core.component.annotations.component

object testclasses:
  @component
  class C1 extends Component
  object C1 extends ComponentType

  @component
  class C2 extends Component
  object C2 extends ComponentType

  @component
  class C3 extends Component
  object C3 extends ComponentType

  @component
  class C4 extends Component
  object C4 extends ComponentType

  @component
  class C5 extends Component
  object C5 extends ComponentType

  @component
  class C6 extends Component
  object C6 extends ComponentType

  @component
  case class Value(x: Int) extends Component
  object Value extends ComponentType

  class NotAnnotated extends Component
  object NotAnnotated extends ComponentType

  case class IntWrapper(n: Int)

  @component
  class OneKinded[T <: Component] extends Component
  object OneKinded extends ComponentType
