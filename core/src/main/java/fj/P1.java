package fj;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import fj.data.Array;
import fj.data.List;
import fj.data.Stream;
import fj.data.Either;
import fj.data.Option;
import fj.data.Validation;
//import fj.data.*;


public abstract class P1<A> implements F0<A> {

    @Override
    public final A f() {
        return _1();
    }

    /**
     * Access the first element of the product.
     *
     * @return The first element of the product.
     */
    public abstract A _1();

    /**
     * Returns a function that returns the first element of a product.
     *
     * @return A function that returns the first element of a product.
     */
    public static <A> F<P1<A>, A> __1() {
        return p -> p._1();
    }

    /**
     * Promote any function to a transformation between P1s.
     *
	 * @deprecated As of release 4.5, use {@link #map_}
     * @param f A function to promote to a transformation between P1s.
     * @return A function promoted to operate on P1s.
     */
    public static <A, B> F<P1<A>, P1<B>> fmap(final F<A, B> f) {
        return map_(f);
    }

	/**
	 * Promote any function to a transformation between P1s.
	 *
	 * @param f A function to promote to a transformation between P1s.
	 * @return A function promoted to operate on P1s.
	 */
	public static <A, B> F<P1<A>, P1<B>> map_(final F<A, B> f) {
		return a -> a.map(f);
	}

	/**
     * Binds the given function to the value in a product-1 with a final join.
     *
     * @param f A function to apply to the value in a product-1.
     * @return The result of applying the given function to the value of given product-1.
     */
    public <B> P1<B> bind(final F<A, P1<B>> f) {
        P1<A> self = this;
        return P.lazy(() -> f.f(self._1())._1());
    }

    /**
     * Promotes the given function so that it returns its value in a P1.
     *
     * @param f A function to have its result wrapped in a P1.
     * @return A function whose result is wrapped in a P1.
     */
    public static <A, B> F<A, P1<B>> curry(final F<A, B> f) {
        return a -> P.lazy(() -> f.f(a));
    }

    /**
     * Performs function application within a P1 (applicative functor pattern).
     *
     * @param cf The P1 function to apply.
     * @return A new P1 after applying the given P1 function to the first argument.
     */
    public <B> P1<B> apply(final P1<F<A, B>> cf) {
        P1<A> self = this;
        return cf.bind(f -> fmap(f).f(self));
    }

    /**
     * Binds the given function to the values in the given P1s with a final join.
     *
     * @param cb A given P1 to bind the given function with.
     * @param f  The function to apply to the values in the given P1s.
     * @return A new P1 after performing the map, then final join.
     */
    public <B, C> P1<C> bind(final P1<B> cb, final F<A, F<B, C>> f) {
        return cb.apply(fmap(f).f(this));
    }

	/**
	 * Binds the given function to the values in the given P1s with a final join.
	 */
	public <B, C> P1<C> bind(final P1<B> cb, final F2<A, B, C> f) {
		return bind(cb, F2W.lift(f).curry());
	}

    /**
     * Joins a P1 of a P1 with a bind operation.
     *
     * @param a The P1 of a P1 to join.
     * @return A new P1 that is the join of the given P1.
     */
    public static <A> P1<A> join(final P1<P1<A>> a) {
        return a.bind(Function.<P1<A>>identity());
    }

    /**
     * Promotes a function of arity-2 to a function on P1s.
     *
     * @param f The function to promote.
     * @return A function of arity-2 promoted to map over P1s.
     */
    public static <A, B, C> F<P1<A>, F<P1<B>, P1<C>>> liftM2(final F<A, F<B, C>> f) {
        return Function.curry((pa, pb) -> pa.bind(pb, f));
    }

	public <B, C> P1<C> liftM2(P1<B> pb, F2<A, B, C> f) {
		return P.lazy(() -> f.f(_1(), pb._1()));
	}

    /**
     * Turns a List of P1s into a single P1 of a List.
     *
     * @param as The list of P1s to transform.
     * @return A single P1 for the given List.
     */
    public static <A> P1<List<A>> sequence(final List<P1<A>> as) {
        return as.foldRight(liftM2(List.<A>cons()), P.p(List.<A>nil()));
    }

    /**
     * A first-class version of the sequence method for lists of P1s.
     *
     * @return A function from a List of P1s to a single P1 of a List.
     */
    public static <A> F<List<P1<A>>, P1<List<A>>> sequenceList() {
        return as -> sequence(as);
    }

    /**
     * Turns a stream of P1s into a single P1 of a stream.
     *
     * @param as The stream of P1s to transform.
     * @return A single P1 for the given stream.
     */
    public static <A> P1<Stream<A>> sequence(final Stream<P1<A>> as) {
        return as.foldRight(liftM2(Stream.<A>cons()), P.p(Stream.<A>nil()));
    }

	/**
	 * Turns an optional P1 into a lazy option.
	 */
	public static <A> P1<Option<A>> sequence(final Option<P1<A>> o) {
		return P.lazy(() -> o.map(p -> p._1()));
	}

