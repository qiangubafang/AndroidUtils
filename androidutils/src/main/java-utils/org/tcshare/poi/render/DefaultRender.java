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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.tcshare.poi.MyLogger;


import org.tcshare.poi.XWPFTemplate;
import org.tcshare.poi.exception.RenderException;
import org.tcshare.poi.policy.DocxRenderPolicy;
import org.tcshare.poi.policy.RenderPolicy;
import org.tcshare.poi.render.compute.RenderDataCompute;
import org.tcshare.poi.render.processor.DelegatePolicy;
import org.tcshare.poi.render.processor.DocumentProcessor;
import org.tcshare.poi.render.processor.LogProcessor;
import org.tcshare.poi.template.MetaTemplate;
import org.tcshare.poi.template.run.RunTemplate;
import org.tcshare.poi.xwpf.NiceXWPFDocument;

/**
 * default render
 * 
 * @author Sayi
 * @since 1.7.0
 */
public class DefaultRender implements Render {

    private static final MyLogger logger = MyLogger.getLogger(DefaultRender.class);

    public DefaultRender() {
    }

    @Override
    public void render(XWPFTemplate template, Object root) {
        Objects.requireNonNull(template, "Template must not be null.");
        Objects.requireNonNull(root, "Data root must not be null");

        logger.info("Render template start...");

        RenderDataCompute renderDataCompute = template.getConfig().getRenderDataComputeFactory().newCompute(root);
        long start = System.nanoTime();
        try {
            renderTemplate(template, renderDataCompute);
            renderInclude(template, renderDataCompute);

        } catch (Exception e) {
            if (e instanceof RenderException) throw (RenderException) e;
            throw new RenderException("Cannot render docx template", e);
        } finally {

        }
        logger.info("Successfully Render template in %d millis", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
    }

    private void renderTemplate(XWPFTemplate template, RenderDataCompute renderDataCompute) {
        // log
        new LogProcessor().process(template.getElementTemplates());

        // render
        DocumentProcessor documentRender = new DocumentProcessor(template, template.getResolver(), renderDataCompute);
        documentRender.process(template.getElementTemplates());
    }

    private void renderInclude(XWPFTemplate template, RenderDataCompute renderDataCompute) throws IOException {
        List<MetaTemplate> elementTemplates = template.getElementTemplates();
        long docxCount = elementTemplates.stream()
                .filter(meta -> (meta instanceof RunTemplate
                        && ((RunTemplate) meta).findPolicy(template.getConfig()) instanceof DocxRenderPolicy))
                .count();
        if (docxCount >= 1) {
            template.reload(template.getXWPFDocument().generate());
            applyDocxPolicy(template, renderDataCompute, docxCount);
        }
    }

    private void applyDocxPolicy(XWPFTemplate template, RenderDataCompute renderDataCompute, long docxItems) {
        RenderPolicy policy = null;
        NiceXWPFDocument current = template.getXWPFDocument();
        List<MetaTemplate> elementTemplates = template.getElementTemplates();
        int k = 0;
        while (k < elementTemplates.size()) {
            for (int j = 0; j < elementTemplates.size(); k=++j) {
                MetaTemplate metaTemplate = elementTemplates.get(j);
                if (!(metaTemplate instanceof RunTemplate)) continue;
                RunTemplate runTemplate = (RunTemplate) metaTemplate;
                policy = runTemplate.findPolicy(template.getConfig());
                if (!(policy instanceof DocxRenderPolicy)) {
                    continue;
                }
                DelegatePolicy.invoke(policy, runTemplate, renderDataCompute.compute(runTemplate.getTagName()), template);

                if (current != template.getXWPFDocument()) {
                    current = template.getXWPFDocument();
                    elementTemplates = template.getElementTemplates();
                    k = 0;
                    break;
                }
            }
        }
    }

}
