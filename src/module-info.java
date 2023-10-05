/**
 * ベクトル, 行列, 線形代数に関する基本コンポーネントを扱うモジュール. 
 * 
 * <p>
 * <i>必須モジュール:</i> <br>
 * {@code matsu.num.Commons}
 * </p>
 * 
 * @author Matsuura Y.
 * @version 16.0
 */
module matsu.num.matrix.Base {
    exports matsu.num.matrix.base;
    exports matsu.num.matrix.base.nlsf;
    exports matsu.num.matrix.base.exception;

    requires transitive matsu.num.Commons;
}