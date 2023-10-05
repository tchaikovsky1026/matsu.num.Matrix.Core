/**
 * 2023.8.18
 */
package matsu.num.matrix.base;

import java.util.Optional;

/**
 * 成分にアクセス可能な単位下三角行列を表す. <br>
 * 単位下三角行列とは, 対角成分が1の下三角行列である. 
 *
 * <p>
 * {@link Matrix}のクラス説明の規約に従う.  
 * </p>
 *
 * @author Matsuura Y.
 * @version 15.0
 * @see Matrix
 */
public interface LowerUnitriangularEntryReadableMatrix
        extends EntryReadableMatrix, Inversion, Determinantable {

    /**
     * {@inheritDoc } 
     * 
     * <p>
     * 単位下三角行列の行列式は1である. 
     * </p>
     * 
     * @return {@inheritDoc} = 1
     */
    @Override
    public default double determinant() {
        return 1d;
    }

    /**
     * {@inheritDoc }
     * 
     * <p>
     * 単位下三角行列の行列式の絶対値の自然対数は0である. 
     * </p>
     * 
     * @return {@inheritDoc} = 0
     */
    @Override
    public default double logAbsDeterminant() {
        return 0d;
    }

    /**
     * {@inheritDoc }
     * 
     * <p>
     * 単位下三角行列の行列式の符号は1である. 
     * </p>
     * 
     * @return 行列式の符号 = 1
     */
    @Override
    public default int signOfDeterminant() {
        return 1;
    }

    /**
     * {@inheritDoc }
     * 
     * <p>
     * 単位下三角行列の逆行列は必ず存在する.
     * </p>
     * 
     */
    @Override
    public Optional<? extends Matrix> inverse();

}
