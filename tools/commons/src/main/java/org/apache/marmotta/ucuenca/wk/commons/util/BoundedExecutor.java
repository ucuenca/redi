/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Jose Ortiz
 */
public class BoundedExecutor {

    private final Executor exec;
    private final Semaphore semaphore;
    private int maxThreads = 0;

    public static BoundedExecutor getThreadPool(int mx) {
        ExecutorService executorServicex = Executors.newFixedThreadPool(mx);
        return new BoundedExecutor(executorServicex, mx);
    }

    public BoundedExecutor(Executor exec, int bound) {
        this.exec = exec;
        this.semaphore = new Semaphore(bound);
        this.maxThreads = bound;
    }

    public int availableThreads() {
        return this.maxThreads - semaphore.availablePermits();
    }

    public void submitTask(final Runnable command)
            throws InterruptedException {
        semaphore.acquire();
        try {
            exec.execute(new Runnable() {
                public void run() {
                    try {
                        command.run();
                    } finally {
                        semaphore.release();
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            semaphore.release();
        }
    }

    public void end() throws InterruptedException {
        ExecutorService sexec = (ExecutorService) exec;
        sexec.shutdown();
        sexec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

}
