/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.3
 */
package matsu.num.matrix.core.helper.value;

import java.util.function.Function;

import matsu.num.matrix.core.validation.ElementsTooManyException;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;
import matsu.num.matrix.core.validation.MatrixNotSymmetricException;
import matsu.num.matrix.core.validation.MatrixRejected;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * Reject理由を表す列挙型.
 * 
 * <p>
 * {@link MatrixStructureAcceptance} の {@link MatrixRejected}
 * に関する準備された定数を扱う.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 23.4
 */
public enum MatrixRejectionConstant {

    /**
     * 正方行列でないために対応していないことを表す.
     */
    REJECTED_BY_NOT_SQUARE(
            o -> new MatrixFormatMismatchException(String.format("正方形ではない行列サイズ:%s", o)),
            "REJECTED_BY_NOT_SQUARE"),

    /**
     * 対称行列でないために対応していないことを表す.
     */
    REJECTED_BY_NOT_SYMMETRIC(
            o -> new MatrixNotSymmetricException(o.toString()),
            "REJECTED_BY_NOT_SYMMETRIC"),

    /**
     * 有効要素数が大きすぎるために対応していないことを表す.
     */
    REJECTED_BY_TOO_MANY_ELEMENTS(
            o -> new ElementsTooManyException(o.toString()),
            "REJECTED_BY_TOO_MANY_ELEMENTS"),

    /**
     * 下三角構造でないことを表す.
     */
    REJECTED_BY_NOT_LOWER_TRIANGULAR(
            o -> new MatrixFormatMismatchException(String.format("下三角構造でない:%s", o)),
            "REJECTED_BY_NOT_LOWER_TRIANGULAR");

    private final MatrixStructureAcceptance reject;

    private MatrixRejectionConstant(Function<Object, IllegalArgumentException> exceptionGetter, String explanation) {
        this.reject = MatrixRejected.by(exceptionGetter, explanation);
    }

    /**
     * この列挙型インスタンスの拒絶理由に適した例外インスタンスを取得する. <br>
     * ACCEPTEDの場合は空を返す.
     * 
     * @return rejectインスタンス
     */
    public MatrixStructureAcceptance get() {
        return this.reject;
    }
}
