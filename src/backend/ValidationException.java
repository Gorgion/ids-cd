/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

/**
 * This exception is thrown when validation of XML data source fails.
 *
 * @author ids-cd team
 */
public class ValidationException extends RuntimeException {

    public ValidationException() {
    }

    /**
     * @param msg the message
     * @param cause the cause
     */
    public ValidationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
