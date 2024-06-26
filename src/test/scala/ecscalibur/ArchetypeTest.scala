package ecscalibur.core

import ecscalibur.error.IllegalTypeParameterException
import ecscalibur.util.sizeof.sizeOf
import org.scalatest.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.*

import component.*
import CSeq.Extensions.*
import archetype.Signature
import archetype.Archetypes.{Aggregate, Fragment}
import ecscalibur.core.archetype.Archetypes.Archetype.DefaultFragmentSizeBytes

object ArchetypeTest:
  private abstract class ArchetypeFixture(components: Component*)(nEntities: Int):
    val componentIds = components.map(_.typeId).toArray
    val entities = (0 until nEntities).map(Entity(_)).toVector
    val nextEntity = Entity(entities.length)

  private class StandardArchetypeFixture(components: Component*)(nEntities: Int = 100, fragmentSize: Long = DefaultFragmentSizeBytes) extends ArchetypeFixture(components*)(nEntities):
    val archetype = Aggregate(Signature(componentIds))(fragmentSize)
    for e <- entities do archetype.add(e, CSeq(components*))

  private class StandardFragmentFixture(components: Component*)(nEntities: Int = 100, maxEntities: Int = 100) extends ArchetypeFixture(components*)(nEntities):
    require(nEntities <= maxEntities)
    val fragment = Fragment(Signature(componentIds), maxEntities)
    for e <- entities do fragment.add(e, CSeq(components*))

class ArchetypeTest extends AnyFlatSpec with should.Matchers:
  import ecscalibur.testutil.testclasses.*

  inline val KindaSmallFragmentSizeBytes = 64
  inline val ExtremelySmallFragmentSizeBytes = 1

  "An Aggregate archetype with no signature" should "throw when created" in:
    an[IllegalArgumentException] should be thrownBy (Aggregate()(DefaultFragmentSizeBytes))

  "An Aggregate archetype" should "be identified by the component classes it holds" in:
    val archetype = Aggregate(C1, C2)(DefaultFragmentSizeBytes)
    archetype.signature shouldBe Signature(C1, C2)
    archetype.signature shouldBe Signature(C2, C1)
    archetype.signature shouldNot be(Signature(C1))
    archetype.signature shouldNot be(Signature(C1, C2, C3))

  it should "have a signature made of distinct component types only" in:
    an[IllegalArgumentException] shouldBe thrownBy(Aggregate(C1, C1, C2)(DefaultFragmentSizeBytes))

  it should "correctly store entities and their components" in:
    val fixture = ArchetypeTest.StandardArchetypeFixture(C1(), C2())(nEntities = 1)
    fixture.archetype.contains(fixture.entities(0)) shouldBe true

  it should "not accept entities that do not satisfy its signature" in:
    val fixture = ArchetypeTest.StandardArchetypeFixture(Value(0), C2())(nEntities = 0)
    val entity = fixture.nextEntity
    an[IllegalArgumentException] should be thrownBy (fixture.archetype.add(entity, CSeq(C2())))
    an[IllegalArgumentException] should be thrownBy (
      fixture.archetype.add(entity, CSeq(Value(1), C2(), C3()))
    )

  it should "correctly remove stored entities" in:
    val fixture = ArchetypeTest.StandardArchetypeFixture(C1())(nEntities = 1)
    val entity = fixture.entities(0)
    fixture.archetype.remove(entity)
    fixture.archetype.contains(entity) shouldBe false

  it should "return all components of an entity when that entity is removed" in:
    val components = Seq(Value(0), C1(), C2())
    val fixture = ArchetypeTest.StandardArchetypeFixture(components*)(nEntities = 1)
    val entity = fixture.entities(0)
    fixture.archetype.remove(entity).underlying should contain allElementsOf (components)
    fixture.archetype.contains(entity) shouldBe false

  it should "correctly soft-remove entities" in:
    val fixture = ArchetypeTest.StandardArchetypeFixture(C1())(nEntities = 1)
    val entity = fixture.entities(0)
    fixture.archetype.softRemove(entity)
    fixture.archetype.contains(entity) shouldBe false

  it should "correctly iterate over all selected entities and components in read-only mode" in:
    val v = Value(2)
    inline val nEntities = 2
    val fixture = ArchetypeTest.StandardArchetypeFixture(v, C1())(nEntities = nEntities)
    var sum = 0
    fixture.archetype.iterate(Signature(Value)): (e, comps, _) =>
      val c = comps.readonly[Value]
      an[IllegalTypeParameterException] should be thrownBy (comps.readwrite[C2])
      sum += c.x
    sum shouldBe v.x * nEntities

  it should "correctly perform load balancing when fragments reach their limit" in:
    noException shouldBe thrownBy (ArchetypeTest.StandardArchetypeFixture(Value(0))(nEntities = 10000))

  it should "correctly return its Fragments" in:
    val c = C1()
    val fixture = ArchetypeTest.StandardArchetypeFixture(c)(nEntities = 1000, KindaSmallFragmentSizeBytes)
    val expectedNumberOfFragments = fixture.entities.length / (KindaSmallFragmentSizeBytes / sizeOf(c))
    fixture.archetype.fragments.size shouldBe expectedNumberOfFragments

  it should "remove all empty Fragments but one" in:
    val c = C1()
    val fixture = ArchetypeTest.StandardArchetypeFixture(c)(nEntities = 1000, KindaSmallFragmentSizeBytes)
    for e <- fixture.entities do fixture.archetype.remove(e)
    fixture.archetype.fragments.size shouldBe 1

  it should "throw if the size of a component is greather than the maximum size limit" in:
    an[IllegalStateException] shouldBe thrownBy (ArchetypeTest.StandardArchetypeFixture(Value(0))(nEntities = 1, ExtremelySmallFragmentSizeBytes))

  "A Fragment" should "correctly report whether it is full or not" in:
    val c = C1()
    val fixture = ArchetypeTest.StandardFragmentFixture(c)(nEntities = 0, maxEntities = 1)
    val frag = fixture.fragment
    frag.isEmpty shouldBe true
    frag.isFull shouldBe !frag.isEmpty
    val e = fixture.nextEntity
    frag.add(e, CSeq(c))
    frag.isEmpty shouldBe false
    frag.isFull shouldBe !frag.isEmpty
    frag.remove(e)
    frag.isEmpty shouldBe true
    frag.isFull shouldBe !frag.isEmpty

  it should "throw when attempting to add more entities than it can store" in:
    val c = C1()
    val fixture = ArchetypeTest.StandardFragmentFixture(c)(nEntities = 1, maxEntities = 1)
    an[IllegalArgumentException] should be thrownBy (fixture.fragment.add(fixture.nextEntity, CSeq(c)))
