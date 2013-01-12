/*
 * Copyright 2012-2013 the original author or authors.
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

package lombok.core.handlers;

import lombok.ast.Expression;
import lombok.ast.IMethod;
import lombok.ast.IType;

import static lombok.ast.AST.*;

/**
 * @author Andres Almiray
 */
public abstract class ActivitiAwareHandler<TYPE_TYPE extends IType<? extends IMethod<?, ?, ?, ?>, ?, ?, ?, ?, ?>> extends AbstractHandler<TYPE_TYPE> implements ActivitiAwareConstants {
    private Expression<?> defaultActivitiProviderInstance() {
        return Call(Name(DEFAULT_ACTIVITI_PROVIDER_TYPE), "getInstance");
    }

    public void addActivitiProviderField(final TYPE_TYPE type) {
        addField(type, ACTIVITI_PROVIDER_TYPE, ACTIVITI_PROVIDER_FIELD_NAME, defaultActivitiProviderInstance());
    }

    public void addActivitiProviderAccessors(final TYPE_TYPE type) {
        type.editor().injectMethod(
            MethodDecl(Type(VOID), METHOD_SET_ACTIVITI_PROVIDER)
                .makePublic()
                .withArgument(Arg(Type(ACTIVITI_PROVIDER_TYPE), PROVIDER))
                .withStatement(
                    If(Equal(Name(PROVIDER), Null()))
                        .Then(Block()
                            .withStatement(Assign(Field(ACTIVITI_PROVIDER_FIELD_NAME), defaultActivitiProviderInstance())))
                        .Else(Block()
                            .withStatement(Assign(Field(ACTIVITI_PROVIDER_FIELD_NAME), Name(PROVIDER)))))
        );

        type.editor().injectMethod(
            MethodDecl(Type(ACTIVITI_PROVIDER_TYPE), METHOD_GET_ACTIVITI_PROVIDER)
                .makePublic()
                .withStatement(Return(Field(ACTIVITI_PROVIDER_FIELD_NAME)))
        );
    }

    public void addActivitiContributionMethods(final TYPE_TYPE type) {
        delegateMethodsTo(type, METHODS, Field(ACTIVITI_PROVIDER_FIELD_NAME));
    }
}
