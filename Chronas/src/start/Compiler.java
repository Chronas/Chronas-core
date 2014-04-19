package start;

import java.io.*;

public class Compiler 
{
	//Pfad zum Dokument und das Dokument
	File pfad;
	ChrDokument dokument;
	
	//Übersetzter Code
	String[] javacode;
	
	//Gibt an, ob die Klasse noch nicht angefangen hat und import Anweisungen erlaubt sind
	boolean startDerKlasse = true;
	
	
	
	
	/*
	 * Konstruktor
	 */
	public Compiler(ChrDokument dokument, File pfad)
	{
		this.dokument = dokument;
		this.pfad = pfad;
		
		javacode = new String[dokument.getLength()];
		
		//Compilieren und javadatei estellen
		compilieren();
		speichern();
	}
	
	
	
	
	
	/*
	 * Übersetzen des Codes in Javacode
	 */
	public void compilieren()
	{
		//Variable, in der die Stärke der Einrückung der Befehle gespeichert ist
		int limitation = 0;
		
		//Forschleife, die alle Zeilen des chr-Dokuments durchgeht und übersetzt
		for (int i = 0; i < dokument.getLength(); i++)
		{	
			//Den Befehl in seine einzelnen Bestandteile aufsplitten
			String[] words = dokument.getText(i).trim().split("\\s+");
			
			//Den Text des Javacodes von "null" auf "" setzen
			javacode[i] = "";
						
			//Leerzeichen am Anfang der Zeilen um den generierten Code übersichtlicher zu gestalten
			for (int j = 0; j < limitation; j++) 
			{
				javacode[i] += "   ";
			}	
			

			
			/*
			 * Befehle prüfen und übersetzen
			 */
			try//Wenn ein Fehler auftritt wird in den catch-Block gewechselt, wo eine Fehlermeldung in den Javacode geschrieben wird.
			{
				switch(words[0].replace(":", "").toLowerCase())
				{
					//Methoden, Klassen & Interfaces
					case "class" : klasse(i); startDerKlasse = false; limitation++;  break;
					case "interface" : schnittstelle(i); limitation++;  break;
					case "method": methode(i); limitation++; break;
					case "run"   : runMethode(i); break;		//Methode ausführen
					case "import": importClasses(i); break; 
				
					
					/*
					 * Kontrollstrukturen
					 */
					case "for": forSchleife(i); limitation++; break;
					case "if":  ifBedingung(i); limitation++; break;
					case "}":   beendeContainer(i, limitation); limitation--; break;
					
					
					/*
					 * Variablen und Objekte
					 */
					case "var": variable(i, dokument.getText(i)); break;
					case "new": newObjekt(i, dokument.getText(i)); break;
					
					//try, catch & finally
					case "try": tryBlock(i); limitation++; break;
					case "catch": catchBlock(i, limitation); break;
					case "finally": finallyBlock(i, limitation); break;
					
					/*
					 * Javacode & Kommentar
					 */
					case "java": for (int j = 1; j < words.length; j++)   //Javacode
									javacode[i] += words[j] + " "; break;
					default: if(!dokument.getText(i).trim().equals("") && dokument.getText(i).trim() != null)
								javacode[i] += "//" + dokument.getText(i).trim(); //Kommentar	
				}
			}
			catch(Error e)
			{
				System.out.println("Fehler");
				javacode[i] += "Fehler in Zeile " + (i+1) + ": Es existiert ein unbekannter Fehler!";
			}
		}
	}
	
	
	
	
	/*
	 * try Block
	 * try: Stream, Stream
	 */
	public void tryBlock(int i) 
	{
		String[] words = dokument.getText(i).trim().split(":");
		
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		if(words[0].trim().split("\\s+").length > 1)
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Hinter dem try stehen unbekannte Zeichen!";
			return;
		}
		
		
		/*
		 * Übersetzung in Javacode
		 */
		javacode[i] += "try";
		
        
        //Streams, die geöffnet werden sollen in Klammern schreiben
		if(dokument.getText(i).contains(":"))
		{
			words = words[1].trim().split(",");
			
			javacode[i] += "(";
			if(words.length > 1)
			for (int j = 0; j < words.length-1; j++) 
			{
				variable(i, words[j]);
				javacode[i] += ", ";
			}
			variable(i, words[words.length-1]);
			
			javacode[i] += ")";
		}
		
