/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.5.9
 */
package matsu.num.matrix.core.helper.matrix.householder;

import matsu.num.matrix.core.HouseholderMatrix;
import matsu.num.matrix.core.SkeletalSymmetricOrthogonalMatrix;

/**
 * {@link HouseholderMatrix} の骨格実装を扱う.
 * 
 * <p>
 * この骨格実装は, {@link #toString()} の実装を提供する.
 * </p>
 * 
 * @author Matsuura Y.
 */
abstract class SkeletalHouseholderMatrix<T extends SkeletalHouseholderMatrix<T>>
        extends SkeletalSymmetricOrthogonalMatrix<T> implements HouseholderMatrix {

    /**
     * 唯一のコンストラクタ.
     */
    SkeletalHouseholderMatrix() {
        super();
    }

    /**
     * このオブジェクトの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code Matrix[dim:(%dimension), householder]}
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[dim: %s, householder]",
                this.matrixDimension());
    }
}
