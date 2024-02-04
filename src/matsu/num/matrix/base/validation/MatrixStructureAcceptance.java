/**
 * 2024.1.22
 */
package matsu.num.matrix.base.validation;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.Matrix;

/**
 * <p>
 * 行列が構造的に処理に対応しているかどうかを表すインターフェース.
 * </p>
 * 
 * <p>
 * 行列が対応していることは, シングルトンインスタンス {@linkplain #ACCEPTED}
 * により表される. <br>
 * 対応していないことを表すインスタンスは, {@linkplain MatrixRejected} により構築することができる.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 19.1
 */
public abstract class MatrixStructureAcceptance {

    /**
     * 行列が処理に対応していることを表すシングルトン.
     */
    public static final MatrixStructureAcceptance ACCEPTED = new MatrixStructureAcceptance() {

        @Override
        public final Type type() {
            return Type.ACCEPTED;
        }

        @Override
        public final Optional<IllegalArgumentException> getException(Matrix matrix) {
            Objects.requireNonNull(matrix);
            return Optional.empty();
        }

        @Override
        public final String toString() {
            return this.type().toString();
        }
    };

    /**
     * パッケージプライベートのアクセス制限を持つコンストラクタ.
     */
    MatrixStructureAcceptance() {
        super();
    }

    /**
     * <p>
     * このインスタンスの属性を返す.
     * </p>
     * 
     * @return 属性
     */
    public abstract Type type();

    /**
     * <p>
     * このインスタンスの拒絶理由に適した例外インスタンスを取得する. <br>
     * {@linkplain #type()} が {@linkplain Type#ACCEPTED} の場合は空を返す.
     * </p>
     * 
     * @param matrix 関連付けられる行列
     * @return スローすべき例外, ACCEPTEDの場合は空
     * @throws NullPointerException 引数がnullの場合
     */
    public abstract Optional<IllegalArgumentException> getException(Matrix matrix);

    /**
     * 対応しているかどうかを示す列挙型.
     */
    public enum Type {

        /**
         * 対応していることを表す. <br>
         * このタイプを有するのはシングルトン
         * {@linkplain MatrixStructureAcceptance#ACCEPTED}
         * のみである.
         */
        ACCEPTED,

        /**
         * 対応していないことを表す.
         */
        REJECTED;
    }
}
