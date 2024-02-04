/**
 * 2023.12.22
 */
package matsu.num.matrix.base.validation;

/**
 * 行列の構造(サイズ等)が規約を満たしていないことを報告する例外. 
 *
 * @author Matsuura Y.
 * @version 17.3
 */
public final class MatrixFormatMismatchException extends IllegalArgumentException {

    private static final long serialVersionUID = -7691523485897771939L;

    /**
     * メッセージ無しの例外を生成する.
     */
    public MatrixFormatMismatchException() {
        super();
    }

    /**
     * メッセージ有りの例外を生成する.
     * @param string メッセージ
     */
    public MatrixFormatMismatchException(String string) {
        super(string);
    }
}
