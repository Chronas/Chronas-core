package start;

import java.io.*;
import java.util.ArrayList;

public class Compiler 
{
	//Pfad zum Dokument und das Dokument
	File pfad;
	ChrDokument dokument;
	
	//Übersetzter Code
	String[] javacode;
	
	//Gibt an, ob die Klasse noch nicht angefangen hat und import Anweisungen erlaubt sind
	boolean startDerKlasse = true;
	
	//Fehlermeldungen
	ArrayList<String> fehler = new ArrayList();
	
	
	
	/*
	 * Konstruktor
	 */
	public Compiler(ChrDokument dokument, File pfad) throws Exception
	{
		Anzeige.konsole.setText("");
		
		this.dokument = dokument;
		this.pfad = pfad;
		
		javacode = new String[dokument.getLength()];
		
		//Compilieren und javadatei estellen
		compilieren();
		
		if(fehler.size() == 0)
		{
			speichern();
		}
		else
		{
			fehlerAusgabe();
			throw new Exception();
		}
	}
	
	
	
	
	
	/*
	 * Übersetzen des Codes in Javacode
	 */
	public void compilieren() throws Exception
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
			
			//Auf Annotation testen und dann word[0] auf die switch-Anweisung vorbereiten
			if(dokument.getText(i).contains("@") && dokument.getText(i).trim().charAt(0)== '@')
				words[0] = "@";

			
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
					case "run"   : runMethode(i, dokument.getText(i)); break;		//Methode ausführen
					case "return": returnMethode(i); break;							//Methode verlassen
					case "import": importClasses(i); break; 
				
					
					/*
					 * Kontrollstrukturen
					 */
					case "for": forSchleife(i); limitation++; break;
					case "while": whileSchleife(i); limitation++; break;
					case "if":  ifBedingung(i); limitation++; break;
					case "else":elseBedingung(i, limitation); break;
					case "}":   beendeContainer(i, limitation); limitation--; break;
					
					//try, catch & finally
					case "try": tryBlock(i); limitation++; break;
					case "catch": catchBlock(i, limitation); break;
					case "finally": finallyBlock(i, limitation); break;
					
					/*
					 * Variablen und Objekte
					 */
					case "var": variable(i, dokument.getText(i)); break;
					case "assign": variableZuweisung(i); break;				//einer Variable einen Wert zuweisen
					case "new": newObjekt(i, dokument.getText(i)); break;
					
