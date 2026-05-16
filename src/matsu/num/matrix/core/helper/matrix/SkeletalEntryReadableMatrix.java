/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.16
 */
package matsu.num.matrix.core.helper.matrix;

import matsu.num.matrix.core.EntryReadableMatrix;

/**
 * 非対称な {@link EntryReadableMatrix} の骨格実装. <br>
 * {@link SkeletalMatrix} に準拠.
 * 
 * <p>
 * このクラスはを型として扱ってはいけない.
 * </p>
 * 
 * @author Matsuura Y.
 * @param <CTT>
 *            生成する転置行列の型を表す. <br>
 *            {@link #createTranspose()} の型を決める.
 * @param <ETT>
 *            {@link #transposeReadable()} の戻り値型を表す. <br>
 *            インターフェースの実装規約に従うこと.
 */
public abstract class SkeletalEntryReadableMatrix<CTT extends ETT, ETT extends EntryReadableMatrix>
        extends SkeletalMatrix<CTT>
        implements EntryReadableMatrix {

    /**
     * 唯一のコンストラクタ.
     */
    protected SkeletalEntryReadableMatrix() {
        super();
    }

    @Override
    public final ETT transposeReadable() {
        return transposeSupplier.get();
    }

    @Override
    public String toString() {
        return "Matrix[dim: %s, %s]"
                .formatted(
                        this.matrixDimension(),
                        EntryReadableMatrix.toSimplifiedEntryString(this));
    }
}
