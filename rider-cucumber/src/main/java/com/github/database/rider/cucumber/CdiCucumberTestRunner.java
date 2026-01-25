/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.database.rider.cucumber;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Singleton;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.core.util.ServiceUtils;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestSuiteRunner;
import org.apache.deltaspike.testcontrol.api.literal.TestControlLiteral;
import org.apache.deltaspike.testcontrol.spi.ExternalContainer;
import org.apache.deltaspike.testcontrol.spi.TestAware;
import org.apache.deltaspike.testcontrol.spi.TestControlValidator;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import cucumber.api.junit.Cucumber;
import cucumber.runtime.junit.FeatureRunner;

/**
 * Created by pestano on 05/10/15.
 */
public class CdiCucumberTestRunner extends Cucumber
{

    private static final Logger LOGGER = Logger.getLogger(CdiCucumberTestRunner.class.getName());

    private boolean containerStarted = false;


    private ContainerAwareTestContext testContext;

    public CdiCucumberTestRunner(Class<?> testClass) throws InitializationError, IOException
    {
        super(testClass);
        TestControl testControl = testClass.getAnnotation(TestControl.class);
        this.testContext = new ContainerAwareTestContext(testControl, null);

        //benefits from the fallback-handling in ContainerAwareTestContext
        Class<? extends Handler> logHandlerClass = this.testContext.getLogHandlerClass();

        if (!Handler.class.equals(logHandlerClass))
        {
            try
            {
                LOGGER.addHandler(logHandlerClass.newInstance());
            }
            catch (Exception e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }
    }

    @Override
    public void run(RunNotifier runNotifier)
    {
        CdiContainer container = CdiContainerLoader.getCdiContainer();

        if (!containerStarted)
        {
            container.boot(CdiTestSuiteRunner.getTestContainerConfig());
            containerStarted = true;
        }

        super.run(runNotifier);
    }


    @Override
    protected void runChild(FeatureRunner featureRunner, RunNotifier notifier)
    {

        TestControl testControl = getTestClass().getJavaClass().getAnnotation(TestControl.class);

        ContainerAwareTestContext currentTestContext =
                new ContainerAwareTestContext(testControl, this.testContext);

        currentTestContext.applyBeforeFeatureConfig(getTestClass().getJavaClass());
        try
        {
            super.runChild(featureRunner, notifier);
        }
        finally
        {
            currentTestContext.applyAfterFeatureConfig();
        }
    }


    private static class ContainerAwareTestContext {
        private ContainerAwareTestContext parent;

        private final ProjectStage projectStage;
        private final TestControl testControl;

        private ProjectStage previousProjectStage;

        private boolean containerStarted = false; //only true for the layer it was started in

        private Stack<Class<? extends Annotation>> startedScopes = new Stack<Class<? extends Annotation>>();

        private List<ExternalContainer> externalContainers;

        ContainerAwareTestContext(TestControl testControl, ContainerAwareTestContext parent) {
            this.parent = parent;

            Class<? extends ProjectStage> foundProjectStageClass;
            if (testControl == null) {
                this.testControl = new TestControlLiteral();
                if (parent != null) {
                    foundProjectStageClass = parent.testControl.projectStage();
                } else {
                    foundProjectStageClass = this.testControl.projectStage();
                }
            } else {
                this.testControl = testControl;
                foundProjectStageClass = this.testControl.projectStage();
            }
            this.projectStage = ProjectStage.valueOf(foundProjectStageClass.getSimpleName());

            ProjectStageProducer.setProjectStage(this.projectStage);
        }

        boolean isContainerStarted() {
            return this.containerStarted || (this.parent != null && this.parent.isContainerStarted());
        }

        Class<? extends Handler> getLogHandlerClass() {
            return this.testControl.logHandler();
        }

        void applyBeforeFeatureConfig(Class testClass) {
            CdiContainer container = CdiContainerLoader.getCdiContainer();

            if (!isContainerStarted()) {
                container.boot(CdiTestSuiteRunner.getTestContainerConfig());
                containerStarted = true;
                bootExternalContainers(testClass);
            }

            List<Class<? extends Annotation>> restrictedScopes = new ArrayList<Class<? extends Annotation>>();

            //controlled by the container and not supported by weld:
            restrictedScopes.add(ApplicationScoped.class);
            restrictedScopes.add(Singleton.class);

            if (this.parent == null && this.testControl.getClass().equals(TestControlLiteral.class)) {
                //skip scope-handling if @TestControl isn't used explicitly on the test-class -> TODO re-visit it
                restrictedScopes.add(RequestScoped.class);
                restrictedScopes.add(SessionScoped.class);
            }

            this.previousProjectStage = ProjectStageProducer.getInstance().getProjectStage();
            ProjectStageProducer.setProjectStage(this.projectStage);

            startScopes(container, testClass, null, restrictedScopes.toArray(new Class[restrictedScopes.size()]));
        }

        private void bootExternalContainers(Class testClass) {
            if (!this.testControl.startExternalContainers()) {
                return;
            }

            if (this.externalContainers == null) {
                List<ExternalContainer> configuredExternalContainers =
                        ServiceUtils.loadServiceImplementations(ExternalContainer.class);
                Collections.sort(configuredExternalContainers, new Comparator<ExternalContainer>() {
                    @Override
                    public int compare(ExternalContainer ec1, ExternalContainer ec2) {
                        return ec1.getOrdinal() > ec2.getOrdinal() ? 1 : -1;
                    }
                });

                this.externalContainers = new ArrayList<ExternalContainer>(configuredExternalContainers.size());

                ExternalContainer externalContainerBean;
                for (ExternalContainer externalContainer : configuredExternalContainers) {
                    //needed to use cdi-observers in the container optionally
                    externalContainerBean = BeanProvider.getContextualReference(externalContainer.getClass(), true);

                    if (externalContainerBean != null) {
                        this.externalContainers.add(externalContainerBean);
                    } else {
                        this.externalContainers.add(externalContainer);
                    }
                }

                for (ExternalContainer externalContainer : this.externalContainers) {
                    try {
                        if (externalContainer instanceof TestAware) {
                            ((TestAware) externalContainer).setTestClass(testClass);
                        }
                        externalContainer.boot();
                    } catch (RuntimeException e) {
                        Logger.getLogger(CdiCucumberTestRunner.class.getName()).log(Level.WARNING,
                                "booting " + externalContainer.getClass().getName() + " failed", e);
                    }
                }
            }
        }

        void applyAfterFeatureConfig() {
            ProjectStageProducer.setProjectStage(previousProjectStage);
            previousProjectStage = null;

            CdiContainer container = CdiContainerLoader.getCdiContainer();

            stopStartedScopes(container);

            if (this.containerStarted) {
                if (isStopContainerAllowed()) {
                    shutdownExternalContainers();

                    container.shutdown(); //stop the container on the same level which started it
                    containerStarted = false;
                }
            }
        }

        private boolean isStopContainerAllowed() {
            return true;
        }

        private void shutdownExternalContainers() {
            if (this.externalContainers == null) {
                return;
            }

            for (ExternalContainer externalContainer : this.externalContainers) {
                try {
                    externalContainer.shutdown();
                } catch (RuntimeException e) {
                    Logger.getLogger(CdiCucumberTestRunner.class.getName()).log(Level.WARNING,
                            "shutting down " + externalContainer.getClass().getName() + " failed", e);
                }
            }
        }


        private void startScopes(CdiContainer container,
                                 Class testClass,
                                 Method testMethod,
                                 Class<? extends Annotation>... restrictedScopes) {

            ContextControl contextControl = container.getContextControl();

            List<Class<? extends Annotation>> scopeClasses = new ArrayList<Class<? extends Annotation>>();

            Collections.addAll(scopeClasses, this.testControl.startScopes());

            if (scopeClasses.isEmpty()) {
                addScopesForDefaultBehavior(scopeClasses);
            } else {
                List<TestControlValidator> testControlValidatorList =
                        ServiceUtils.loadServiceImplementations(TestControlValidator.class);

                for (TestControlValidator testControlValidator : testControlValidatorList) {
                    if (testControlValidator instanceof TestAware) {
                        if (testMethod != null) {
                            ((TestAware) testControlValidator).setTestMethod(testMethod);
                        }
                        ((TestAware) testControlValidator).setTestClass(testClass);
                    }
                    try {
                        testControlValidator.validate(this.testControl);
                    } finally {
                        if (testControlValidator instanceof TestAware) {
                            ((TestAware) testControlValidator).setTestClass(null);
                            ((TestAware) testControlValidator).setTestMethod(null);
                        }
                    }
                }
            }

            for (Class<? extends Annotation> scopeAnnotation : scopeClasses) {
                if (this.parent != null && this.parent.isScopeStarted(scopeAnnotation)) {
                    continue;
                }

                if (isRestrictedScope(scopeAnnotation, restrictedScopes)) {
                    continue;
                }

                try {
                    //force a clean context - TODO discuss onScopeStopped call
                    contextControl.stopContext(scopeAnnotation);

                    contextControl.startContext(scopeAnnotation);
                    this.startedScopes.add(scopeAnnotation);

                    onScopeStarted(scopeAnnotation);
                } catch (RuntimeException e) {
                    Logger logger = Logger.getLogger(CdiCucumberTestRunner.class.getName());
                    logger.setLevel(Level.SEVERE);
                    logger.log(Level.SEVERE, "failed to start scope @" + scopeAnnotation.getName(), e);
                }
            }

        }

        private void addScopesForDefaultBehavior(List<Class<? extends Annotation>> scopeClasses) {
            if (this.parent != null && !this.parent.isScopeStarted(RequestScoped.class)) {
                if (!scopeClasses.contains(RequestScoped.class)) {
                    scopeClasses.add(RequestScoped.class);
                }
            }
            if (this.parent != null && !this.parent.isScopeStarted(SessionScoped.class)) {
                if (!scopeClasses.contains(SessionScoped.class)) {
                    scopeClasses.add(SessionScoped.class);
                }
            }
        }

        private boolean isRestrictedScope(Class<? extends Annotation> scopeAnnotation,
                                          Class<? extends Annotation>[] restrictedScopes) {
            for (Class<? extends Annotation> restrictedScope : restrictedScopes) {
                if (scopeAnnotation.equals(restrictedScope)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isScopeStarted(Class<? extends Annotation> scopeAnnotation) {
            return this.startedScopes.contains(scopeAnnotation);
        }

        private void stopStartedScopes(CdiContainer container) {
            while (!this.startedScopes.empty()) {
                Class<? extends Annotation> scopeAnnotation = this.startedScopes.pop();
                //TODO check if context was started by parent
                try {
                    container.getContextControl().stopContext(scopeAnnotation);
                    onScopeStopped(scopeAnnotation);
                } catch (RuntimeException e) {
                    Logger logger = Logger.getLogger(CdiCucumberTestRunner.class.getName());
                    logger.setLevel(Level.SEVERE);
                    logger.log(Level.SEVERE, "failed to stop scope @" + scopeAnnotation.getName(), e);
                }
            }
        }

        private void onScopeStarted(Class<? extends Annotation> scopeClass) {
            List<ExternalContainer> externalContainerList = collectExternalContainers(this);

            for (ExternalContainer externalContainer : externalContainerList) {
                externalContainer.startScope(scopeClass);
            }
        }

        private void onScopeStopped(Class<? extends Annotation> scopeClass) {
            List<ExternalContainer> externalContainerList = collectExternalContainers(this);

            for (ExternalContainer externalContainer : externalContainerList) {
                externalContainer.stopScope(scopeClass);
            }
        }

        private static List<ExternalContainer> collectExternalContainers(ContainerAwareTestContext testContext) {
            List<ExternalContainer> result = new ArrayList<ExternalContainer>();

            if (testContext.externalContainers != null) {
                result.addAll(testContext.externalContainers);
            }

            if (testContext.parent != null) {
                result.addAll(collectExternalContainers(testContext.parent));
            }
            return result;
        }

    }

}
