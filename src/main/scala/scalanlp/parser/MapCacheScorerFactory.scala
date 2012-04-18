package scalanlp.parser

import java.util.concurrent.ConcurrentHashMap


/**
 * TODO: make the cache kick things out.
 * @author dlwh
 */
@SerialVersionUID(1L)
class MapCacheScorerFactory[L, W](trueFactory: DerivationScorer.Factory[L, W]) extends DerivationScorer.Factory[L, W] with Serializable {
  private val cache = new ConcurrentHashMap[Seq[W], DerivationScorer[L, W]]

  def grammar = trueFactory.grammar
  def lexicon = trueFactory.lexicon

  def specialize(words: Seq[W]) = {
    val r = cache.get(words)
    if (r != null) r
    else {
      val scorer = trueFactory.specialize(words)
      cache.putIfAbsent(words, scorer)
      scorer
    }
  }
}
