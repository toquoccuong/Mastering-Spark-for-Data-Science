/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gzet.community

import io.gzet.community.clustering.wcc.WCCDetection
import io.gzet.test.SparkFunSuite
import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx.{Graph, Edge}

import scala.io.Source

class GzetCommunitiesTest extends SparkFunSuite {

  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  localTest("WCC communities") { spark =>

    val lines = Source.fromInputStream(getClass.getResourceAsStream("/local-edges.csv")).getLines().zipWithIndex.filter(_._2 > 0).map(_._1).toSeq
    val sc = spark.sparkContext
    val edges = sc.parallelize(lines).map({ line =>
      val a = line.split(",").map(_.toLong).sorted
      Edge(a.head, a.last, 1L)
    }).distinct()

    val graph = Graph.fromEdges(edges, 0L)

    graph.triplets.take(2).foreach(println)
    val communities = new WCCDetection(1).run(graph, sc)
    communities.map(_._2 -> 1).reduceByKey(_+_).collectAsMap() should be(Map(5L -> 5, 15L -> 6, 21L -> 5))
  }
}
