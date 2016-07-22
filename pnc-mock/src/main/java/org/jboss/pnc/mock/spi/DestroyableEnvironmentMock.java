/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc.mock.spi;

import org.jboss.pnc.mock.repositorymanager.RepositorySessionMock;
import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.environment.DestroyableEnvironment;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DestroyableEnvironmentMock {

    public static DestroyableEnvironment build() {
        RepositorySession repositorySession = new RepositorySessionMock();
        Path workingDir = Paths.get("/root");
        return RunningEnvironment.createInstance(
                "1",
                8080,
                "localhost",
                "build/agent/url",
                "internal,build/agent/url",
                repositorySession,
                workingDir,
                () -> {
                },
                new DebugData(false));

    }
}
