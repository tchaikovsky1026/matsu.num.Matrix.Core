/**
 * 2023.12.25
 */
package matsu.num.matrix.base;

/**
 * <p>
 * 成分にアクセス可能な単位下三角行列を表す. <br>
 * 単位下三角行列とは, 対角成分が1の下三角行列である.
 * </p>
 * 
 * <p>
 * 行列式は1である. <br>
 * したがって, 逆行列は必ず存在する. <br>
 * 単位下三角行列の逆行列は, 行列ベクトル積が容易に計算できるので,
 * このインターフェースは {@linkplain Inversion} を継承する.
 * </p>
 *
 * <p>
 * {@link Matrix}のクラス説明の規約に従う.
 * </p>
 *
 * @author Matsuura Y.
 * @version 18.0
 * @see Matrix
 */
public interface LowerUnitriangularEntryReadableMatrix
        extends EntryReadableMatrix, Inversion, Determinantable {

    @Override
    public abstract LowerUnitriangularEntryReadableMatrix target();

}
