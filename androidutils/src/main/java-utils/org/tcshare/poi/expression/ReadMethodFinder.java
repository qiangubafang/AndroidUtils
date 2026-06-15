/*
 * Copyright 2014-2026 Sayi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tcshare.poi.expression;



import java.lang.reflect.Method;

import org.tcshare.poi.MyLogger;

class ReadMethodFinder {

    private static final MyLogger MY_LOGGER = MyLogger.getLogger(ReadMethodFinder.class);

    public static Method find(Class<?> objClass, String key) {
        if (objClass == null || key == null || key.isEmpty()) {
            return null;
        }

        // 首字母大写
        String capitalizedKey = key.substring(0, 1).toUpperCase() + key.substring(1);

        // 拼接 getter 方法名
        String getterName = "get" + capitalizedKey;

        try {
            // 获取 public 方法（包括继承）
            return objClass.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            // 尝试 boolean 类型 isXXX()
            if (key.startsWith("is") && key.length() > 2) {
                try {
                    return objClass.getMethod(key);
                } catch (NoSuchMethodException ex) {
                    // ignore
                }
            }

            // 尝试 isXXX() 风格
            String isGetterName = "is" + capitalizedKey;
            try {
                return objClass.getMethod(isGetterName);
            } catch (NoSuchMethodException ex) {
                // ignore
            }
        }

        MY_LOGGER.debug(String.format("Fail introspector the property: %s from %s", key, objClass.getName()));
        return null;
    }

}
