/*
 *
 *  Copyright 2016-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package springfox.bean.validators.plugins.parameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.bean.validators.plugins.Validators;
import springfox.documentation.service.AllowableRangeValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;

import javax.validation.constraints.Size;
import java.util.Optional;

import static springfox.bean.validators.plugins.RangeAnnotations.*;
import static springfox.bean.validators.plugins.Validators.*;

@Component
@Order(Validators.BEAN_VALIDATOR_PLUGIN_ORDER)
public class SizeAnnotationPlugin implements ParameterBuilderPlugin {

  private static final Logger LOG = LoggerFactory.getLogger(SizeAnnotationPlugin.class);

  @Override
  public boolean supports(DocumentationType delimiter) {
    // we simply support all documentationTypes!
    return true;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void apply(ParameterContext context) {
    Optional<Size> size = annotationFromParameter(context, Size.class);
    LOG.debug("searching for @size: {}", size.isPresent());
    if (size.isPresent()) {
      AllowableRangeValues values = stringLengthRange(size.get());
      LOG.debug("Adding allowable Values @Size: {} - {}", values.getMin(), values.getMax());
      context.parameterBuilder().allowableValues(values);
      context.requestParameterBuilder()
             .query(q -> q.stringFacet(s -> {
               s.minLength(tryGetInteger(values.getMin()).orElse(null));
               s.maxLength(tryGetInteger(values.getMax()).orElse(null));
             }));
    }
  }

  private Optional<Integer> tryGetInteger(String min) {
    try {
      return Optional.of(Integer.valueOf(min));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }
}
