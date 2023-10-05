/**
 * 2023.8.21
 */
package matsu.num.matrix.base;

import matsu.num.matrix.base.exception.MatrixFormatMismatchException;
import matsu.num.matrix.base.helper.matrix.UnitMatrixImpl;

/**
 * 単位行列を扱う. 
 *
 * @author Matsuura Y.
 * @version 15.1
 */
public interface UnitMatrix extends PermutationMatrix, BandMatrix, Symmetric {

    @Override
    public abstract UnitMatrix target();

    /**
     * 与えられた次元(サイズ)の単位行列を生成する.
     *
     * @param matrixDimension 行列サイズ
     * @return 単位行列
     * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static UnitMatrix matrixOf(final MatrixDimension matrixDimension) {
        return new UnitMatrixImpl(matrixDimension);
    }
}
