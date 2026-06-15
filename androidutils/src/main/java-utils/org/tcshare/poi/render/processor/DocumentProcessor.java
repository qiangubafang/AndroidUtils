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
package org.tcshare.poi.render.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import org.tcshare.poi.XWPFTemplate;
import org.tcshare.poi.render.compute.RenderDataCompute;
import org.tcshare.poi.resolver.Resolver;
import org.tcshare.poi.template.BlockTemplate;
import org.tcshare.poi.template.ChartTemplate;
import org.tcshare.poi.template.InlineIterableTemplate;
import org.tcshare.poi.template.IterableTemplate;
import org.tcshare.poi.template.MetaTemplate;
import org.tcshare.poi.template.PictImageTemplate;
import org.tcshare.poi.template.PictureTemplate;
import org.tcshare.poi.template.run.RunTemplate;
import org.tcshare.poi.xwpf.XWPFTextboxContent;

/**
 * Process all templates of the document
 * 
 * @author Sayi
 *
 */
public class DocumentProcessor implements Visitor {

    private ElementProcessor elementProcessor;
    private IterableProcessor iterableProcessor;
    private InlineIterableProcessor inlineIterableProcessor;

    public DocumentProcessor(XWPFTemplate template, final Resolver resolver,
            final RenderDataCompute renderDataCompute) {
        elementProcessor = new ElementProcessor(template, resolver, renderDataCompute);
        iterableProcessor = new IterableProcessor(template, resolver, renderDataCompute);
        inlineIterableProcessor = new InlineIterableProcessor(template, resolver, renderDataCompute);
    }

    public void process(List<MetaTemplate> templates) {
        // process in order( or sort first)
        templates.forEach(template -> template.accept(this));
        Set<XWPFTextboxContent> textboxs = obtainTextboxes(templates);
        textboxs.forEach(content -> {
            content.getXmlObject().set(content.getCTTxbxContent());
        });
    }

    @SuppressWarnings("deprecation")
    private Set<XWPFTextboxContent> obtainTextboxes(List<MetaTemplate> templates) {
        Set<XWPFTextboxContent> textboxs = new HashSet<>();
        if (CollectionUtils.isEmpty(templates)) return textboxs;
        templates.forEach(template -> {
            RunTemplate checkTemplate = template instanceof RunTemplate ? (RunTemplate) template
                    : (template instanceof BlockTemplate ? ((BlockTemplate) template).getStartMark() : null);
            if (null != checkTemplate) {
                if (checkTemplate.getRun().getParent() instanceof XWPFParagraph
                        && checkTemplate.getRun().getParagraph().getBody() instanceof XWPFTextboxContent) {
                    textboxs.add((XWPFTextboxContent) checkTemplate.getRun().getParagraph().getBody());
                }
            }
        });
        return textboxs;
    }

    @Override
    public void visit(InlineIterableTemplate iterableTemplate) {
        iterableTemplate.accept(inlineIterableProcessor);
    }

    @Override
    public void visit(IterableTemplate iterableTemplate) {
        iterableTemplate.accept(iterableProcessor);
    }

    @Override
    public void visit(RunTemplate runTemplate) {
        runTemplate.accept(elementProcessor);
    }

    @Override
    public void visit(PictureTemplate pictureTemplate) {
        pictureTemplate.accept(elementProcessor);
    }

    @Override
    public void visit(PictImageTemplate pictImageTemplate) {
        pictImageTemplate.accept(elementProcessor);
    }

    @Override
    public void visit(ChartTemplate chartTemplate) {
        chartTemplate.accept(elementProcessor);
    }

}
