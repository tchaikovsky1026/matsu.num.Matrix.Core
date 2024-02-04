/**
 * 2024.1.22
 */
package matsu.num.matrix.base.nlsf;

import java.util.function.Function;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.validation.ElementsTooManyException;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;
import matsu.num.matrix.base.validation.MatrixNotSymmetricException;
import matsu.num.matrix.base.validation.MatrixRejected;
import matsu.num.matrix.base.validation.MatrixStructureAcceptance;

/**
 * このパッケージで扱う, reject理由を表す列挙型.
 * 
 * @author Matsuura Y.
 * @version 19.1
 */
enum MatrixRejectionInLSF {

    /**
     * 正方行列でないために対応していないことを表す.
     */
    REJECTED_BY_NOT_SQUARE(m -> new MatrixFormatMismatchException(
            String.format("正方形ではない行列サイズ:%s", m.matrixDimension())), "REJECTED_BY_NOT_SQUARE"),

    /**
     * 対称行列でないために対応していないことを表す.
     */
    REJECTED_BY_NOT_SYMMETRIC(m -> new MatrixNotSymmetricException(m.toString()), "REJECTED_BY_NOT_SYMMETRIC"),

    /**
     * 有効要素数が大きすぎるために対応していないことを表す.
     */
    REJECTED_BY_TOO_MANY_ELEMENTS(m -> new ElementsTooManyException(m.toString()), "REJECTED_BY_TOO_MANY_ELEMENTS");

    private final MatrixStructureAcceptance reject;

    private MatrixRejectionInLSF(Function<Matrix, IllegalArgumentException> exceptionGetter, String explanation) {
        this.reject = MatrixRejected.by(exceptionGetter, explanation);
    }

    /**
     * この列挙型インスタンスの拒絶理由に適した例外インスタンスを取得する. <br>
     * ACCEPTEDの場合は空を返す.
     * 
     * @return rejectインスタンス
     */
    MatrixStructureAcceptance get() {
        return this.reject;
    }
}
