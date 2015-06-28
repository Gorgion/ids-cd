/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend.entity;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Tomáš
 */
public class CdTickets
{

    private BigDecimal totalGroupPrice;
    private List<Ticket> tickets;

    public CdTickets()
    {
    }

    public CdTickets(BigDecimal totalGroupPrice, List<Ticket> tickets)
    {
        this.totalGroupPrice = totalGroupPrice;
        this.tickets = tickets;
    }

    public List<Ticket> getTickets()
    {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets)
    {
        this.tickets = tickets;
    }

    public BigDecimal getTotalGroupPrice()
    {
        return totalGroupPrice;
    }

    public void setTotalGroupPrice(BigDecimal totalGroupPrice)
    {
        this.totalGroupPrice = totalGroupPrice;
    }

    public static class Ticket
    {

        private String ticketName;
        private BigDecimal ticketPrice;
        private int ticketsCount;

        public Ticket()
        {
        }

        public Ticket(String ticketName, BigDecimal ticketPrice, int ticketsCount)
        {
            this.ticketName = ticketName;
            this.ticketPrice = ticketPrice;
            this.ticketsCount = ticketsCount;
        }

        public String getTicketName()
        {
            return ticketName;
        }

        public void setTicketName(String ticketName)
        {
            this.ticketName = ticketName;
        }

        public BigDecimal getTicketPrice()
        {
            return ticketPrice;
        }

        public void setTicketPrice(BigDecimal ticketPrice)
        {
            this.ticketPrice = ticketPrice;
        }

        public int getTicketsCount()
        {
            return ticketsCount;
        }

        public void setTicketsCount(int ticketsCount)
        {
            this.ticketsCount = ticketsCount;
        }
    }
}
