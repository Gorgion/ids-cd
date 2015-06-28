/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend.entity;

/**
 *
 * @author Tomáš
 */
public class Prices
{

    private CdTickets cdTicket;

    private OptimalTicket idsTicket;

    public Prices(CdTickets cdTicket, OptimalTicket idsTicket)
    {
        this.cdTicket = cdTicket;
        this.idsTicket = idsTicket;
    }

    public Prices()
    {
    }

    public CdTickets getCdTicket()
    {
        return cdTicket;
    }

    public void setCdTicket(CdTickets cdTicket)
    {
        this.cdTicket = cdTicket;
    }

    public OptimalTicket getIdsTicket()
    {
        return idsTicket;
    }

    public void setIdsTicket(OptimalTicket idsTicket)
    {
        this.idsTicket = idsTicket;
    }
}
