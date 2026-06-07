package slimeknights.mantle.util;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

/**
 * Implementation of {@link Consumer} that weakly references a parent object.
 * Designed for use in capability listeners (removed in NeoForge 1.21.1).
 * 
 * @param <TE> Parent object type, typically a TE
 * @param <C>  Consumer value
 * @deprecated Capability system redesigned in NeoForge 1.21.1, LazyOptional and
 *             NonNullConsumer removed
 */
@Deprecated(forRemoval = true, since = "NeoForge 21.1")
public class WeakConsumerWrapper<TE, C> implements Consumer<C> {
  private final WeakReference<TE> te;
  private final NonnullBiConsumer<TE, C> consumer;

  /**
   * Creates a new weak consumer wrapper
   * 
   * @param te       Weak reference, typically to a TE
   * @param consumer Consumer using the TE and the consumed value. Should not use
   *                 a lambda reference to an object that may need to be garbage
   *                 collected
   */
  public WeakConsumerWrapper(TE te, NonnullBiConsumer<TE, C> consumer) {
    this.te = new WeakReference<>(te);
    this.consumer = consumer;
  }

  @Override
  public void accept(C c) {
    TE te = this.te.get();
    if (te != null) {
      consumer.accept(te, c);
    }
  }
}