					/*
					 * Annotationen
					 */
					case "@": annotation(i); break;

					
					/*
					 * Vereinfachte Methoden
					 */
					//Konsolenausgabemethoden
					case "print":   javacode[i] += "System.out.print(" + dokument.getText(i).replace("print", "").replace(":", "").trim() + ");"; break;
					case "println": javacode[i] += "System.out.println(" + dokument.getText(i).replace("println", "").replace(":", "").trim() + ");"; break;
					case "printf":  javacode[i] += "System.out.printf(" + dokument.getText(i).replace("printf", "").replace(":", "").trim() + ");"; break;


					
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
				fehler.add("Fehler in Zeile " + (i+1) + ": Es existiert ein unbekannter Fehler!\n");
			}
		}
	}
	
	
	
	
	/*
	 * try Block
	 * try: Stream, Stream
	 */
	public void tryBlock(int i) throws Exception 
	{
		String[] words = dokument.getText(i).trim().split(":");
		
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		if(words[0].trim().split("\\s+").length > 1)
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Hinter dem try stehen unbekannte Zeichen!\n");
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
			fehler.add("Fehler in Zeile " + (i+1) + ": Hinter dem catch stehen unbekannte Zeichen!\n");
			return;
		}
		else if(words.length == 1)
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Es wurde keine Exception angegeben!\n");
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
			fehler.add("Fehler in Zeile " + (i+1) + ": Hinter finally stehen unbekannte Zeichen!\n");
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
		try
		{
			/*
			 * Grammatiküberprüfung und Fehlerausgabe
			 */
			String[] words = dokument.getText(i).trim().split("<");
			int length = words[0].split("\\s+").length;	//Anzahl der Wörter vor den Modifizierern
			if(length > 2) 
			{
				fehler.add("Fehler in Zeile " + (i+1) + ": Zwischen dem Namen und dem Diamantoperator stehen unbekannte Zeichen!\n");
				return;
			}
			else if(length == 1) 
			{
				fehler.add("Fehler in Zeile " + (i+1) + ": Der Klassenname fehlt!\n");
				return;
			}
			
			//Grammatik innerhalb des Diamantoperators
			if(words.length == 2)
			{
				words = words[1].split(">"); 
				if(words.length != 1  && !dokument.getText(i).contains("extends")) 
				{
					fehler.add("Fehler in Zeile " + (i+1) + ": Nach dem Diamantoperator darf kein weiteres Zeichen mehr kommen!\n");
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
			}
			
			//Klassenbeginn
			javacode[i] += "{";
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Es trat ein unbekannter Fehler auf!\n");
		}
	}
	
	
	
	
	/*
	 * Schnittstelle erstellen:
	 * interface name <modifizierer> extends Test, Test1
	 */
	public void schnittstelle(int i) //i=Zeile
	{		
		try
		{
			/*
			 * Grammatiküberprüfung und Fehlerausgabe
			 */
			String[] words = dokument.getText(i).trim().split("<");
			int length = words[0].split("\\s+").length;	//Anzahl der Wörter vor den Modifizierern
			if(length > 2) 
			{
				fehler.add("Fehler in Zeile " + (i+1) + ": Zwischen dem Namen und dem Diamantoperator stehen unbekannte Zeichen!\n");
				return;
			}
			else if(length == 1) 
			{
				fehler.add("Fehler in Zeile " + (i+1) + ": Der Schnittstellenname fehlt!\n");
				return;
			}
			words = words[1].trim().split(",");
			length = words.length;
			if(length > 1) 
			{
				fehler.add("Fehler in Zeile " + (i+1) + ": Ein Interface darf nur von einer Klasse oder einem Interface erben!\n");
				return;
			}
			
			//Grammatik innerhalb des Diamantoperators
			if(words.length == 2)
			{
				words = words[1].split(">"); 
				if(words.length != 1  && !dokument.getText(i).contains("extends")) 
				{
					fehler.add("Fehler in Zeile " + (i+1) + ": Nach dem Diamantoperator darf kein weiteres Zeichen mehr kommen!\n");
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
		catch(ArrayIndexOutOfBoundsException e)
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Es trat ein unbekannter Fehler auf!\n");
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
			fehler.add("Fehler in Zeile " + (i+1) + ": Hinter dem Klassennamen stehen unbekannte Zeichen!\n");
			return;
		}
		else if(words[0].split("\\s+").length < 2)
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Der Klassenname fehlt!\n");
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
	        javacode[i] += words[1].trim();
		}
		javacode[i] += ");";
	}
	

	
	
	/*
	 * Mehtode erstellen:
	 * method rückgabetyp name <modifizierer>: parameter
	 */
	public void methode(int i) //i=Zeile
	{	
		try
		{
			String[] words;
			
			/*
			 * Grammatiküberprüfung und Fehlerausgabe
			 */
			//Die ersten Wörter vor den Argumenten & Modifizierern herausfiltern
			words = dokument.getText(i).trim().split(":");
			words = words[0].trim().split("throws");
			words = words[0].trim().split("<");
			words = words[0].trim().split("\\s+");
			
			//Namen und Rückgabetyp testen
			if(words.length < 3)
			{
				fehler.add("Fehler in Zeile " + (i+1) + ": Der Rückgabetyp oder der Name fehlen!\n");
				return;
			}
			else if(words.length > 3)
			{
				fehler.add("Fehler in Zeile " + (i+1) + ": Es existieren unbekannte Zeichen im Befehl!\n");
				return;
			}		
			
			
			//Throwsteilanweisung testen
			words = dokument.getText(i).trim().split(":");
			if(dokument.getText(i).contains("throws"))
			{
				words = words[0].trim().split("throws");
				words = words[1].trim().split("\\s+");
				if(words[0].trim().equals(""))
				{
					Anzeige.konsole.append("Fehler in Zeile " + (i+1) + ": In der Throwanweisung wurde kein Error genannt!\n");
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
			
			//Rückgabetyp
			words = dokument.getText(i).trim().split("\\s+");
			javacode[i] += words[1].trim() + " ";
			
			//Name wird nur eingetragen, wenn der Name ungleich "constructor" ist
			words = words[2].trim().split("<");
			if(!words[0].trim().equals("constructor"))
				javacode[i] += words[0].trim().replace(":", "").trim();
				
	        
	        //Argumente
		    words = dokument.getText(i).trim().split(":");
		    if(words.length > 1)
		    	javacode[i] += "(" + words[1].trim() + ")";
		    
		    //Throws Klausel
		    if(dokument.getText(i).contains("throws"))
		    {
		    	words = dokument.getText(i).split("throws");
		    	words = words[1].split(":");
		    	javacode[i] += " throws " + words[0].replace("throws", "").trim();
		    }
		    
		    javacode[i] += "{";
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Ein unbekannter Fehler ist aufgetaucht!\n");
		}
	}
	
	
	
	
	/*
	 * Methode ausführen
	 * Die Argumente sind hinter dem Doppelpunkt und werden durch Kommas getrennt
	 */
	public void runMethode(int i, String zeichenkette) throws Exception //i=Zeile
	{		
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		String[] words = zeichenkette.trim().split(":");
		int length = words[0].trim().split("\\s+").length;	
		if(length != 2) 
		{
			System.out.println(length);
			fehler.add("Fehler in Zeile " + (i+1) + ": Nach dem Methodenpfad stehen unbekannte Zeichen!\n");
			return;
		}
		else if(length == 1) 
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Der Methodenpfad fehlt!\n");
			return;
		}
		
		/*
		 * Die neu geladene Klasse ebenfalls übersetzen, falls es sich dabei um eine .chr Datei handelt.
		 */
		//Den Namen der Klasse in die Variable klasse herausfiltern
		words = zeichenkette.trim().split(":");
		words = words[0].trim().split("\\s+");		
		words = words[1].trim().split("\\.");	
		String klasse = words[0].trim();
		
		//Neuen Pfad generieren und testen, ob er existiert
		String[] pfadteile = pfad.getAbsolutePath().split("\\\\");
		pfadteile[pfadteile.length-1] = klasse + ".chr";
		
		String neupfad = "";
		for (int j = 0; j < pfadteile.length-1; j++) {
			neupfad += pfadteile[j] + "\\";
		}
		neupfad += pfadteile[pfadteile.length-1];
		
		/*
		 * Wenn der Pfad existiert, wird ein neuer Compiler generiert.
		 * Tritt ein Fehler auf, wird keine der Dateien gespeichert.
		 */
		File file = new File(neupfad);
		if(file.exists())
		{
			try
			{
				//rekursiv wird ein nuer Compieler erzeugt. 
				new Compiler(new ChrDokument(file.getAbsoluteFile()), file);
			}
			catch(Exception e)
			{	
				//Fehlerausgabe
				if(fehler.size() > 0)
				{
					fehlerAusgabe();
				}
				
				//Neue Exception, damit keine Dateien gespeichert werden
				throw new Exception();
			}
		}
		
		
		
		//String in einzelne Bestandteile aufteilen und den Doppelpunkt entfernen
		words = zeichenkette.replace("run", "").trim().replace(':', ' ').replace(',', ' ').split("\\s+");
		
		
		/*
		 * Den Code übersetzen
		 */
		javacode[i] += words[0] + "(";
		words = zeichenkette.trim().split(":");
		
		//Argumente dem Javacode hinzufügen
		if(zeichenkette.contains(":"))
		{
			javacode[i] += words[1].trim();
		}
		javacode[i] += ");";
	}
	
	
	
	
	/*
	 * Methode verlassen:
	 * return Rückgabewert
	 */
	public void returnMethode(int i) //i=Zeile
	{			
		//String in einzelne Bestandteile aufteilen und den Doppelpunkt entfernen
		String words = dokument.getText(i).replace("return", "").trim();
		
		
		/*
		 * Den Code übersetzen
		 */
		javacode[i] += "return " + words + ";";
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
			fehler.add("Fehler in Zeile " + (i+1) + ": Nach dem Pfad stehen unbekannte Zeichen!\n");
			return;
		}
		else if(words.length == 1)
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Die Pfadangabe fehlt\n");
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
	public void variable(int i, String zeichenkette) throws Exception 
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
			fehler.add("Fehler in Zeile " + (i+1) + ": Nach dem Namen kommen unbekannte Zeichen!\n");
			return;
		}
		else if(words.length < 2)
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Der Name, der Rückgabetyp oder beides fehlt!\n");
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
					
			//Wenn ein Methodenaufruf existiert
			if(wert[1].contains("run"))
			{
				words = dokument.getText(i).trim().split("=");
				runMethode(i, words[1]);
			}
			//Wenn ein neues Objekt erzeugt wird
			else if(wert[1].contains("new"))
			{
				words = dokument.getText(i).trim().split("=");
				newObjekt(i, words[1]);
			}
			else //Primitive Wertzuweisung
			{
				words = dokument.getText(i).trim().split("=");
				javacode[i] += words[1].trim() + ";";
			}		
			return;
		}
		javacode[i] += ";";
	}
	
	
	
	
	/*
	 * Variable einen Wert zuweisen
	 * assign variablenname = wertoderMethodenaufruf
	 */
	public void variableZuweisung(int i) throws Exception 
	{
		/*
		 * Grammatiküberprüfung und Fehlerausgabe
		 */
		String[] words = dokument.getText(i).toLowerCase().replace("assign", "").replace("=", " ").trim().split("\\s+");
		
		if(words.length < 2) 
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Ein Teil des Befehls fehlt!\n");
			return;
		}
		else if(words.length > 2) 
		{
			//Test auf einen Methodenaufruf in der Zuweisung
			if(!(words[1].contains("run") || words[1].contains("new")))
			{
				fehler.add("Fehler in Zeile " + (i+1) + ": Ein Teil des Befehls fehlt!\n");
				return;
			}
		}
		
		/*
		 * Übersetzen in Javacode
		 */
		javacode[i] += words[0] + " = ";
		
		//Wenn ein Methodenaufruf existiert
		if(words[1].contains("run"))
		{
			words = dokument.getText(i).trim().split("=");
			runMethode(i, words[1]);
		}
		//Wenn ein neues Objekt erzeugt wird
		else if(words[1].contains("new"))
		{
			words = dokument.getText(i).trim().split("=");
			newObjekt(i, words[1]);
		}
		else //Primitive Wertzuweisung
		{
			words = dokument.getText(i).trim().split("=");
			javacode[i] += words[1].trim() + ";";
		}	
	}



	/* 
	 * for-Schleife:
	 * for variablenname=startwert to endwert "step schrittgröße"
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
				fehler.add("Fehler in Zeile " + (i+1) + ": Am Ende des Befehls stehen unbekannte Zeichen!\n");
				return;
			}
		}
		else if(words.length < 5)
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Der Befehl ist unvollständig!\n");
			return;
		}
		else if(words[3].equals("to"))
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Der Teilbefehl 'to' fehlt!\n");
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
	 * while-Schleife:
	 * while bedingung
	 */
	public void whileSchleife(int i) //i=Zeile
	{	
		/*
		 * Grammatikprüfung
		 */
		String[] words = dokument.getText(i).trim().split("\\s+");
		if(words.length == 1) 
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Die Bedingung fehlt!\n");
			return;
		}
		
		
		//Die Bedingung aus der Eingabe rausfiltern
		String bedingung = dokument.getText(i).replace("while", "").trim();
		
		//Die Bedingung dem Javacode hinzufügen
		javacode[i] += "while(" + bedingung + "){";
	}
	
	
	
	
	
	/*
	 * If Bedingung erstellen:
	 * if bedingung 
	 */
	public void ifBedingung(int i) 
	{
		/*
		 * Grammatikprüfung
		 */
		String[] words = dokument.getText(i).trim().split("\\s+");
		if(words.length == 1) 
		{
			fehler.add(javacode[i] += "Fehler in Zeile " + (i+1) + ": Die Bedingung fehlt!\n");
			return;
		}
		
		
		//Die Bedingung aus der Eingabe rausfiltern
		String bedingung = dokument.getText(i).replace("if", "").trim();
		
		//Die Bedingung dem Javacode hinzufügen
		javacode[i] += "if(" + bedingung + "){";
	}
	
	
	
	
	/*
	 * Else Bedingung erstellen:
	 * else bedingung 
	 */
	public void elseBedingung(int i, int limitation) 
	{
		//Einrückung anpassen
		javacode[i] = "";
		for (int j = 0; j < limitation-1; j++) 
		{
			javacode[i] += "   ";
		}
		
		//Die Bedingung dem Javacode hinzufügen
		javacode[i] += "}else{";
		
		//Kommentare
		if(dokument.getText(i).trim().split("\\s+").length > 1)
		{
			String words = dokument.getText(i).trim();
			javacode[i] += "//" + words;
		}
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
	 * Annotationen:
	 * @annotation 
	 */
	public void annotation(int i) //i=Zeile
	{	
		/*
		 * Grammatikprüfung
		 */
		String[] words = dokument.getText(i).trim().split("\\s+");
		
		if(words.length > 1) 
		{
			fehler.add("Fehler in Zeile " + (i+1) + ": Der Befehl enthält unbekannte Zeichen!\n");
			return;
		}
		
		
		/*
		 * Annotation rausfiltern und in Javacode übersetzen
		 */
		switch(words[0].toLowerCase()) 
		{
			case "@deprecated": javacode[i] += "@Deprecated"; break;
			case "@override"  : javacode[i] += "@Override"; break;
			case "@suppresswarnings": javacode[i] += "@SuppressWarnings"; break;
			case "@safevarargs":javacode[i] += "@SafeVarargs"; break;
			case "@documented": javacode[i] += "@Documented"; break;
			case "@inherited" : javacode[i] += "@Inherited"; break;
			case "@retention" : javacode[i] += "@Retention"; break;
			case "@target"    : javacode[i] += "@Target"; break;
			
			default: fehler.add("Fehler in Zeile " + (i+1) + ": " + words[0].trim() + " ist keine gültige Annotation!\n");

		}
	}
	
	
	
	
	/* 
	 * Fehlerausgabe, wovor der Dateipfad ausgegeben wird
	 */
	public void fehlerAusgabe()
	{	
		//Pfad ausgeben
		Anzeige.konsole.append(pfad.getAbsolutePath() + ":\n");
		
		//Fehler ausgeben
		for (String fehler: this.fehler) 
		{
			Anzeige.konsole.append(fehler + "\n");
		}
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
	    
		Anzeige.konsole.append("Der Übersetzungsvorgang wurde erfolgreich abgeschlossen!\n");
		Anzeige.konsole.append("Pfad der neuen Datei:  \n");
		Anzeige.konsole.append("	" + datei.getAbsolutePath() + "\n");


	}
}
