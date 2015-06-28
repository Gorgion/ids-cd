/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package backend;

/**
 *
 * @author Tomáš
 */
public class ServiceException extends RuntimeException
{
    public ServiceException()
    {
        super();
    }
    
    public ServiceException(String msg)
    {
        super(msg);
    }
    
    public ServiceException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
