package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.AbstractGremlinSuite
import com.tinkerpop.gremlin.AbstractGraphProvider
import com.tinkerpop.gremlin.process.ProcessStandardSuite
import org.junit.runner.RunWith
import org.junit.runners.model.RunnerBuilder
import java.util.{Map => JMap}
import java.io.File
import org.apache.commons.configuration.Configuration
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.process.graph.filter._
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import com.tinkerpop.gremlin.structure.Element

object Tests {
  // actual tests are inside an object so that they are not executed twice
  class ScalaDedupTest extends DedupTest with StandardTest {
    override def get_g_V_both_dedup_name = ScalaGraph(g).V.both.dedup.value[String]("name")

    override def get_g_V_both_dedupXlangX_name =
      ScalaGraph(g).V.both
        .dedup(_.getProperty[String]("lang").orElse(null))
        .value[String]("name")
  }

  class ScalaFilterTest extends FilterTest with StandardTest {
    override def get_g_V_filterXfalseX = ScalaGraph(g).V.filter(_ => false)

    override def get_g_V_filterXtrueX = ScalaGraph(g).V.filter(_ => true)

    override def get_g_V_filterXlang_eq_javaX = 
      ScalaGraph(g).V.filter(_.getProperty("lang").orElse("none") == "java")

    override def get_g_v1_out_filterXage_gt_30X(v1Id: AnyRef) = 
      ScalaGraph(g).v(v1Id).get.out.filter(_.getProperty("age").orElse(0) > 30)

    override def get_g_V_filterXname_startsWith_m_OR_name_startsWith_pX = ScalaGraph(g).V.filter { v =>
      val name = v.getValue[String]("name")
      name.startsWith("m") || name.startsWith("p")
    }
  }

  class ScalaExceptTest extends ExceptTest with StandardTest {
    override def get_g_v1_out_exceptXg_v2X(v1Id: AnyRef, v2Id: AnyRef) = 
      ScalaGraph(g).v(v1Id).get.out.except(g.v(v2Id))
  
    override def get_g_v1_out_aggregateXxX_out_exceptXxX(v1Id: AnyRef) = 
      ScalaGraph(g).v(v1Id).get.out.aggregate("x").out.exceptVar("x")

    override def get_g_v1_outXcreatedX_inXcreatedX_exceptXg_v1X_valueXnameX(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.out("created").in("created")
        .except(g.v(v1Id)).value[String]("name")
  }

  class ScalaSimplePathTest extends SimplePathTest with StandardTest {
    override def get_g_v1_outXcreatedX_inXcreatedX_simplePath(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.out("created").in("created").simplePath
  }

  class ScalaCyclicPathTest extends CyclicPathTest with StandardTest {
    override def get_g_v1_outXcreatedX_inXcreatedX_cyclicPath(v1Id: AnyRef) =
      ScalaGraph(g).v(v1Id).get.out("created").in("created").cyclicPath
  }

  class ScalaHasTest extends HasTest with StandardTest {
    override def get_g_V_hasXname_markoX = ScalaGraph(g).V.has("name", "marko")

    override def get_g_V_hasXname_blahX = ScalaGraph(g).V.has("name", "blah")

    override def get_g_V_hasXblahX = ScalaGraph(g).V.has("blah")

    override def get_g_v1_out_hasXid_2X(v1Id: AnyRef, v2Id: AnyRef) = 
      ScalaGraph(g).v(v1Id).get.out().has(Element.ID, v2Id)

    override def get_g_V_hasXage_gt_30X = ScalaGraph(g).V.has("age", T.gt, 30)

    override def get_g_E_hasXlabelXknowsX = ScalaGraph(g).E.has("label", "knows")

    override def get_g_E_hasXlabelXknows_createdX =
      ScalaGraph(g).E.has("label", T.in, List("knows", "created"))
  }
}

import Tests._
class ScalaProcessStandardSuite(clazz: Class[_], builder: RunnerBuilder) 
  extends AbstractGremlinSuite(clazz, builder, Array(
    classOf[ScalaDedupTest],
    classOf[ScalaFilterTest],
    classOf[ScalaExceptTest],
    classOf[ScalaSimplePathTest],
    classOf[ScalaCyclicPathTest],
    classOf[ScalaHasTest]
  ))

trait StandardTest {
  implicit def toTraversal[S,E](gs: GremlinScala[_,E]): Traversal[S,E] = gs.traversal.asInstanceOf[Traversal[S,E]]
}

@RunWith(classOf[ScalaProcessStandardSuite])
@AbstractGremlinSuite.GraphProviderClass(classOf[ScalaTinkerGraphProcessStandardTest])
class ScalaTinkerGraphProcessStandardTest extends AbstractGraphProvider {
  override def getBaseConfiguration(graphName: String): JMap[String, AnyRef] =
    Map("gremlin.graph" -> classOf[TinkerGraph].getName)

  override def clear(graph: Graph, configuration: Configuration): Unit = 
    Option(graph) map { graph ⇒ 
      graph.close()
      if (configuration.containsKey("gremlin.tg.directory"))
        new File(configuration.getString("gremlin.tg.directory")).delete()
    }

}
