/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.6.26
 */
package matsu.num.matrix.core.common;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

/**
 * クラスの文字列生成の補助.
 * 
 * @author Matsuura Y.
 */
public final class ClassStringUtil {

    private ClassStringUtil() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * 与えられたインスタンスのクラスに関する, 成形された文字列表現を取得する.
     * 
     * <p>
     * 仕様:
     * </p>
     * 
     * @param object インスタンス
     * @return 文字列表現
     */
    public static String getClassString(Object object) {
        if (Objects.isNull(object)) {
            return "null";
        }

        Deque<Class<?>> enclosingClassLevels = new LinkedList<>();

        Class<?> currentLevel = object.getClass();
        while (Objects.nonNull(currentLevel)) {
            enclosingClassLevels.add(currentLevel);
            currentLevel = currentLevel.getEnclosingClass();
        }

        StringBuilder sb = new StringBuilder();
        for (Iterator<Class<?>> ite = enclosingClassLevels.descendingIterator();
                ite.hasNext();) {
            Class<?> clazz = ite.next();

            String name = clazz.getSimpleName();
            if (clazz.isAnonymousClass()) {
                name = "Anonymous";
            }
            if (name.isBlank()) {
                name = "Unknown";
            }

            sb.append(name);
            if (ite.hasNext()) {
                sb.append('.');
            }
        }

        return sb.toString();
    }
}
