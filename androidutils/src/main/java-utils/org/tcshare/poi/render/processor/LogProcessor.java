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

import java.util.List;

import org.tcshare.poi.MyLogger;


import org.tcshare.poi.template.ChartTemplate;
import org.tcshare.poi.template.InlineIterableTemplate;
import org.tcshare.poi.template.IterableTemplate;
import org.tcshare.poi.template.MetaTemplate;
import org.tcshare.poi.template.PictImageTemplate;
import org.tcshare.poi.template.PictureTemplate;
import org.tcshare.poi.template.run.RunTemplate;

public class LogProcessor implements Visitor {

    private static MyLogger log = MyLogger.getLogger(LogProcessor.class);

    private String indentState;

    public LogProcessor() {
        this.indentState = "";
    }

    public LogProcessor(String indent) {
        this.indentState = indent;
    }

    public void process(List<MetaTemplate> templates) {
        templates.forEach(template -> template.accept(this));
    }

    @Override
    public void visit(InlineIterableTemplate iterableTemplate) {
        visit((IterableTemplate) iterableTemplate);
    }

    @Override
    public void visit(RunTemplate runTemplate) {
        log.info("%s%s", indentState, runTemplate);
    }

    @Override
    public void visit(PictureTemplate pictureTemplate) {
        log.info("%s%s", indentState, pictureTemplate);
    }

    @Override
    public void visit(PictImageTemplate pictImageTemplate) {
        log.info("%s%s", indentState, pictImageTemplate);
    }

    @Override
    public void visit(IterableTemplate iterableTemplate) {
        log.info("%s%s", indentState, iterableTemplate.getStartMark());
        new LogProcessor(indentState + "  ").process(iterableTemplate.getTemplates());
        log.info("%s%s", indentState, iterableTemplate.getEndMark());
    }

    @Override
    public void visit(ChartTemplate chartTemplate) {
        log.info("%s%s", indentState, chartTemplate);
    }

}
