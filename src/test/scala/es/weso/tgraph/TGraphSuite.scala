package es.weso.tgraph

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import es.weso.tgraph._
import es.weso.tgraph.TGraphImpl._
import scalax.collection.immutable.Graph
import scalax.collection.GraphEdge._


@RunWith(classOf[JUnitRunner])
class TGraphSuite extends FunSuite {

  test("empty graph") {
    val g = new TGraphImpl(Graph[Int,Triple]())
    assert(g.isEmpty)
  }
  
  test("simple triple contains origin") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val g1 = g0.addTriple(('a','b','c'))
    assert(g1.nodes.contains('a'))
  }

  test("simple triple contains edge") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val g1 = g0.addTriple(('a','b','c'))
    assert(g1.nodes.contains('b'))
  }

  test("simple triple contains destiny") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val g1 = g0.addTriple(('a','b','c'))
    assert(g1.nodes.contains('c'))
  }


  test("decomp with two triples, a") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val g1 = g0.addTriple('a','b','c')
    val g2 = g1.addTriple('a','b','d')
    assert(g2.decomp('a').get._1.pred === Set())
    assert(g2.decomp('a').get._1.succ === Set(('b','c'),('b','d')))
    assert(g2.decomp('a').get._1.rels === Set())
  }

  test("decomp with two triples, b") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val g1 = g0.addTriple('a','b','c')
    val g2 = g1.addTriple('a','b','d')
    assert(g2.decomp('b').get._1.pred === Set())
    assert(g2.decomp('b').get._1.succ === Set())
    assert(g2.decomp('b').get._1.rels === Set(('a','c'),('a','d')))
  }

  test("decomp with two triples, c") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val g1 = g0.addTriple('a','b','c')
    val g2 = g1.addTriple('a','b','d')
    assert(g2.decomp('c').get._1.pred === Set(('a','b')))
    assert(g2.decomp('c').get._1.succ === Set())
    assert(g2.decomp('c').get._1.rels === Set())
  }

  test("triples, a b c") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val g1 = g0.addTriple('a','b','c')
    assert(g1.triples == Set(('a','b','c')))
  }

  test("triples, abc, abd") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val g1 = g0.addTriple('a','b','c')
    val g2 = g1.addTriple('a','b','d')
    assert(g2.triples == Set(('a','b','c'),('a','b','d')))
  }

  test("foldTGraph length") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val g1 = g0.addTriple('b','c','a')
    val g2 = g1.addTriple('a','b','d')
    val g = g2
    val result = g.foldTGraph(0)((r,_) => 1 + r)
    assert(result === 4)
  }

  test("foldTGraph sum") {
    val g0 = new TGraphImpl(Graph[Int,Triple]())
    val g1 = g0.addTriple(1,2,3)
    val g2 = g1.addTriple(3,2,1)
    val g = g2
    val result = g.foldTGraph(0)((r,ctx) => r + ctx.node)
    assert(result === 6)
  }

  test("foldTGraph sum with 2 nodes") {
    val g0 = new TGraphImpl(Graph[Int,Triple]())
    val g1 = g0.addTriple(1,2,1)
    val g = g1
    val result = g.foldTGraph(0)((r,ctx) => r + ctx.node)
    assert(result === 3)
  }

  test("foldTGraphOrd min") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val g1 = g0.addTriple('b','c','a')
    val g2 = g1.addTriple('a','b','d')
    val g = g2
    val ls = g.foldTGraphOrd(List[Char]())((r,ctx) => ctx.node :: r)
    assert(ls === List('d','c','b','a'))
  }

  test("foldTGraphOrd min loop") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val g1 = g0.addTriple('a','b','c')
    val g2 = g1.addTriple('c','b','d')
    val g3 = g2.addTriple('d','b','a')
    val g = g3
    val ord = new Ordering[Char] { def compare(x :Char,y :Char):Int = x compare y } 
    val ls = g.foldTGraphOrd(List[Char]())((r,ctx) => ctx.node :: r)(ord)
    assert(ls === List('d','c','b','a'))
  }

  test("foldTGraphOrd max") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val g1 = g0.addTriple('b','c','a')
    val g2 = g1.addTriple('a','b','d')
    val g = g2
    val ord = new Ordering[Char] { def compare(x :Char,y :Char):Int = y - x }
    assert(g.nodes.min(ord) === 'd')
    val ls = g.foldTGraphOrd(List[Char]())((r,ctx) => ctx.node :: r)(ord)
    assert(ls === List('a','b','c','d'))
  }

  test("map empty") {
    val g0 = new TGraphImpl(Graph[Int,Triple]())
    val g = g0.map((x:Int) => x + 1)
    assert(g.nodes === Set())
  }

  test("mapGraph (+1)") {
    val g0 = new TGraphImpl(Graph[Int,Triple]())
    val g1 = g0.addTriple(1,2,3)
    val g2 = g1.addTriple(3,2,1)

    val gN = g2.map((x:Int) => x + 1)
    assert(gN.nodes === Set(2,3,4))
  }

  test("map toString") {
    val g0 = new TGraphImpl(Graph[Int,Triple]())
    val g1 = g0.addTriple(1,2,3)
    val g2 = g1.addTriple(3,2,1)

    val gN = g2.map((x:Int) => x.toString)
    assert(gN.nodes === Set("1","2","3"))
  }

  test("extend empty with a list of succ") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val e = Set[(Char,Char)]()
    val ctx = TContext('a',e,Set(('b','c'),('b','d')),e)
    val g1 = g0.extend(ctx)
    assert(g1.triples === Set(('a','b','c'),('a','b','d')))
  }

  test("extend empty with a list of pred") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val e = Set[(Char,Char)]()
    val ctx = TContext('a',Set(('b','c'),('b','d')),e,e)
    val g1 = g0.extend(ctx)
    assert(g1.triples === Set(('b','c','a'),('b','d','a')))
  }

  test("extend empty with a list of rels") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val e = Set[(Char,Char)]()
    val ctx = TContext('a',e,e,Set(('b','c'),('b','d')))
    val g1 = g0.extend(ctx)
    assert(g1.triples === Set(('b','a','c'),('b','a','d')))
  }

  test("extend empty with succ, rels and pred") {
    val g0 = new TGraphImpl(Graph[Char,Triple]())
    val e = Set[(Char,Char)]()
    val ctx = TContext('x',
    		Set(('a','b'),('c','d')),
    		Set(('e','f'),('g','h')),
    		Set(('i','j'),('k','l'))
        )
    val g1 = g0.extend(ctx)
    assert(g1.triples === 
      	Set(('a','b','x'),('c','d','x'),
      	    ('x','e','f'),('x','g','h'),
      	    ('i','x','j'),('k','x','l')))
  }
}
