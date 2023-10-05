/*
 * 2023.8.20
 */
package matsu.num.matrix.base;

import java.util.Optional;

import matsu.num.matrix.base.exception.MatrixFormatMismatchException;
import matsu.num.matrix.base.helper.matrix.multiply.OrthogonalMatrixMultiplication;

/**
 * 直交行列であることを表す.
 * 
 * <p>
 * {@link Matrix}のクラス説明の規約に従う.  
 * </p>
 * 
 * @author Matsuura Y.
 * @version 15.1
 * @see Matrix
 */
public interface OrthogonalMatrix extends Matrix, Inversion {

    @Override
    public OrthogonalMatrix target();

    /**
     * この直交行列の逆行列を取得する. <br>
     * 正則行列であり, 空でない.
     * 
     * @return この行列の逆行列
     */
    @Override
    public Optional<? extends OrthogonalMatrix> inverse();

    /**
     * この行列の転置行列を返す. 
     * 
     * @return 転置行列
     */
    @Override
    public abstract OrthogonalMatrix transpose();

    /**
     * 1個以上の直交行列に対し, それらの行列積を返す.
     * 
     * @param first 行列積の左端の行列
     * @param following firstに続く行列, 左から順番
     * @return 直交行列の行列積
     * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static OrthogonalMatrix multiply(OrthogonalMatrix first, OrthogonalMatrix... following) {
        return OrthogonalMatrixMultiplication.instance().apply(first, following);
    }

}
