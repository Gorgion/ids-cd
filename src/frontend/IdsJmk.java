package frontend;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.math.*;
import org.jdesktop.swingx.JXDatePicker; // swingx-all-1.6.4.jar
import org.jdesktop.swingx.JXMonthView;
import backend.*;
import backend.entity.*;

class SnapJFrame extends JFrame implements ComponentListener
{  // kvoli novym vlastnostiam Windows 7, napr. prichytenie hornej hrany okna o hranu obrazovky (tzv. snap)

    @Override
    public void componentMoved(ComponentEvent e)
    {
        invalidate();
        validate();
    } // vynuti prekreslenie okna (kvoli maximalizacii vo zvislom smere vo Windows 7)

    @Override
    public void componentResized(ComponentEvent e)
    {
    }

    @Override
    public void componentShown(ComponentEvent e)
    {
    }

    @Override
    public void componentHidden(ComponentEvent e)
    {
    }
}

//--------------------------------------------------------------------------------------
public class IdsJmk extends SnapJFrame
{ // hlavny formular (nie je potomkom rovno JFrame, ale nasej triedy (vid vyssie))

    IdsJmk_Gps jmkGps;
    typeMapa mapa;
    private JPanel lavyPanel;
    private JScrollPane pravyPanel;
    private JCheckBox chkKreslit;
    private JComboBox cmbZast1, cmbZast2;
    private JRadioButton radZast1, radZast2;
    private ButtonGroup btgZast;
    private JLabel labZast1, labZast2, labPocDosp, labPocDeti, labVysledek, labKlik1, labKlik2, labDateTime;
    private JSpinner spinDosp, spinDeti;
    private JXDatePicker datePicker;
    private JSpinner timeSpinner;

    private JButton btnClear, btnVypoc;

    private final DefaultListModel modelVysledok;

    private TariffManager tariffMan = null;

    // Nejake 2 zastavky (v nasom pripade Tisnov a Slavkov) budu tzv. referencne - podla nich budeme prevadzat pixely na gps a naopak
    int refX0 = 0, refY0 = 0, refX1 = 0, refY1 = 0;  // hodnoty suradnic v pixeloch (naplnia sa v constructore)
    double refgpsX0 = 0, refgpsY0 = 0, refgpsX1 = 0, refgpsY1 = 0; // hodnoty gps (Tisnova a Slavkova) sa naplnia neskor z backendu

    int pixX, pixY; // kvoli PixelsToGps()
    double gpsX, gpsY; // kvoli GpsToPixels()

//--------------------------------------------------------------------------------------
    class typeMapa extends JComponent
    { // ovladaci prvok obsahujuci mapu

        BufferedImage bufImage;

        public typeMapa()
        { // constructor

            String imageName = "resources/map.gif";
            try
            {
                bufImage = ImageIO.read(IdsJmk.class.getResourceAsStream(imageName)); // bufImage = ImageIO.read(new File("map.gif")); 
            } catch (Exception e)
            {
                JOptionPane.showMessageDialog(null, "Obrazok " + imageName + " sa nenasiel");
            }
            this.setPreferredSize(new Dimension(bufImage.getWidth(), bufImage.getHeight()));
        }