		javacode[i] += "{";
	}
	
	
	
	
	/*
	 * catch Block
	 * catch: Error
	 */
	public void catchBlock(int i, int limitation)
	{
		String[] words = dokument.getText(i).trim().split(":");
		
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		if(words[0].trim().split("\\s+").length > 1)
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Hinter dem catch stehen unbekannte Zeichen!";
			return;
		}
		else if(words.length == 1)
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Es wurde keine Exception angegeben!";
			return;
		}
		
		//Einrückung anpassen
		javacode[i] = "";
		
		for (int j = 0; j < limitation-1; j++) 
		{
			javacode[i] += "   ";
		}
		
		
		/*
		 * Übersetzung in Javacode
		 */		
		words = words[1].trim().split(",");

		javacode[i] += "}catch(";
		
		//Multicatching
		if(words.length > 1)
			for (int j = 0; j < words.length-1; j++) 
			{
				javacode[i] += words[j].trim() + " | ";
			}
		
		javacode[i] += words[words.length -1].trim();
		javacode[i] += " e" + "){";		
	}
	
	
	
	
	/*
	 * finally Block
	 * finally
	 */
	public void finallyBlock(int i, int limitation) 
	{
		String[] words = dokument.getText(i).trim().split("\\s+");
		
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		if(words.length > 1)
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Hinter finally stehen unbekannte Zeichen!";
			return;
		}
		
		
		/*
		 * Übersetzung in Javacode
		 */
		//Einrückung anpassen
		javacode[i] = "";
		
		for (int j = 0; j < limitation-1; j++) 
		{
			javacode[i] += "   ";
		}
		javacode[i] += "}"; 
		//Javacode
		javacode[i] += "finally{";
	}




	/*
	 * Klasse erstellen:
	 * class name <modifizierer> extends Test, Test1
	 */
	public void klasse(int i) //i=Zeile
	{		
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		String[] words = dokument.getText(i).trim().split("<");
		int length = words[0].split("\\s+").length;	//Anzahl der Wörter vor den Modifizierern
		if(length > 2) 
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Zwischen dem Namen und dem Diamantoperator stehen unbekannte Zeichen!";
			return;
		}
		else if(length == 1) 
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Der Klassenname fehlt!";
			return;
		}
		
		//Grammatik innerhalb des Diamantoperators
		if(words.length == 2)
		{
			words = words[1].split(">"); 
			if(words.length != 1  && !dokument.getText(i).contains("extends")) 
			{
				javacode[i] += "Fehler in Zeile " + (i+1) + ": Nach dem Diamantoperator darf kein weiteres Zeichen mehr kommen!";
				return;
			}
		}
		
		//Modifizierer wie public, private, final, static
		words = dokument.getText(i).trim().split("<");
		if(words.length == 2)
		{
			words = words[1].trim().split(">");
			String   modifizierer = words[0].trim().replace(",", " ");
			javacode[i] += modifizierer + " ";
		}
		
		//Klassenname 
		words = dokument.getText(i).trim().split("\\s+");
		words = words[1].trim().split("<");
		javacode[i] += "class " + words[0];
		
		//Vererbung
		if(dokument.getText(i).contains("extends"))
		{
			//Klassen rausfiltern
			words = dokument.getText(i).trim().split("extends");
			words = words[1].trim().split(",");
			
			//Vererbung
			if(!words[0].trim().equals(""))
			{
				javacode[i] += " extends " + words[0].trim();
			}
			
			//Interfaces
			if(words.length > 1)
			{
				javacode[i] += " implements ";
				if(words.length > 2)
				{
					for (int j = 1; j < words.length-1; j++) 
					{
						javacode[i] += words[j].trim() + ", ";
					}
				}
				javacode[i] += words[words.length-1].trim();
			}
			
			//Klassenbeginn
			javacode[i] += "{";
		}
	}
	
	
	
	
	/*
	 * Schnittstelle erstellen:
	 * interface name <modifizierer> extends Test, Test1
	 */
	public void schnittstelle(int i) //i=Zeile
	{		
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		String[] words = dokument.getText(i).trim().split("<");
		int length = words[0].split("\\s+").length;	//Anzahl der Wörter vor den Modifizierern
		if(length > 2) 
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Zwischen dem Namen und dem Diamantoperator stehen unbekannte Zeichen!";
			return;
		}
		else if(length == 1) 
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Der Schnittstellenname fehlt!";
			return;
		}
		words = words[1].trim().split(",");
		length = words.length;
		if(length > 1) 
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Ein Interface darf nur von einer Klasse oder einem Interface erben!";
			return;
		}
		
		//Grammatik innerhalb des Diamantoperators
		if(words.length == 2)
		{
			words = words[1].split(">"); 
			if(words.length != 1  && !dokument.getText(i).contains("extends")) 
			{
				javacode[i] += "Fehler in Zeile " + (i+1) + ": Nach dem Diamantoperator darf kein weiteres Zeichen mehr kommen!";
				return;
			}
		}
		
		//Modifizierer wie public, private, final, static
		words = dokument.getText(i).trim().split("<");
		if(words.length == 2)
		{
			words = words[1].trim().split(">");
			String   modifizierer = words[0].trim().replace(",", " ");
			javacode[i] += modifizierer + " ";
		}
		
		//Interfacename 
		words = dokument.getText(i).trim().split("\\s+");
		words = words[1].trim().split("<");
		javacode[i] += "interface " + words[0];
		
		//Vererbung
		if(dokument.getText(i).contains("extends"))
		{
			//Klassen rausfiltern
			words = dokument.getText(i).trim().split("extends");
			words = words[1].trim().split(",");
			
			//Vererbung
			if(!words[0].trim().equals(""))
			{
				javacode[i] += " extends " + words[0].trim();
			}
			
			//Schnittstellenbeginn
			javacode[i] += "{";
		}
	}
	
	
	
	
	/*
	 * Objekteinstanzen erstellen
	 * new Klassenname: Argumente
	 */
	public void newObjekt(int i, String zeichenkette) //i=Zeile
	{		
		String[] words = zeichenkette.trim().split(":");
		
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		if(words[0].split("\\s+").length > 2)
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Hinter dem Klassennamen stehen unbekannte Zeichen!";
			return;
		}
		else if(words[0].split("\\s+").length < 2)
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Der Klassenname fehlt!";
			return;
		}
		
		
		/*
		 * Übersetzung in Javacode
		 */
		javacode[i] += "new ";
		
		//Klassenname
		words = words[0].trim().split("\\s+");
		javacode[i] += words[1];
		
        
        //Argumente
		javacode[i] += "(";
		if(zeichenkette.contains(":"))
		{
		    words = zeichenkette.trim().split(":");
	        javacode[i] += words[1];
		}
		javacode[i] += ");";
	}
	

	
	
	/*
	 * Mehtode erstellen:
	 * method rückgabetyp name <modifizierer>: parameter
	 */
	public void methode(int i) //i=Zeile
	{		
		String[] words;
		
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		//Die ersten Wörter vor den Argumenten & Modifizierern herausfiltern
		if(dokument.getText(i).contains("<") && dokument.getText(i).contains(">"))
			words = dokument.getText(i).trim().split("<");
		else if(dokument.getText(i).contains(":"))
			words = dokument.getText(i).trim().split(":");
		else
			words = dokument.getText(i).trim().split("\\s+");
			
		//Anzahl der Wörter vor den Modifizierern & Argumenten überprüfen
		int length = words[0].split("\\s+").length;	
		if(length != 3) 
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Zwischen dem Namen und dem Diamantoperator stehen unbekannte Zeichen!";
			return;
		}
		
		//Modifizierer wie public, private, final, static
		words = dokument.getText(i).trim().split("<");
		if(words.length == 2)
		{
			words = words[1].trim().split(">");
			String   modifizierer = words[0].trim().replace(",", " ");
			javacode[i] += modifizierer + " ";
		}
		
		//Rückgabetyp
		words = dokument.getText(i).trim().split("\\s+");
		javacode[i] += words[1] + " ";
		
		//Name 
		words = words[2].trim().split("<");
		javacode[i] += words[0].replace(":", "") + "(";
        
        //Argumente
	    words = dokument.getText(i).trim().split(":");
        javacode[i] += words[1] + "){";
	}
	
	
	
	
	/*
	 * Methode ausführen
	 * Die Argumente sind hinter dem Doppelpunkt und werden durch Kommas getrennt
	 */
	public void runMethode(int i) //i=Zeile
	{		
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		String[] words = dokument.getText(i).trim().split(":");
		int length = words[0].split("\\s+").length;	
		if(length != 2) 
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Nach dem Methodennamen stehen unbekannte Zeichen!";
			return;
		}
		else if(length == 1) 
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Der Methodenname fehlt!";
			return;
		}
		
		//String in einzelne Bestandteile aufteilen und den Doppelpunkt entfernen
		words = dokument.getText(i).replace("run", "").trim().replace(':', ' ').replace(',', ' ').split("\\s+");;
		
		
		/*
		 * Den Code übersetzen
		 */
		javacode[i] += words[0] + "(";
		words = dokument.getText(i).trim().split(":");
		
		//Argumente dem Javacode hinzufügen
		if(dokument.getText(i).contains(":"))
		{
			javacode[i] += words[1].trim();
		}
		javacode[i] += ");";
	}
	
	
	
	
	/*
	 * Import Anweisung 
	 * import importanweisung
	 */
	public void importClasses(int i) 
	{
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		String[] words = dokument.getText(i).trim().split("\\s+");
		if(words.length > 2) 
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Nach dem Pfad stehen unbekannte Zeichen!";
			return;
		}
		else if(words.length == 1)
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Die Pfadangabe fehlt";
			return;
		}
		
		String pfad = dokument.getText(i).replace("import", "").trim();

		//Javacode wird nur als Import gewertet, wenn die Klasse noch nicht begonnen hat
		//Ansonsten wird es als Kommentar genommen.
		if(startDerKlasse)
			javacode[i] += "import " + pfad + ";";
		else
			javacode[i] += "//" + dokument.getText(i).trim();
	}

	
	
	
	/*
	 * Variable erstellen:
	 * var variablentyp name
	 */
	public void variable(int i, String zeichenkette) 
	{
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		String[] words;
		zeichenkette = zeichenkette.replace("var", "");
		
		if(zeichenkette.contains("<") && zeichenkette.contains(">"))
		{
			words = zeichenkette.trim().split("<");
			words = words[0].trim().split("\\s+");
		}
		else if(zeichenkette.contains("="))
		{
			words = zeichenkette.trim().split("=");
			words = words[0].trim().split("\\s+");
		}
		else
			words = zeichenkette.trim().split("\\s+");
		
		if(words.length > 2) 
		{
			System.out.println(words[0]);
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Nach dem Namen kommen unbekannte Zeichen!";
			return;
		}
		else if(words.length < 2)
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Der Name, der Rückgabetyp oder beides fehlt!";
			return;
		}
		
		//Modifizierer wie public, private, final, static
		words = zeichenkette.trim().split("<");
		if(words.length == 2)
		{
			words = words[1].trim().split(">");
			String modifizierer = words[0].trim().replace(",", " ");
			javacode[i] += modifizierer + " ";
		}
		
		//Variable erstellen
		if(zeichenkette.contains("<") && zeichenkette.contains(">"))
			words = zeichenkette.trim().split("<");
		else 
			words = zeichenkette.trim().split("=");
		words = words[0].trim().split("\\s+");
		javacode[i] += words[0] + " " + words[1];
		
		//Wird nur ausgeführt, wenn die Variable eine Zuwesiung besitzt
		if(zeichenkette.contains("="))
		{
			String[] wert = zeichenkette.trim().split("=");
			
			javacode[i] += " = ";
					
			//Objektinstanz oder Variablenwert erstellen
			if(wert[1].contains("new"))
				newObjekt(i, wert[1]);
			else
			{
				words = wert[0].split("\\s+");
				javacode[i] += wert[1].trim();
			}
		}
	}



	/* 
	 * for-Schleife mit der Struktur:
	 * for variablenname=startwert to endwert "step schrittgröße"
	 * übersetzt
	 */
	public void forSchleife(int i) //i=Zeile
	{	
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		String[] words = dokument.getText(i).trim().replace("=", " ").split("\\s+");
		
		if(words.length > 5) 
		{
			if(words.length != 7 || !words[5].equals("step"))
			{
				javacode[i] += "Fehler in Zeile " + (i+1) + ": Am Ende des Befehls stehen unbekannte Zeichen!";
				return;
			}
		}
		else if(words.length < 5)
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Der Befehl ist unvollständig!";
			return;
		}
		else if(words[3].equals("to"))
		{
			javacode[i] += "Fehler in Zeile " + (i+1) + ": Der Teilbefehl 'to' fehlt!";
			return;
		}
		
		 //Variable deklarieren
		javacode[i] += "for(int " + words[1] + "=" + words[2] + "; ";   
	
		//Schrittzahl ist standartmäßig 1, kann allerdings geändert werden
		int step = 1;
		if(words.length == 7 && dokument.getText(i).contains("step"))
			step = Integer.parseInt(words[6]);
		
		//Ist die erste Zahl größer als die Zweite, wird das erste ausgeführt, 
		//ansonsten das Zweite
		if(Integer.parseInt(words[2]) > Integer.parseInt(words[4]))
		{
			javacode[i] += words[1] + ">" + words[4] + "; " + words[1] + "-=" + step + "){";
		}
		else
		{
			javacode[i] += words[1] + "<" + words[4] + "; " + words[1] + "+=" + step + "){";
		}
	}
	
	
	
	
	
	/*
	 * If Bedingung erstellen:
	 * if bedingung 
	 */
	public void ifBedingung(int i) 
	{
		//Die Bedingung aus der Eingabe rausfiltern
		String bedingung = dokument.getText(i).replace("if", "").trim();
		
		//Die Bedingung dem Javacode hinzufügen
		javacode[i] += "if(" + bedingung + "){";
	}
	
	
	
	
	/*
	 *Kontainer beenden und die Einrückung(limitation)
	 *korrigieren, da sonst alle geschweiften Schlussklammern 
	 *zu weit hinten stehen
	 */
	public void beendeContainer(int i, int limitation) //i=Zeile
	{		
		javacode[i] = "";
		
		for (int j = 0; j < limitation-1; j++) 
		{
			javacode[i] += "   ";
		}
		javacode[i] += "}"; 
		
		//Falls vorhanden, Kommentar hinter die Klammer setzen
		String words = dokument.getText(i).replace("}", "").trim();
		
		if(!words.equals(""))
			javacode[i] += " //" + words;
	}
	
	
	
	/*
	 * Abspeichern des Javacodes als .java Datei
	 */
	public void speichern()
	{
		//Neuen Pfad erstellen
		String neuPfad = pfad.getAbsolutePath();
		String epfad = neuPfad.replace("chr", "java");
		File datei = new File(epfad);
		
	    
	    //in dem Javadokument den Code speichern
	    try
	    { 
            PrintWriter pWriter = new PrintWriter(new FileWriter(datei)); 
            for (int i = 0; i < javacode.length; i++) 
            {
				pWriter.println(javacode[i]);
	            pWriter.flush(); 
			}
            pWriter.close();
        }
	    catch(IOException ioe)
	    { 
            ioe.printStackTrace(); 
        } 
	}
}
