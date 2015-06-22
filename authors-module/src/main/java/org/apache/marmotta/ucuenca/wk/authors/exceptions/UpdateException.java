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
public class UpdateException extends Exception {

    /**
     * Creates a new instance of <code>DaoException</code> without detail
     * message.
     */
    
    public UpdateException() {
    }

    /**
     * Constructs an instance of <code>UpdateException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public UpdateException(String msg) {
        super(msg);
    }
    
}