        @Override
        public void paint(Graphics g)
        {
            int pocZastGps, i, j, ind1, ind2;
            String currZast, zast1, zast2;
            int pX1, pY1, pX2, pY2;

            g.drawImage(bufImage, 0, 0, this); // vykreslenie obrazka

            if (chkKreslit.isSelected())
            { //(trebaKreslit) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(3)); // nastavenie hrubky pera (tj. hrubky kreslenych ciar)

                pocZastGps = jmkGps.getPocetZastavok();
                zast1 = cmbZast1.getSelectedItem().toString();
                zast2 = cmbZast2.getSelectedItem().toString();
                ind1 = -1;
                ind2 = -1;
                for (i = 0; i < pocZastGps; i++)
                { // prechadzame nase zastavky s Gps
                    GpsToPixels(jmkGps.getZastX(i), jmkGps.getZastY(i)); // vypocita pixX, pixY
                    g.setColor(Color.blue);
                    currZast = jmkGps.getZastNazov(i);
                    if ((currZast.equals(zast1)) || (currZast.equals(zast2)))
                    {
                        if (currZast.equals(zast2))
                        {
                            g.setColor(Color.red); // cielova zastavka bude inou farbou
                        }
                        g.drawOval(pixX - 12, pixY - 12, 24, 24); // pozn.: 10 = 20/2
                    } else
                    {
                        g.drawOval(pixX - 6, pixY - 6, 12, 12); // nakreslime kruzky okolo zastavok pozn.: 5 = 10/2
                    }
                    if (currZast.equals(zast1))
                    {
                        ind1 = i;
                    }
                    if (currZast.equals(zast2))
                    {
                        ind2 = i;
                    }
                }
                if ((ind1 >= 0) && (ind2 >= 0))
                {
                    GpsToPixels(jmkGps.getZastX(ind1), jmkGps.getZastY(ind1));
                    pX1 = pixX;
                    pY1 = pixY;
                    GpsToPixels(jmkGps.getZastX(ind2), jmkGps.getZastY(ind2));
                    pX2 = pixX;
                    pY2 = pixY;
                    g.setColor(Color.blue);
                    g.drawLine(pX1, pY1, pX2, pY2); // spojime prvu a druhu zastavku rovnou ciarou
                }
            }
        }
    }

//--------------------------------------------------------------------------------------
    public IdsJmk()
    { // constructor hlavnej triedy (hlavneho formulara)
        int i, pocZast;

        jmkGps = new IdsJmk_Gps();  // nasa trieda z front-endu, bude obsahovat zastavky, ktore su v backende
        pocZast = jmkGps.getPocetZastavok();
        for (i = 0; i < pocZast; i++)
        {
            if (jmkGps.getZastNazov(i).equals("Tišnov"))
            {
                refgpsX0 = jmkGps.getZastX(i);
                refgpsY0 = jmkGps.getZastY(i);
            }
            if (jmkGps.getZastNazov(i).equals("Slavkov u Brna"))
            {
                refgpsX1 = jmkGps.getZastX(i);
                refgpsY1 = jmkGps.getZastY(i);
            }

//            refX0 = 578; // Tisnov  (napr. toto je x-pixels nadrazi v Tisnove na nasom konkretnom obrazku map.gif)
//            refY0 = 460;
//            refX1 = 851; // Slavkov u Brna
//            refY1 = 648;
            refX0 = 663; // Tisnov  (napr. toto je x-pixels nadrazi v Tisnove na nasom konkretnom obrazku map.gif)
            refY0 = 579;
            refX1 = 933; // Slavkov u Brna
            refY1 = 770;

        }

        try
        {
            tariffMan = new TariffManager(); // vytvori instanciu TariffManageru na vypocet
        } catch (java.net.URISyntaxException e)
        {
        }

        modelVysledok = new DefaultListModel();

        setLayout(new GridBagLayout());
        vytvorLavyPanel();
        vytvorPravyPanel();
    }

