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


import org.tcshare.poi.XWPFTemplate;
import org.tcshare.poi.render.compute.RenderDataCompute;
import org.tcshare.poi.resolver.Resolver;
import org.tcshare.poi.template.IterableTemplate;
import org.tcshare.poi.template.MetaTemplate;
import org.tcshare.poi.xwpf.BodyContainer;
import org.tcshare.poi.xwpf.BodyContainerFactory;

public abstract class AbstractIterableProcessor extends DefaultTemplateProcessor implements Iteration {

    protected MyLogger logger = MyLogger.getLogger(this.getClass());

    public AbstractIterableProcessor(XWPFTemplate template, Resolver resolver, RenderDataCompute renderDataCompute) {
        super(template, resolver, renderDataCompute);
    }

    @Override
    public void visit(IterableTemplate iterableTemplate) {
        BodyContainer bodyContainer = BodyContainerFactory.getBodyContainer(iterableTemplate);
        Object compute = renderDataCompute.compute(iterableTemplate.getStartMark().getTagName());

        if (null == compute || (compute instanceof Boolean && !(Boolean) compute)) {
            handleNever(iterableTemplate, bodyContainer);
            afterHandle(iterableTemplate, bodyContainer, true);
        } else if (compute instanceof Iterable) {
            handleIterable(iterableTemplate, bodyContainer, (Iterable<?>) compute);
            afterHandle(iterableTemplate, bodyContainer, false);
        } else {
            if (compute instanceof Boolean && (Boolean) compute) {
                handleOnceWithScope(iterableTemplate, renderDataCompute);
            } else {
                handleOnce(iterableTemplate, compute);
            }
            afterHandle(iterableTemplate, bodyContainer, false);
        }
    }

    protected void afterHandle(IterableTemplate iterableTemplate, BodyContainer bodyContainer, boolean remove) {
        bodyContainer.clearPlaceholder(iterableTemplate.getStartRun(), remove);
        bodyContainer.clearPlaceholder(iterableTemplate.getEndRun(), remove);
    }

    protected abstract void handleNever(IterableTemplate iterableTemplate, BodyContainer bodyContainer);

    protected abstract void handleIterable(IterableTemplate iterableTemplate, BodyContainer bodyContainer,
            Iterable<?> compute);

    protected void handleOnce(IterableTemplate iterableTemplate, Object compute) {
        process(iterableTemplate.getTemplates(), compute);
    }

    protected void handleOnceWithScope(IterableTemplate iterableTemplate, RenderDataCompute dataCompute) {
        new DocumentProcessor(this.template, this.resolver, dataCompute).process(iterableTemplate.getTemplates());
    }

    protected void process(List<MetaTemplate> templates, Object model) {
        RenderDataCompute dataCompute = template.getConfig().getRenderDataComputeFactory().newCompute(model);
        new DocumentProcessor(this.template, this.resolver, dataCompute).process(templates);
    }

}
