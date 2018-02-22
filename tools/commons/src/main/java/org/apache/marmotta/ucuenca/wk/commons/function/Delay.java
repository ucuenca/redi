/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.function;

/**
 *
 * @author José Ortiz
 */
public final class Delay {

    private Delay() {
    }

    public static void call() {
        long get1 = 1 + (long) (Math.round(2.0 * Math.random()));
        try {
            Thread.sleep(get1*1000);
        } catch (InterruptedException ex) {
        }
    }
}