    private void vytvorLavyPanel()
    {
        GridBagConstraints gbc, gbcForm;
        int i, j, pocZast;

        lavyPanel = new JPanel();
        lavyPanel.setLayout(new GridBagLayout());

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);  // odstupy ovl. prvkov od ciar mriezky (vo vsetkych 4 smeroch)

        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        labZast1 = new JLabel("Prva zastavka:");
        lavyPanel.add(labZast1, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        chkKreslit = new JCheckBox("kreslit");
        lavyPanel.add(chkKreslit, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        cmbZast1 = new JComboBox();
        lavyPanel.add(cmbZast1, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        radZast1 = new JRadioButton("mapa");
        lavyPanel.add(radZast1, gbc);
        cmbZast1.setMaximumRowCount(40);
        chkKreslit.setSelected(true);
        radZast1.setSelected(true);
//---------------------------------------				
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        labZast2 = new JLabel("Druha zastavka:");
        lavyPanel.add(labZast2, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        cmbZast2 = new JComboBox();
        lavyPanel.add(cmbZast2, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        radZast2 = new JRadioButton("mapa");
        lavyPanel.add(radZast2, gbc);
        cmbZast2.setMaximumRowCount(40);
//----------------------------------------		
        btgZast = new ButtonGroup();
        btgZast.add(radZast1);
        btgZast.add(radZast2); // spojenie radiobuttonov
//----------------------------------------				
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 4;
        labPocDosp = new JLabel("Pocet dospelych:");
        lavyPanel.add(labPocDosp, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        spinDosp = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        lavyPanel.add(spinDosp, gbc);
        JFormattedTextField tfDosp = ((JSpinner.DefaultEditor) spinDosp.getEditor()).getTextField(); // takto komplikovane sa nastavuje, aby sa nedalo priamo editovat
        tfDosp.setEditable(true);
        tfDosp.setFont(cmbZast1.getFont());
        tfDosp.setHorizontalAlignment(JFormattedTextField.CENTER);
        //(1,1,100,1) znamena (poc.hodn., min, max, step)
//----------------------------------------				
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 5;
        labPocDeti = new JLabel("Pocet deti:");
        lavyPanel.add(labPocDeti, gbc);
        gbc.gridx = 1;
        gbc.gridy = 5;
        spinDeti = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        lavyPanel.add(spinDeti, gbc);
        JFormattedTextField tfDeti = ((JSpinner.DefaultEditor) spinDeti.getEditor()).getTextField();
        tfDeti.setEditable(true);
        tfDeti.setFont(cmbZast1.getFont()); // nastavit rovnaky font ako ma comboBox cmbZast1
        tfDeti.setHorizontalAlignment(JFormattedTextField.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        labDateTime = new JLabel("Datum a cas odchodu:");
        lavyPanel.add(labDateTime, gbc);
//---------------------------------------- Date-time
        JXMonthView monthView = new JXMonthView();
        monthView.setFirstDayOfWeek(Calendar.MONDAY); //===== Nastavenie prveho dna na pondelok (nasl.3 riadky)- pozn.: defaultne je "Sun Mon Tue Wed Thu Fri Sat"
        datePicker = new JXDatePicker();
        datePicker.setMonthView(monthView);
        String[] datF =
        {
            "dd-MM-yyyy"
        };
        datePicker.setFormats(datF);
        datePicker.getMonthView().setZoomable(true);
//		datePicker.setLocale(Locale.UK);

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 7;
        lavyPanel.add(datePicker, gbc);
        //===== Vytvorenie a nastavenie time spinneru (nasl. 5 riadkov)
        timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        dateEditor.setPreferredSize(new Dimension(45, 20));
        timeSpinner.setEditor(dateEditor);
        timeSpinner.setValue(new Date());
        gbc.insets = new Insets(5, 20, 5, 5);
        gbc.gridwidth = GridBagConstraints.REMAINDER; //spoj vsetky bunky doprava
        gbc.gridx = 1;
        gbc.gridy = 7;
        lavyPanel.add(timeSpinner, gbc);
//----------------------------------------
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 8;
        btnClear = new JButton("Vymazat");
        lavyPanel.add(btnClear, gbc);
        gbc.gridx = 1;
        gbc.gridy = 8;
        btnVypoc = new JButton("Vypocitaj");
        lavyPanel.add(btnVypoc, gbc);
//----------------------------------------
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridx = 0;
        gbc.gridy = 9;
        labVysledek = new JLabel("Vysledek:");
        lavyPanel.add(labVysledek, gbc);
//----------------------------------------		
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridx = 0;
        gbc.gridy = 10;
        labKlik1 = new JLabel("");
        lavyPanel.add(labKlik1, gbc);
        gbc.gridx = 0;
        gbc.gridy = 11;
        labKlik2 = new JLabel("");
        lavyPanel.add(labKlik2, gbc);
        Font font = labKlik1.getFont();
        labKlik1.setFont(font.deriveFont(font.getStyle() & ~Font.BOLD)); // vypnut pismo bold
        labKlik2.setFont(font.deriveFont(font.getStyle() & ~Font.BOLD));
//---------------------------------------- naplnime comboboxy so zastavkami				
        cmbZast1.addItem("--zvolte prvu zastavku--");  // index 0
        cmbZast2.addItem("--zvolte druhu zastavku--"); // index 0

        pocZast = jmkGps.getPocetZastavok();
        for (i = 0; i < pocZast; i++)
        {
            cmbZast1.addItem(jmkGps.getZastNazov(i));  // indexy 1 .. pocZast            
            cmbZast2.addItem(jmkGps.getZastNazov(i));  // indexy 1 .. pocZast            
        }
//----------------------------------------
        gbcForm = new GridBagConstraints();
        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        gbcForm.weightx = 0; // 0 znamena, ze sa panel nebude roztahovat - bude pevny
        gbcForm.weighty = 0;
        gbcForm.fill = GridBagConstraints.NONE; // nevyplnovat zvysne miesto medzi ovladacie prvky ale nechat ho dole
        gbcForm.anchor = GridBagConstraints.NORTH;
        this.getContentPane().add(lavyPanel, gbcForm); // pridanie laveho panelu do hlavneho okna

        ClearFormular();

        chkKreslit.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                repaint();
            }
        });
        btnClear.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ClearFormular();
            }
        });
        btnVypoc.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Vypocitaj();
            }
        });
        cmbZast1.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                repaint();
                ClearLabels();
            }
        });
        cmbZast2.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                repaint();
                ClearLabels();
            }
        });
    }

    private void vytvorPravyPanel()
    {
        GridBagConstraints gbcForm;

        mapa = new typeMapa(); // nasa trieda, ktora nacita obrazok
        pravyPanel = new JScrollPane(mapa); // aby nam obrazok aj scrolloval
        pravyPanel.getVerticalScrollBar().setUnitIncrement(10);   // nastavenie rychlosti scrollovania
        pravyPanel.getHorizontalScrollBar().setUnitIncrement(10);

        gbcForm = new GridBagConstraints();
        gbcForm.gridx = 1;
        gbcForm.gridy = 0;
        gbcForm.weightx = 1; // 1 znamena, ze sa panel v gride bude roztahovat spolu s oknom
        gbcForm.weighty = 1;
        gbcForm.gridwidth = GridBagConstraints.REMAINDER;
        gbcForm.fill = GridBagConstraints.BOTH; // panel sa roztiahne na celu velkost bunky v gride
        this.getContentPane().add(pravyPanel, gbcForm); // pridanie praveho panelu (s obrazkom) do hlavneho okna

        mapa.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                KlikMysiNaObrazku(e);
            }
        });
    }

    private void KlikMysiNaObrazku(MouseEvent e)
    {
        int i, pocZast;
        int priemerZeme = 12742;
        double kmY = priemerZeme * Math.PI / 360;                  // v smere sever-juh - kolko kilometrov je 1 stupen
        double kmX = (priemerZeme * Math.cos(((Math.PI / 2) * (double) 49 / 90))) * Math.PI / 360; // v smere vychod-zapad, na 49 rovnobezke
        // pozn.: Math.PI/2)*(double)49/90 je vyjadrenie 49 stupnov v radianoch (podla def. PI/2 je 90 stupnov)
        double vzdialKm, minVzdialKm;
        int iMin = -1;
        double gX, gY;

        PixelsToGps(e.getX(), e.getY()); // naplni premenne triedy gpsX, gpsY
        minVzdialKm = 10000;       // dame na zaciatok nejaku velku hodnotu...
        pocZast = jmkGps.getPocetZastavok();
        for (i = 0; i < pocZast; i++)
        {
            gX = jmkGps.getZastX(i);
            gY = jmkGps.getZastY(i);
            vzdialKm = Math.sqrt(Math.pow((gpsX - gX) * kmX, 2) + Math.pow((gpsY - gY) * kmY, 2)); // Pytagorova veta
            if (vzdialKm < minVzdialKm)
            {
                minVzdialKm = vzdialKm;
                iMin = i;
            }
        }
        if (minVzdialKm < 5)
        { // kliklo sa blizko pri niektorej zastavke
            if (radZast1.isSelected())
            {
                cmbZast1.setSelectedItem(jmkGps.getZastNazov(iMin));
            } else
            {
                cmbZast2.setSelectedItem(jmkGps.getZastNazov(iMin));
            }
        }
        labKlik1.setText(jmkGps.getZastNazov(iMin) + ",  vzdial:   " + String.format("%.2f", minVzdialKm));
        labKlik2.setText("Gps: " + String.format("%.4f", gpsX) + "/" + String.format("%.4f", gpsY) + ",   Pix: " + e.getX() + "/" + e.getY());
        repaint();
    }

    private void ClearFormular()
    {
        cmbZast1.setSelectedIndex(0);
        cmbZast2.setSelectedIndex(0);
        spinDosp.setValue(1);
        spinDeti.setValue(0);
//        datetimeSpinner.setValue(new Date()); // nastavi aktualny datum a cas
        datePicker.setDate(new Date());
        timeSpinner.setValue(new Date());
        ClearLabels();
        repaint();
    }

    private void ClearLabels()
    {
        labKlik1.setText(" ");
        labKlik2.setText(" ");
        labVysledek.setText(" ");
    }

    //-----------------------

    private static String padRight(String s, int n)
    { // dna 24.5.2014 http://stackoverflow.com/questions/388461/how-can-i-pad-a-string-in-java
        return String.format("%1$-" + n + "s", s);     // doplni medzery na koniec stringu na celkovu dlzku "n"
    }

    public static String padLeft(String s, int n)
    {
        return String.format("%1$" + n + "s", s);     // doplni medzery na zaciatok stringu na celkovu dlzku "n"
    }

    //-----------------------
    private void Vypocitaj()
    { // vstupy posleme backendu a ten nam vrati vysledky
        int ind1, ind2, pDosp, pDeti;
        String zast1, zast2, casOdchod, casPrichod, typVlaku;
        Date dat;
        SimpleDateFormat sdat;
        Calendar cal = Calendar.getInstance();
        Calendar calPomocny = Calendar.getInstance();
        BigDecimal bd;

        ind1 = cmbZast1.getSelectedIndex() - 1;
        ind2 = cmbZast2.getSelectedIndex() - 1;
        zast1 = cmbZast1.getSelectedItem().toString();
        zast2 = cmbZast2.getSelectedItem().toString();
        pDosp = (Integer) spinDosp.getValue();
        pDeti = (Integer) spinDeti.getValue();
        cal.setTime(datePicker.getDate()); // datum z datePickeru
        calPomocny.setTime((Date) timeSpinner.getValue()); // hodiny a minuty z timeSpinneru
        cal.set(Calendar.HOUR_OF_DAY, calPomocny.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, calPomocny.get(Calendar.MINUTE));

        if ((ind1 >= 0) && (ind2 >= 0) && (ind1 != ind2) && (pDosp + pDeti > 0))
        {
            if (tariffMan != null)
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                modelVysledok.clear();
                modelVysledok.addElement("1. zastavka:     " + zast1);
                modelVysledok.addElement("2. zastavka:     " + zast2);
                modelVysledok.addElement("Pocet dospelych: " + pDosp);
                modelVysledok.addElement("Pocet deti:      " + pDeti);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                modelVysledok.addElement("Datum:           " + sdf.format(cal.getTime()));
                modelVysledok.addElement("");
                modelVysledok.addElement("======================VÝSLEDKY======================");
                Map<Train, Prices> items = tariffMan.computePrices(zast1, zast2, cal, pDosp, pDeti);
                if (items.isEmpty())
                {
                    modelVysledok.addElement("-Spojení nebylo nalezeno-");
                    modelVysledok.addElement("Možné příčiny:");
                    modelVysledok.addElement("1. spojení v příslušném časovém období neexistuje,");
                    modelVysledok.addElement("2. spojení nelze realizovat dostupnymi dopravními prostředky.");
                }
                for (Entry<Train, Prices> item : items.entrySet())
                {
                    CdTrainConnection.TrainStation stat1;
                    modelVysledok.addElement(padRight("-Odkiaľ/Prestup/Kam-", 25) + padRight("-Prích.-", 10) + padRight("-Odch.-", 13) + "-Spoje-");
                    for (CdTrainConnection.TrainStation stat : item.getKey().getStations())
                    {
                        if (stat.getArrivalTime() != null)
                        {
                            casPrichod = stat.getArrivalTime().toString();
                        } else
                        {
                            casPrichod = "";
                        }
                        if (stat.getDepartueTime() != null)
                        {
                            casOdchod = stat.getDepartueTime().toString();
                        } else
                        {
                            casOdchod = "";
                        }
                        if (stat.getTrainId() != null)
                        {
                            typVlaku = stat.getTrainId().toString() + "[" + stat.getLinkId().toString() + "]";
                        } else
                        {
                            typVlaku = "";
                        }
                        modelVysledok.addElement(padRight(stat.getName(), 25) + padRight(casPrichod, 10) + padRight(casOdchod, 13) + typVlaku);
                        modelVysledok.addElement("");
                    }
                    modelVysledok.addElement("----------CENA----------");
                    modelVysledok.addElement("CD cena celkem: " + padLeft(item.getValue().getCdTicket().getTotalGroupPrice().toString(), 5));
                    for (CdTickets.Ticket tic : item.getValue().getCdTicket().getTickets())
                    {
//                        modelVysledok.addElement("Nazev jizdenky: " + tic.getTicketName());
                        modelVysledok.addElement("Dosiahnutý typ zľavy:  " + padLeft(tic.getTicketName().toString(), 5) + "  (" + tic.getTicketsCount() + ")");
                    }
                    modelVysledok.addElement("IDS cena celkem:" + padLeft(item.getValue().getIdsTicket().getPrice().toString(), 5));
                    modelVysledok.addElement("Popis jak ziskat jizdenky: " + item.getValue().getIdsTicket().getDescription());
                    modelVysledok.addElement("====================================================");
                }
                setCursor(Cursor.getDefaultCursor());
                dialogVysledok(); // zobrazi 
            } else
            {
                labVysledek.setText("TariffManager je null");
            }
        } else
        {
            labVysledek.setText("Nespravne alebo neuplne vstupne udaje");
        }
        repaint();
    }

    private void dialogVysledok()
    { // vytvori dialogove okno a zobrazi v nom vysledky 
        final JDialog dialog;
        final JList listVysledok;
        JScrollPane scrollPane;
        GridBagConstraints gbcForm, gbc;
        JButton btnClose_dlg;

        dialog = new JDialog(this, Dialog.ModalityType/*.APPLICATION_MODAL*/.MODELESS);
        dialog.setTitle("Vysledky hladania");
        dialog.setBounds(100, 100, 600, 800);
        dialog.setLayout(new GridBagLayout());

        listVysledok = new JList(); // pre dialog box s vypocitanymi vysledkami
        listVysledok.setFont(new Font("Courier New", Font.BOLD, 12)); //nastavenie pisma
        listVysledok.setModel(modelVysledok); // listVysledok obsahuje zoznam riadkov z premennej triedy "modelVysledok"

        scrollPane = new JScrollPane();
        scrollPane.setViewportView(listVysledok); // listVysledok obsahuje zoznam riadkov z premennej triedy "modelVysledok"

        gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(5, 5, 5, 5);
        gbcForm.weightx = 1;
        gbcForm.weighty = 1;
        gbcForm.fill = GridBagConstraints.BOTH;
        gbcForm.anchor = GridBagConstraints.NORTH;
        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        gbcForm.gridwidth = GridBagConstraints.REMAINDER;
        dialog.getContentPane().add(scrollPane, gbcForm);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        btnClose_dlg = new JButton("OK");
        dialog.getContentPane().add(btnClose_dlg, gbc);
        btnClose_dlg.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    public void PixelsToGps(Integer pX, Integer pY)
    { // gpsX, gpsY su premenne triedy
        gpsX = refgpsX0 + (double) (pX - refX0) / (double) (refX1 - refX0) * (refgpsX1 - refgpsX0);
        gpsY = refgpsY0 + (double) (pY - refY0) / (double) (refY1 - refY0) * (refgpsY1 - refgpsY0);
    }

    public void GpsToPixels(double gX, double gY)
    { // pixX, pixY su premenne triedy
        pixX = refX0 + (int) ((gX - refgpsX0) / (refgpsX1 - refgpsX0) * (refX1 - refX0));
        pixY = refY0 + (int) ((gY - refgpsY0) / (refgpsY1 - refgpsY0) * (refY1 - refY0));
    }

    public static void main(String[] args)
    {
        IdsJmk app = new IdsJmk();
        app.setTitle("Porovnanie ceny IDS JMK a Ceskych Drah");
        app.setPreferredSize(new Dimension(800, 600));
        app.addComponentListener(app); // aby reagoval na componentMoved(...) {invalidate(); validate();} definovane na zaciatku programu
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // stlacenie "x" v pravom hornom rohu ukonci aplikaciu
        app.pack(); // nastavenie uvodnej velkosti okna podla jeho component (podla lavyPanel, pravyPanel, GridBagLayout)
        app.setLocationRelativeTo(null); // kde sa ma okno na obrazovke zobrazit - null znamena v strede
        app.setVisible(true);
    }
}
