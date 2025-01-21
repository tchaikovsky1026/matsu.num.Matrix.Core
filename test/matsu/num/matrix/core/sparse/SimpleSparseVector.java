/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core.sparse;

import java.util.Objects;

import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;

/**
 * {@link SparseVector} の最もシンプルな実装. <br>
 * {@link Vector} をラップする.
 * 
 * @author Matsuura Y.
 */
final class SimpleSparseVector implements SparseVectorSealed {

    private final Vector vec;

    /**
     * {@link Vector} をラップしてインスタンスを構築する.
     * 
     * @param src ラップされるベクトル
     * @throws NullPointerException 引数がnull
     */
    SimpleSparseVector(Vector src) {
        this.vec = Objects.requireNonNull(src);
    }

    @Override
    public VectorDimension vectorDimension() {
        return this.vec.vectorDimension();
    }

    @Override
    public double valueAt(int index) {
        return this.vec.valueAt(index);
    }

    @Override
    public Vector asVector() {
        return this.vec;
    }

    @Override
    public double norm2() {
        return this.vec.norm2();
    }

    @Override
    public double normMax() {
        return this.vec.normMax();
    }

    @Override
    public double dot(Vector reference) {
        return this.vec.dot(reference);
    }

    @Override
    public SparseVector times(double scalar) {
        return new SimpleSparseVector(this.vec.times(scalar));
    }

    @Override
    public Vector plus(Vector reference) {
        return this.vec.plus(reference);
    }

    @Override
    public SparseVector normalizedEuclidean() {
        return new SimpleSparseVector(this.vec.normalizedEuclidean());
    }

    @Override
    public SparseVector negated() {
        return new SimpleSparseVector(this.vec.negated());
    }
}
