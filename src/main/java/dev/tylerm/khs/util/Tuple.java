package dev.tylerm.khs.util;

public class Tuple<L, C, R> {

    private final L left;
    private final C center;
    private final R right;

    public Tuple(L left, C center, R right) {
        this.left = left;
        this.center = center;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public C getCenter() {
        return center;
    }

    public R getRight() {
        return right;
    }

}
