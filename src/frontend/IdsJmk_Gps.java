package frontend;

import java.util.ArrayList;
import java.util.*;
import backend.*;
import backend.entity.*;
import java.text.*;
import java.util.Comparator;

class typeZastavka {	
	public String ZastNazov;
	public Double ZastX; // GPS suradnice v stupnoch, napr. 49.342143
	public Double ZastY;
	
	public typeZastavka(String Naz, int Xstup, int Xmin, int Xsec, int Ystup, int Ymin, int Ysec) {
		ZastNazov = Naz;
		ZastX = (double)Xstup + (double)Xmin/60 + (double)Xsec/3600; // prevod stupnov,minut,sekund na jedno double cislo
		ZastY = (double)Ystup + (double)Ymin/60 + (double)Ysec/3600;
	}
}

class CzechComparator implements Comparator<Station> { // http://www.java.cz/article/ceskerazeni
    private static String rules =
            "< ' ' < A,a;Á,á;À,à;Â,â;Ä,ä;Ą,ą < B,b < C,c;Ç,ç < Č,č < D,d;Ď,ď < E,e;É,é;È,è;Ê,ê;Ě,ě" +
            "< F,f < G,g < H,h < CH,Ch,cH,ch < I,i;Í,í < J,j < K,k < L,l;Ľ,ľ;Ł,ł < M,m < N,n;Ň,ň" +
            "< O,o;Ó,ó;Ô,ô;Ö,ö < P,p < Q,q < R,r;Ŕ,ŕ < Ř,ř < S,s < Š,š < T,t;Ť,ť" +
            "< U,u;Ú,ú;Ů,ů;Ü,ü < V,v < W,w < X,x < Y,y;Ý,ý < Z,z;Ż,ż < Ž,ž" +
            "< 0 < 1 < 2 < 3 < 4 < 5 < 6 < 7 < 8 < 9" +
            "< '.' < ',' < ';' < '?' < '¿' < '!' < '¡' < ':' < '\"' < '\'' < '«' < '»'" +
            "< '-' < '|' < '/' < '\\' < '(' < ')' < '[' < ']' < '<' < '>' < '{' < '}'" +
            "< '&' < '¢' < '£' < '¤' < '¥' < '§' < '©' < '®' < '%' < '‰' < '$'" +
            "< '=' < '+' < '×' < '*' < '÷' < '~'";
    private RuleBasedCollator comparator = new RuleBasedCollator(rules);

    public CzechComparator() throws ParseException {
    }

    public int compare(Station s1, Station s2) {
        return comparator.compare(s1.getName(), s2.getName());
    }
}

public class IdsJmk_Gps {
	private final ArrayList<typeZastavka> ZastavkyArr;
    private ArrayList<Station> ZastavkyBackEnd; // zoznam zastavok z backendu (z getAllStations() )

    public IdsJmk_Gps() { // constructor
        int  pocZastBackEnd, i;

        ZastavkyArr     = new ArrayList();
        ZastavkyBackEnd = new ArrayList();
        PathManager pm = null;
        Set<Station> pmZastSet;
        Station[]    pmZastArr;
        try {
            pm = new PathManager();
        } catch (java.net.URISyntaxException e) {}
        
        if (pm != null) {
            pmZastSet = pm.getAllStations();
            pmZastArr = pmZastSet.toArray(new Station[pmZastSet.size()]);
            pocZastBackEnd = pmZastArr.length;
            for (i = 0; i < pocZastBackEnd; i++) {
                ZastavkyBackEnd.add(pmZastArr[i]);
            }
			
			try {
				Collections.sort(ZastavkyBackEnd, new CzechComparator());
			} catch(ParseException e) {}
			
            for (i = 0; i < pocZastBackEnd; i++) {
                ZastavkyArr.add(new typeZastavka(ZastavkyBackEnd.get(i).getName(),
                                                 ZastavkyBackEnd.get(i).getLongitudeDegree(), 
                                                 ZastavkyBackEnd.get(i).getLongitudeMinute(), 
                                                 ZastavkyBackEnd.get(i).getLongitudeSecond(), 
                                                 ZastavkyBackEnd.get(i).getLatitudeDegree(), 
                                                 ZastavkyBackEnd.get(i).getLatitudeMinute(), 
                                                 ZastavkyBackEnd.get(i).getLatitudeSecond()));
            }
        }
		
    }
	
	public int    getPocetZastavok() { return ZastavkyArr.size(); }	
	public String getZastNazov(int i) { return ZastavkyArr.get(i).ZastNazov; }
	public double getZastX(int i) { return ZastavkyArr.get(i).ZastX; }
	public double getZastY(int i) { return ZastavkyArr.get(i).ZastY; }
}
