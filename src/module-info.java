/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/**
 * ベクトル, 行列, 線形代数に関する基本コンポーネントを扱うモジュール.
 * 
 * <p>
 * 基本的なベクトルと行列を表現する機能が
 * {@link matsu.num.matrix.core}
 * パッケージに用意されている. <br>
 * {@link matsu.num.matrix.core.block}
 * パッケージ,
 * {@link matsu.num.matrix.core.sparse}
 * パッケージは,
 * 疎な行列 &middot; ベクトルを扱うためのパッケージである.
 * </p>
 * 
 * <p>
 * 線形連立方程式を解くことに関する機能が,
 * {@link matsu.num.matrix.core.nlsf}
 * パッケージ,
 * {@link matsu.num.matrix.core.qr}
 * パッケージに含まれる. <br>
 * それらの機能は, 逆行列 &middot; 一般化逆行列を返すというAPIで提供される.
 * </p>
 * 
 * <p>
 * <i>依存モジュール:</i> <br>
 * (無し)
 * </p>
 * 
 * @author Matsuura Y.
 * @version 28.1.0
 */
module matsu.num.Matrix.Core {
    exports matsu.num.matrix.core;
    exports matsu.num.matrix.core.block;
    exports matsu.num.matrix.core.nlsf;
    exports matsu.num.matrix.core.qr;
    exports matsu.num.matrix.core.sparse;
    exports matsu.num.matrix.core.validation;
}
