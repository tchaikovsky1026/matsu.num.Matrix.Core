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
 * 成分アクセス可能行列の,
 * transpose に関わる骨格実装.
 * 
 * <p>
 * このクラスはを型として扱ってはいけない.
 * </p>
 * 
 * @implSpec
 *               型をバインドしたクラスは {@code final} とするのが望ましい.
 * 
 * @author Matsuura Y.
 * @param <T>
 *            生成する転置行列の型を表す. <br>
 *            {@link #transpose()} の戻り値型にバインドされる型を決める.
 * @param <ETT>
 *            {@link #transposeReadable()} の戻り値型にバインドされる型を決める. <br>
 *            インターフェースの実装規約に従うこと.
 */
public abstract class SkeletalEntryReadableMatrix<T extends ETT, ETT extends EntryReadableMatrix>
        extends SkeletalMatrix<T>
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
