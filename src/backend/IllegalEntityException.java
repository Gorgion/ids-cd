/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

/**
 * This exception is thrown when entity is not suitable for operation
 *
 * @author ids-cd team
 */
public class IllegalEntityException extends RuntimeException {

    public IllegalEntityException() {
    }

    /**
     * @param msg the message
     */
    public IllegalEntityException(String msg) {
        super(msg);
    }

}
