/*-
 * Copyright (c) 2014-2024 Red Hat, Inc.
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
package org.fedoraproject.xmvn.connector.maven;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;

/**
 * Forwards Maven execution events to a chain of listeners.
 *
 * <p>Maven allows only one execution listener. This class can be used to workaround for this limitation.
 *
 * @author Mikolaj Izdebski
 */
class ChainedExecutionListener implements ExecutionListener {
    private final List<ExecutionListener> listeners = new ArrayList<>();

    public void addExecutionListener(ExecutionListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void projectDiscoveryStarted(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.projectDiscoveryStarted(event);
    }

    @Override
    public void sessionStarted(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.sessionStarted(event);
    }

    @Override
    public void sessionEnded(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.sessionEnded(event);
    }

    @Override
    public void projectSkipped(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.projectSkipped(event);
    }

    @Override
    public void projectStarted(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.projectStarted(event);
    }

    @Override
    public void projectSucceeded(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.projectSucceeded(event);
    }

    @Override
    public void projectFailed(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.projectFailed(event);
    }

    @Override
    public void mojoSkipped(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.mojoSkipped(event);
    }

    @Override
    public void mojoStarted(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.mojoStarted(event);
    }

    @Override
    public void mojoSucceeded(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.mojoSucceeded(event);
    }

    @Override
    public void mojoFailed(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.mojoFailed(event);
    }

    @Override
    public void forkStarted(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.forkStarted(event);
    }

    @Override
    public void forkSucceeded(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.forkSucceeded(event);
    }

    @Override
    public void forkFailed(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.forkFailed(event);
    }

    @Override
    public void forkedProjectStarted(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.forkedProjectStarted(event);
    }

    @Override
    public void forkedProjectSucceeded(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.forkedProjectSucceeded(event);
    }

    @Override
    public void forkedProjectFailed(ExecutionEvent event) {
        for (ExecutionListener listener : listeners) listener.forkedProjectFailed(event);
    }
}
