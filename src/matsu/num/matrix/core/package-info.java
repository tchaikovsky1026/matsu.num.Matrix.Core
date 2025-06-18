/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/**
 * ベクトル, 行列, 線形代数に関する基本コンポーネントを扱うパッケージ.
 * 
 * <p>
 * ベクトルは {@link matsu.num.matrix.core.Vector} クラスにより,
 * 行列は {@link matsu.num.matrix.core.Matrix} インターフェースとそのサブタイプにより扱う. <br>
 * このモジュールにおいて, ベクトル &middot; 行列は全てイミュータブルなオブジェクトとして扱い,
 * ほとんどのインスタンスの生成はビルダを介して行う.
 * </p>
 * 
 * <p>
 * 行列は, 様々なタイプ (型) が用意されている. <br>
 * </p>
 * 
 * <p>
 * まず {@link matsu.num.matrix.core.Matrix} は行列ベクトル積を提供する. <br>
 * さらに行列成分にアクセスできるためには, {@link matsu.num.matrix.core.Matrix} のサブタイプである
 * {@link matsu.num.matrix.core.EntryReadableMatrix} が実装される必要がある. <br>
 * 帯行列である場合, {@link matsu.num.matrix.core.BandMatrix} を実装することで,
 * メモリが節約され, 帯行列に特化したソルバー機能が使えるようになる. <br>
 * 対称行列の場合は {@link matsu.num.matrix.core.Symmetric} を付与することで,
 * 対称行列に特化した仕組みを利用できるようになる.
 * </p>
 * 
 * <p>
 * 行列の基本的な実装クラスは,
 * {@link matsu.num.matrix.core.GeneralMatrix},
 * {@link matsu.num.matrix.core.GeneralBandMatrix},
 * {@link matsu.num.matrix.core.SymmetricMatrix},
 * {@link matsu.num.matrix.core.SymmetricBandMatrix}
 * であり,
 * {@link matsu.num.matrix.core.EntryReadableMatrix},
 * {@link matsu.num.matrix.core.BandMatrix}
 * の具象クラスである. <br>
 * それぞれのインスタンスはビルダより生成する.
 * </p>
 * 
 * <p>
 * その他, 次のようなタイプが用意されている. <br>
 * インスタンスの生成方法を提供しているタイプもあり,
 * クラス &middot; インターフェースの説明文に記載されている.
 * </p>
 * 
 * <ul>
 * <li>{@link matsu.num.matrix.core.DiagonalMatrix}: 対角行列</li>
 * <li>{@link matsu.num.matrix.core.LowerUnitriangular}: 単位下三角行列
 * <ul>
 * <li>{@link matsu.num.matrix.core.LowerUnitriangularMatrix}: 単位下三角密行列</li>
 * <li>{@link matsu.num.matrix.core.LowerUnitriangularBandMatrix}: 単位下三角帯行列</li>
 * </ul>
 * </li>
 * <li>{@link matsu.num.matrix.core.OrthogonalMatrix}: 直交行列
 * <ul>
 * <li>{@link matsu.num.matrix.core.PermutationMatrix}: 置換行列</li>
 * <li>{@link matsu.num.matrix.core.SignatureMatrix}: 符号行列</li>
 * <li>{@link matsu.num.matrix.core.UnitMatrix}: 単位行列</li>
 * <li>{@link matsu.num.matrix.core.HouseholderMatrix}: Householder 行列</li>
 * </ul>
 * </li>
 * <li>{@link matsu.num.matrix.core.ZeroMatrix}: 零行列
 * <ul>
 * <li>{@link matsu.num.matrix.core.SquareZeroMatrix}: 正方の零行列</li>
 * </ul>
 * </li>
 * </ul>
 * 
 */
package matsu.num.matrix.core;
