/*
 * Copyright 2024 IBM Corporation and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an \"AS IS\" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.apache.yoko.orb.OB;

import java.util.Vector;

import org.omg.CORBA.OBJ_ADAPTER;

final class ThreadPool {
    private boolean destroy_ = false; // True if destroy was called

    private Vector requests_ = new Vector();

    private ThreadGroup group_; // Thread group for the threads in the pool

    //
    // ThreadPoolDispatcher
    //
    private class Dispatcher extends Thread {
        private ThreadPool threadPool_;

        Dispatcher(ThreadGroup group, ThreadPool threadPool, int id, int n) {
            super(group, "Yoko:ThreadPool-" + id + ":Dispatcher-" + n);
            threadPool_ = threadPool;
        }

        public void run() {
            while (true) {
                DispatchRequest req = threadPool_.get();

                //
                // ThreadPool has terminated
                //
                if (req == null)
                    return;

                req.invoke();
            }
        }
    }

    public ThreadPool(int id, int n) {
        //
        // Create a new thread group. Place each of the threads in the
        // pool in this new group.
        //
        group_ = new ThreadGroup("ThreadPool-" + id);

        //
        // Start all each of the threads in the pool
        //
        for (int i = 0; i < n; i++) {
            Thread t = new Dispatcher(group_, this, id, i);
            t.start();
        }
    }

    protected void finalize() throws Throwable {
        if (!destroy_)
            throw new InternalError();

        super.finalize();
    }

    void destroy() {
        synchronized (this) {
            if (destroy_)
                return;
            destroy_ = true;
            notifyAll();
        }

        //
        // Wait for all the threads in the pool to end
        //
        synchronized (group_) {
            while (group_.activeCount() > 0) {
                try {
                    group_.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    synchronized void add(DispatchRequest request) {
        //
        // If the thread pool has been destroyed then this is an
        // OBJ_ADAPTER error
        //
        if (destroy_)
            throw new OBJ_ADAPTER("Thread pool is destroyed");

        requests_.addElement(request);
        notify();
    }

    private synchronized DispatchRequest get() {
        while (!destroy_ && requests_.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }

        DispatchRequest result = null;
        if (!destroy_) {
            result = (DispatchRequest) requests_.firstElement();
            requests_.removeElementAt(0);
        }
        return result;
    }
}