    /**
     * Turns an array of P1s into a single P1 of an array.
     *
     * @param as The array of P1s to transform.
     * @return A single P1 for the given array.
     */
    public static <A> P1<Array<A>> sequence(final Array<P1<A>> as) {
        return P.lazy(() -> as.map(P1.<A>__1()));
    }

    /**
     * Traversable instance of P1 for List
     *
     * @param f The function that takes A and produces a List<B> (non-deterministic result)
     * @return A List of P1<B>
     */
    public <B> List<P1<B>> traverseList(final F<A, List<B>>  f){
        return f.f(_1()).map(b -> P.p(b));
    }

    /**
     * Traversable instance of P1 for Either
     *
     * @param f The function produces Either
     * @return An Either of  P1<B>
     */
    public <B, X> Either<X, P1<B>> traverseEither(final F<A, Either<X, B>>  f){
        return f.f(_1()).right().map(b -> P.p(b));
    }

    /**
     * Traversable instance of P1 for Option
     *
     * @param f The function that produces Option
     * @return An Option of  P1<B>
     */
    public <B> Option<P1<B>> traverseOption(final F<A, Option<B>>  f){
        return f.f(_1()).map(b -> P.p(b));
    }

    /**
     * Traversable instance of P1 for Validation
     *
     * @param f The function might produces Validation
     * @return An Validation  of P1<B>
     */
    public <B, E> Validation<E, P1<B>> traverseValidation(final F<A, Validation<E, B>> f){
        return f.f(_1()).map(b -> P.p(b));
    }

    /**
     * Traversable instance of P1 for Stream
     *
     * @param f The function that produces Stream
     * @return An Stream of  P1<B>
     */
    public <B> Stream<P1<B>> traverseStream(final F<A, Stream<B>>  f){
        return f.f(_1()).map(b -> P.p(b));
    }

    /**
       * Map the element of the product.
       *
       * @param f The function to map with.
       * @return A product with the given function applied.
       */
      public <B> P1<B> map(final F<A, B> f) {
          final P1<A> self = this;
        return P.lazy(() -> f.f(self._1()));
      }

    public P1<A> memo() {
        return weakMemo();
    }

    /**
     * Returns a P1 that remembers its value.
     *
     * @return A P1 that calls this P1 once and remembers the value for subsequent calls.
     */
    public P1<A> hardMemo() { return new Memo<>(this); }

    /**
     * Like <code>memo</code>, but the memoized value is wrapped into a <code>WeakReference</code>
     */
    public P1<A> weakMemo() { return new WeakReferenceMemo<>(this); }

    /**
     * Like <code>memo</code>, but the memoized value is wrapped into a <code>SoftReference</code>
     */
    public P1<A> softMemo() { return new SoftReferenceMemo<>(this); }

    public static <A> P1<A> memo(F<Unit, A> f) {
        return P.lazy(f).memo();
    }

	public static <A> P1<A> memo(F0<A> f) {
		return P.lazy(f).memo();
	}

    static class Memo<A> extends P1<A> {
      private final P1<A> self;
      private volatile boolean initialized;
      private A value;

      Memo(P1<A> self) { this.self = self; }

      @Override public A _1() {
        if (!initialized) {
          synchronized (this) {
            if (!initialized) {
              A a = self._1();
              value = a;
              initialized = true;
              return a;
            }
          }
        }
        return value;
      }

      @Override public P1<A> memo() { return this; }
    }

    abstract static class ReferenceMemo<A> extends P1<A> {
      private final P1<A> self;
      private final Object latch = new Object();
      private volatile Reference<Option<A>> v = null;

      ReferenceMemo(final P1<A> self) { this.self = self; }

      @Override public A _1() {
        Option<A> o = v != null ? v.get() : null;
        if (o == null) {
          synchronized (latch) {
            o = v != null ? v.get() : null;
            if (o == null) {
              o = Option.some(self._1());
              v = newReference(o);
            }
          }
        }
        return o.some();
      }

      abstract Reference<Option<A>> newReference(Option<A> o);
    }

    static class WeakReferenceMemo<A> extends ReferenceMemo<A> {
      WeakReferenceMemo(P1<A> self) { super(self); }
      @Override Reference<Option<A>> newReference(final Option<A> o) { return new WeakReference<>(o); }
      @Override public P1<A> weakMemo() { return this; }
    }

    static class SoftReferenceMemo<A> extends ReferenceMemo<A> {
      SoftReferenceMemo(P1<A> self) { super(self); }
      @Override Reference<Option<A>> newReference(final Option<A> o) { return new SoftReference<>(o); }
      @Override public P1<A> softMemo() { return this; }
    }

    /**
     * Returns a constant function that always uses this value.
     *
     * @return A constant function that always uses this value.
     */
    public <B> F<B, A> constant() { return Function.constant(_1()); }

    @Override
    public String toString() {
		return Show.p1Show(Show.<A>anyShow()).showS(this);
	}

    @Override
    public boolean equals(Object other) {
        return Equal.equals0(P1.class, this, other, () -> Equal.p1Equal(Equal.<A>anyEqual()));
    }

    @Override
    public int hashCode() {
        return Hash.p1Hash(Hash.<A>anyHash()).hash(this);
    }

}