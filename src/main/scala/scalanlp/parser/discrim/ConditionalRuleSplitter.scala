package scalanlp.parser
package discrim


import scalala.tensor.Counter
import collection.immutable.{IndexedSeq, Map}

/**
 * Provides split rules for fracToSplit of rules, and leaves the others not split.
 * Returns a map of all the new symbols, grouped by their coarse symbol, and the new rules
 * @author dlwh
 */
class ConditionalRuleSplitter(fracToSplit: Double = 0.5, minRuleValue: Double = 1E-4) {
  def chooseRulesToSplit[L,C](rules: Counter[Rule[L],Double], split: L=>Seq[L], proj: L=>C): (Map[C, Set[L]], IndexedSeq[Rule[L]]) = {
    def splitRule(r: Rule[L]) = r match {
      case BinaryRule(a,b,c) => for(a_ <- split(a); b_ <- split(b); c_ <- split(c)) yield BinaryRule(a_,b_,c_)
      case UnaryRule(a,b) => for(a_ <- split(a); b_ <- split(b)) yield UnaryRule(a_,b_)
    }
    val ruleSeq = rules.keysIterator.toIndexedSeq.sortBy((r: Rule[L]) => -rules(r).abs)
    // partition between rules that we split and those we do not.
    val (toSplit,toKeep) = ruleSeq.splitAt( (fracToSplit * ruleSeq.size).toInt)
    val allRules = toSplit.flatMap(r => if(rules(r).abs > minRuleValue) splitRule(r) else Seq(r)) ++ toKeep

    val syms = allRules.iterator.flatMap(_.symbols).toSet
    val coarsened = syms.groupBy(proj)
    // unaries that go between scales
    val shiftUnaries = for( UnaryRule(a,b) <- rules.keysIterator; aa <- coarsened(proj(a)); bb <- coarsened(proj(b))) yield UnaryRule(aa,bb)

    (coarsened,allRules ++ shiftUnaries.toSet)
  }

}