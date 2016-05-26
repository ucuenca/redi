/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.authors.exceptions;

    /**
     * Creates a new instance of <code>DaoException</code> without detail
     * message.
     */
public class AskException extends Exception {

    /**
     * Creates a new instance of <code>DaoException</code> without detail
     * message.
     */
    
    public AskException() {
    }

    /**
     * Constructs an instance of <code>AskException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public AskException(String msg) {
        super(msg);
    }
    
}
