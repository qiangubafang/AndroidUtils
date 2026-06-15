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
package org.tcshare.poi.converter;

import org.tcshare.poi.data.PictureRenderData;
import org.tcshare.poi.data.Pictures;

/**
 * Convert Object to PictureRenderData
 * 
 * @author sayi
 *
 */
public class ObjectToPictureRenderDataConverter implements ToRenderDataConverter<Object, PictureRenderData> {

    @Override
    public PictureRenderData convert(Object source) throws Exception {
        if (null == source || source instanceof PictureRenderData) return (PictureRenderData) source;
        return Pictures.of(source.toString()).fitSize().create();
    }

}
