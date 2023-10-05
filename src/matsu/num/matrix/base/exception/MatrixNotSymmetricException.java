/**
 * 2023.8.16
 */
package matsu.num.matrix.base.exception;

/**
 * 行列オブジェクトが対称行列でないことを報告する例外.
 *
 * @author Matsuura Y.
 * @version 15.0
 */
public final class MatrixNotSymmetricException extends IllegalArgumentException {

    private static final long serialVersionUID = 435736393339255423L;

    /**
     * メッセージ無しの例外を生成する.
     */
    public MatrixNotSymmetricException() {
        super();
    }

    /**
     * メッセージ有りの例外を生成する.
     * @param string メッセージ
     */
    public MatrixNotSymmetricException(String string) {
        super(string);
    }
}
