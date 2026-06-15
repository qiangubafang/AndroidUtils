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
package org.tcshare.poi.policy;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import org.tcshare.poi.converter.ObjectToNumberingRenderDataConverter;
import org.tcshare.poi.converter.ToRenderDataConverter;
import org.tcshare.poi.data.NumberingFormat;
import org.tcshare.poi.data.NumberingItemRenderData;
import org.tcshare.poi.data.NumberingRenderData;
import org.tcshare.poi.render.RenderContext;
import org.tcshare.poi.util.StyleUtils;
import org.tcshare.poi.xwpf.BodyContainer;
import org.tcshare.poi.xwpf.BodyContainerFactory;
import org.tcshare.poi.xwpf.NiceXWPFDocument;

/**
 * Numbering render
 * 
 * @author Sayi
 */
public class NumberingRenderPolicy extends AbstractRenderPolicy<NumberingRenderData> {

    private static ToRenderDataConverter<Object, NumberingRenderData> converter = new ObjectToNumberingRenderDataConverter();

    @Override
    public NumberingRenderData cast(Object source) throws Exception {
        return converter.convert(source);
    }

    @Override
    protected boolean validate(NumberingRenderData data) {
        if (null == data) return false;
        return CollectionUtils.isNotEmpty(((NumberingRenderData) data).getItems());
    }

    @Override
    public void doRender(RenderContext<NumberingRenderData> context) throws Exception {
        Helper.renderNumbering(context.getRun(), context.getData());
    }

    @Override
    protected void afterRender(RenderContext<NumberingRenderData> context) {
        clearPlaceholder(context, true);
    }

    public static class Helper {

        public static void renderNumbering(XWPFRun run, NumberingRenderData data) throws Exception {
            List<NumberingItemRenderData> items = data.getItems();
            NumberingFormat[] array = data.getFormats().toArray(new NumberingFormat[] {});
            BigInteger numID = ((NiceXWPFDocument) run.getParent().getDocument()).addNewMultiLevelNumberingId(array);
            BodyContainer bodyContainer = BodyContainerFactory.getBodyContainer(run);
            for (NumberingItemRenderData item : items) {
                XWPFParagraph paragraph = bodyContainer.insertNewParagraph(run);
                int level = item.getLevel();
                if (NumberingItemRenderData.LEVEL_NORMAL != level) {
                    paragraph.setNumID(numID);
                    paragraph.setNumILvl(BigInteger.valueOf(level));
                }
                XWPFRun createRun = paragraph.createRun();
//                 StyleUtils.styleParaRpr(paragraph, StyleUtils.retriveStyle(run));
                StyleUtils.styleRun(createRun, run);
                ParagraphRenderPolicy.Helper.renderParagraph(createRun, item.getItem());
            }
        }

    }
}
