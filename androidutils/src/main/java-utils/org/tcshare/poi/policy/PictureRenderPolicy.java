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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.tcshare.poi.converter.ObjectToPictureRenderDataConverter;
import org.tcshare.poi.converter.ToRenderDataConverter;
import org.tcshare.poi.data.PictureRenderData;
import org.tcshare.poi.data.PictureType;
import org.tcshare.poi.data.style.PictureStyle;
import org.tcshare.poi.data.style.PictureStyle.PictureAlign;
import org.tcshare.poi.exception.RenderException;
import org.tcshare.poi.render.RenderContext;
import org.tcshare.poi.util.SVGConvertor;
import org.tcshare.poi.util.UnitUtils;
import org.tcshare.poi.xwpf.BodyContainer;
import org.tcshare.poi.xwpf.BodyContainerFactory;
import org.tcshare.poi.xwpf.WidthScalePattern;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * picture render
 *
 * @author Sayi
 */
public class PictureRenderPolicy extends AbstractRenderPolicy<PictureRenderData> {

    private static ToRenderDataConverter<Object, PictureRenderData> converter = new ObjectToPictureRenderDataConverter();

    @Override
    public PictureRenderData cast(Object source) throws Exception {
        return converter.convert(source);
    }

    @Override
    protected boolean validate(PictureRenderData data) {
        return null != data;
    }

    @Override
    public void doRender(RenderContext<PictureRenderData> context) throws Exception {
        Helper.renderPicture(context.getRun(), context.getData());
    }

    @Override
    protected void afterRender(RenderContext<PictureRenderData> context) {
        clearPlaceholder(context, false);
    }

    @Override
    protected void reThrowException(RenderContext<PictureRenderData> context, Exception e) {
        logger.info("Render picture " + context.getEleTemplate() + " error: %s", e.getMessage());
        String alt = context.getData().getAltMeta();
        context.getRun().setText(alt, 0);
    }

    public static class Helper {
        public static void renderPicture(XWPFRun run, PictureRenderData picture) throws Exception {
            byte[] imageBytes = picture.readPictureData();
            if (null == imageBytes) {
                throw new IllegalStateException("Can't read picture byte arrays!");
            }
            PictureType pictureType = picture.getPictureType();
            if (null == pictureType) {
                pictureType = PictureType.suggestFileType(imageBytes);
            }
            if (null == pictureType) {
                throw new RenderException("PictureRenderData must set picture type!");
            }

            PictureStyle style = picture.getPictureStyle();
            if (null == style) style = new PictureStyle();
            int width = style.getWidth();
            int height = style.getHeight();
            int svgScale = style.getSvgScale();

            if (pictureType == PictureType.SVG) {
                imageBytes = SVGConvertor.toPng(imageBytes, (float) width, (float) height, svgScale);
                pictureType = PictureType.PNG;
            }
            if (!isSetSize(style)) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                if (style.getScalePattern() == WidthScalePattern.FIT) {
                    BodyContainer bodyContainer = BodyContainerFactory.getBodyContainer(run);
                    int pageWidth = UnitUtils
                        .twips2Pixel(bodyContainer.elementPageWidth((IBodyElement) run.getParent()));
                    if (width > pageWidth) {
                        double ratio = pageWidth / (double) width;
                        width = pageWidth;
                        height = (int) (height * ratio);
                    }
                }
            }
            try (InputStream stream = new ByteArrayInputStream(imageBytes)) {
                PictureAlign align = style.getAlign();
                if (null != align && run.getParent() instanceof XWPFParagraph) {
                    ((XWPFParagraph) run.getParent()).setAlignment(ParagraphAlignment.valueOf(align.ordinal() + 1));
                }
                run.addPicture(stream, pictureType.type(), "Generated", Units.pixelToEMU(width),
                    Units.pixelToEMU(height));
            }
        }

        private static boolean isSetSize(PictureStyle style) {
            return (style.getWidth() != 0 || style.getHeight() != 0)
                && style.getScalePattern() == WidthScalePattern.NONE;
        }
    }
}