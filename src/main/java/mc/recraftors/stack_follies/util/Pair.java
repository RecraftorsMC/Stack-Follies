package mc.recraftors.stack_follies.util;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class Pair <E, F> {
    private final E e;
    private final F f;

    private Pair(E e, F f) {
        this.e = e;
        this.f = f;
    }

    public E getFirst() {
        return e;
    }

    public F getSecond() {
        return f;
    }

    public boolean hasFirst() {
        return e != null;
    }

    public boolean hasSecond() {
        return f != null;
    }

    public boolean empty() {
        return e == null && f == null;
    }

    public void compute(BiConsumer<E, F> consumer) {
        consumer.accept(e, f);
    }

    public <M, N> Pair<M, N> map(Function<E, M> f1, Function<F, N> f2) {
        return Pair.ofNullable(f1.apply(e), f2.apply(f));
    }

    public static <E, F> Pair<E, F> of(E e, F f) {
        return new Pair<>(Objects.requireNonNull(e), Objects.requireNonNull(f));
    }

    public static <E, F> Pair<E, F> ofNullable(E e, F f) {
        return new Pair<>(e, f);
    }

    public static <E, F> Pair<E, F> ofNull() {
        return new Pair<>(null, null);
    }
}
