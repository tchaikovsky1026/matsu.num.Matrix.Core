/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.2
 */
package matsu.num.matrix.base.validation;

/**
 * 行列が構造的に処理に対応しているかどうかを表すインターフェース.
 * 
 * <p>
 * 行列が対応していることは, シングルトンインスタンス {@link #ACCEPTED}
 * により表される. <br>
 * 対応していないことを表すインスタンスは, {@link MatrixRejected} により構築することができる.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 23.3
 */
public abstract sealed class MatrixStructureAcceptance permits MatrixAccepted, MatrixRejected {

    /**
     * 行列が処理に対応していることを表すシングルトン.
     */
    public static final MatrixStructureAcceptance ACCEPTED = new MatrixAccepted();

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
    abstract Type type();

    /**
     * このインスタンスがACCEPTを表現するかを判定する.
     * 
     * @return ACCEPTを扱うなら true
     */
    public final boolean isAccept() {
        return this.type() == Type.ACCEPTED;
    }

    /**
     * このインスタンスがREJECTを表現するかを判定する.
     * 
     * @return REJECTを扱うなら true
     */
    public final boolean isReject() {
        return this.type() == Type.REJECTED;
    }

    /**
     * このインスタンスの拒絶理由に適した例外インスタンスを取得する. <br>
     * {@link #type()} が {@link Type#ACCEPTED} の場合は空を返す.
     * 
     * @param cause 関連付けられる行列
     * @return スローすべき例外, ACCEPTEDの場合は空
     * @throws IllegalStateException このインスタンスがACCEPTの場合
     */
    public abstract IllegalArgumentException getException(Object cause);

    /**
     * -
     * 
     * @return -
     * @throws CloneNotSupportedException 常に
     * @deprecated Clone不可
     */
    @Deprecated
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * オーバーライド不可.
     */
    @Override
    @Deprecated
    protected final void finalize() throws Throwable {
        super.finalize();
    }

    /**
     * 対応しているかどうかを示す列挙型.
     */
    enum Type {

        /**
         * 対応していることを表す. <br>
         * このタイプを有するのはシングルトン
         * {@link MatrixStructureAcceptance#ACCEPTED}
         * のみである.
         */
        ACCEPTED,

        /**
         * 対応していないことを表す.
         */
        REJECTED;
    }
}
