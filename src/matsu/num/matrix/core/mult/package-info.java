/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/**
 * 行列積の実行により得られるコンポーネントを扱うパッケージ.
 * 
 * <p>
 * 単純な行列積の場合は,
 * {@link matsu.num.matrix.core.Matrix#multiply(matsu.num.matrix.core.Matrix, matsu.num.matrix.core.Matrix...)
 * Matrix.multiply(Matrix, Matrix...)}
 * や
 * {@link matsu.num.matrix.core.OrthogonalMatrix#multiply(matsu.num.matrix.core.OrthogonalMatrix, matsu.num.matrix.core.OrthogonalMatrix...)
 * OrthogonalMatrix.multiply(OrthogonalMatrix, OrthogonalMatrix...)}
 * が適切である. <br>
 * このパッケージでは, より複雑 &middot; 高機能な行列積とその結果を提供する.
 * </p>
 * 
 * <p>
 * 機能としては,
 * {@link matsu.num.matrix.core.mult.SequentialMultiplyingContainer
 * SequentialMultiplyingContainer}
 * と
 * {@link matsu.num.matrix.core.mult.OrthSequentialMultiplyingContainer
 * OrthSequentialMultiplyingContainer}
 * を参照すること.
 * </p>
 */
package matsu.num.matrix.core.mult;
