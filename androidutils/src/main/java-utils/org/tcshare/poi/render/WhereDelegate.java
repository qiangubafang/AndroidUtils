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
package org.tcshare.poi.render;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import org.tcshare.poi.converter.ObjectToTextRenderDataConverter;
import org.tcshare.poi.converter.ToRenderDataConverter;
import org.tcshare.poi.data.NumberingRenderData;
import org.tcshare.poi.data.PictureRenderData;
import org.tcshare.poi.data.TableRenderData;
import org.tcshare.poi.data.TextRenderData;
import org.tcshare.poi.policy.NumberingRenderPolicy;
import org.tcshare.poi.policy.PictureRenderPolicy;
import org.tcshare.poi.policy.TableRenderPolicy;
import org.tcshare.poi.policy.TextRenderPolicy;

/**
 * The delegation of the current location provides more methods of operating the
 * current location.
 * 
 * @author Sayi
 * @since 1.5.1
 */
public class WhereDelegate {

    private static final ToRenderDataConverter<Object, TextRenderData> converter = new ObjectToTextRenderDataConverter();

    private final XWPFRun run;

    public WhereDelegate(XWPFRun run) {
        this.run = run;
    }

    public XWPFRun getRun() {
        return this.run;
    }

    public void renderText(Object data) throws Exception {
        TextRenderData renderData = converter.convert(data);
        TextRenderPolicy.Helper.renderTextRun(run, renderData);
    }

    public void renderNumbering(NumberingRenderData data) throws Exception {
        NumberingRenderPolicy.Helper.renderNumbering(run, data);
    }

    public void renderPicture(PictureRenderData data) throws Exception {
        PictureRenderPolicy.Helper.renderPicture(run, data);
    }

    public void renderTable(TableRenderData data) throws Exception {
        TableRenderPolicy.Helper.renderTable(run, data);
    }

    public void addPicture(InputStream inputStream, int type, int width, int height)
            throws InvalidFormatException, IOException {
        run.addPicture(inputStream, type, "Generated", Units.pixelToEMU(width), Units.pixelToEMU(height));
    }

}
