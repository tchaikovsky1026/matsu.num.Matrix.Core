/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/**
 * ブロック行列を扱うパッケージ.
 * 
 * <p>
 * ブロック行列を生成するもっとも原始的な方法は,
 * ブロック構造体を経由する方法である. <br>
 * ブロック構造体は {@link matsu.num.matrix.core.block.BlockMatrixStructure}
 * により表現され, そのインスタンス生成にはビルダ
 * ({@link matsu.num.matrix.core.block.BlockMatrixStructure.Builder})
 * が用いられる. <br>
 * ブロック構造体をもとに,
 * {@link matsu.num.matrix.core.block.BlockMatrix#of(BlockMatrixStructure)},
 * {@link matsu.num.matrix.core.block.BlockMatrixEntryReadable#of(BlockMatrixStructure)}
 * によってブロック行列を生成する. <br>
 * {@link matsu.num.matrix.core.block.BlockMatrixEntryReadable}
 * は成分にアクセス可能なブロック行列を表現するため,
 * {@link matsu.num.matrix.core.block.BlockMatrixEntryReadable#of(BlockMatrixStructure)}
 * メソッドに渡すブロック構造体は {@link matsu.num.matrix.core.EntryReadableMatrix}
 * を扱うものでなければならない (型パラメータにより制限している).
 * </p>
 * 
 * <p>
 * {@link matsu.num.matrix.core.block.BlockMatrixSupport}
 * は, ブロック構造体を経由せずにブロック行列を生成する仕組みを提供している.
 * </p>
 * 
 * <p>
 * ブロック構造体 &middot; ブロック行列の全体の行列次元 (サイズ) が
 * {@link matsu.num.matrix.core.MatrixDimension} の扱える範囲を超えるようなインスタンスは生成できない.
 * <br>
 * そのような場合, {@link matsu.num.matrix.core.validation.ElementsTooManyException}
 * がスローされる.
 * </p>
 */
package matsu.num.matrix.core.block;
