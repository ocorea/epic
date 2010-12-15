package scalanlp.parser
package projections

import org.junit.runner.RunWith;
import org.scalatest._;
import org.scalatest.junit._;
import org.scalatest.prop._;

/**
 *
 * @author dlwh
 */
@RunWith(classOf[JUnitRunner])
class AnchoredRuleScorerTest  extends ParserTestHarness with FunSuite {

  test("We can parse using span scorer") {
    val gen = ParserTestHarness.simpleParser;
    val projections = new ProjectionIndexer(gen.builder.grammar.index,gen.builder.grammar.index,identity[String])
    val f = new AnchoredRuleScorerFactory(gen.builder.withCharts(ParseChart.logProb), projections, Double.NegativeInfinity);
    val zero = new CKYChartBuilder[ParseChart.LogProbabilityParseChart,String,String](gen.builder.root,new ZeroLexicon(gen.builder.lexicon), new ZeroGrammar(gen.builder.grammar),ParseChart.logProb);
    val fnext = new AnchoredRuleScorerFactory(zero, projections, Double.NegativeInfinity);
    for( (t,w) <- getTestTrees()) try {
      val gent = gen(w);
      val scorer = f.mkSpanScorer(w);
      val ginside = zero.buildInsideChart(w,scorer);
      println(w + " " + ginside.labelScore(0,w.length,gen.builder.root));
      val (goutside,goutsideP) = zero.buildOutsideChart(ginside,scorer)
      val scorer2 = fnext.buildSpanScorer(ginside,goutside,goutsideP,ginside.labelScore(0,w.length,""),scorer)
      val ginside2 = zero.buildInsideChart(w,scorer2);
      println(w + " " + ginside2.labelScore(0,w.length,gen.builder.root));
      lazy val goutside2 = zero.buildOutsideChart(ginside2,scorer2)._1;
      val tree = SimpleViterbiDecoder(ParserTestHarness.simpleGrammar).extractBestParse("",zero.grammar, ginside,goutside, null, scorer)
      val tree2 = SimpleViterbiDecoder(ParserTestHarness.simpleGrammar).extractBestParse("",zero.grammar, ginside2,goutside2, null, scorer)
      println(tree render w,t render w,tree2 render w);
      assert(tree2 == tree);
    } catch {
      case e: Exception =>
      throw new RuntimeException("Trouble with " + t.render(w),e);
    }

  }

}