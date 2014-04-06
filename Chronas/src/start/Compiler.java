package start;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Compiler 
{
	//Pfad zum Dokument und das Dokument
	File pfad;
	ChrDokument dokument;
	
	//Übersetzter Code
	String[] javacode;
	
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
	private void compilieren()
	{
		//Variable, in der die Stärke der Einrückung der Befehle gespeichert ist
		int einrückung = 0;
		
		//Forschleife, die alle Zeilen des chr-Dokuments durchgeht und übersetzt
		for (int i = 0; i < dokument.getLength(); i++)
		{
			//Zeile in einzelne Befehle aufteilen und Leerzeichen am Anfang und am Ende entfernen
			String[] wörter = dokument.getText(i).trim().split("\\s+");
			
			//Den Text des Javacodes von "null" auf "" setzen
			javacode[i] = "";
						
			//Leerzeichen am Anfang der Zeilen um den generierten Code übersichtlicher zu gestalten
			for (int j = 0; j < einrückung; j++) 
			{
				javacode[i] += "   ";
			}
			
			
			
			
			//Befehle prüfen und übersetzen
			switch(wörter[0].toLowerCase())
			{
				//Methoden und Klassen
				case "class" : javacode[i] += "class " + wörter[1] + "{"; einrückung++;  break;
				case "run"   : runMethode(i); break;		//Methode ausführen
				case "method": javacode[i] += "void " + wörter[1] + "(){"; einrückung++; break;
			
				
				/*
				 * Kontrollstrukturen
				 */
				case "for": forSchleife(wörter, i); einrückung++; break;
				case "}":   javacode[i] += "}";     einrückung--; break;
				
				
				/*
				 * Variablentypen
				 */
				//Zahlen
				case "int":    javacode[i] += "int " + wörter[1] + ";";  break;
				case "long":   javacode[i] += "long " + wörter[1] + ";"; break;
				//Gleitkommazahlen
				case "float":    javacode[i] += "float " + wörter[1] + ";";  break;
				case "double":   javacode[i] += "double " + wörter[1] + ";"; break;
				
				//Zeichen und Zeichenketten
				case "string": javacode[i] += "String " + wörter[1] + ";";  break;
				case "char":   javacode[i] += "char " + wörter[1] + ";";  break;
				
				//Wahrheitswert
				case "bool":   javacode[i] += "boolean " + wörter[1] + ";";  break;
				
					
				
				/*
				 * Javacode & Kommentar
				 */
				case "java": for (int j = 1; j < wörter.length; j++)   //Javacode
								javacode[i] += wörter[j] + " ";
						     break;
				default:    javacode[i] += "//" + dokument.getText(i).trim(); //Kommentar	
			}
			
		}
	}
	
	
	
	
	
	/* 
	 * for-Schleife mit der Struktur:
	 * for variablenname=startwert to endwert "step schrittgröße"
	 * übersetzt
	 */
	public void forSchleife(String[]wörter, int i) //i=Zeile
	{		//Nur wenn die Sprachgrammatik stimmt, wird der Code ausgeführt
		if(wörter[2].equals("to"))
		{
		
			String[] var1 = wörter[1].split("=");					   //Aufteilung der Variablendeklaration in 2 Wörter
			javacode[i] += "for(int " + wörter[1] + "; " + var1[0];    //Variable deklarieren
		
			//Schrittzahl ist standartmäßig 1, kann allerdings geändert werden
			int step = 1;
			if(wörter.length >= 6 && wörter[5].equals("step"))
				step = Integer.parseInt(wörter[5]);
			
			if(Integer.parseInt(var1[1]) > Integer.parseInt(wörter[3]))//größer oder kleiner Zeichen
			{
				javacode[i] += ">" + wörter[3] + "; " + var1[0] + "-=" + step + "){";
			}
			else
			{
				javacode[i] += "<" + wörter[3] + "; " + var1[0] + "+=" + step + "){";
			}
		}
	}
	
	
	
	
	/*
	 * Methode ausführen
	 * Die Argumente sind hinter dem Doppelpunkt und werden durch Kommas getrennt
	 */
	public void runMethode(int i) //i=Zeile
	{		
		//String in einzelne Bestandteile aufteilen und den Doppelpunkt entfernen
		String[] wörter = dokument.getText(i).replace("run", "").trim().replace(':', ' ').replace(',', ' ').split("\\s+");
		
		/*
		 * Den Code übersetzen
		 */
		javacode[i] += wörter[0] + "(";
		//Die Variable wörter neu bestimmen
		wörter = dokument.getText(i).trim().split(":");
		
		//Argumente dem Javacode hinzufügen
		javacode[i] += wörter[1].trim() + ");";
		
		
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
	    try{ 
            PrintWriter pWriter = new PrintWriter(new FileWriter(datei)); 
            for (int i = 0; i < javacode.length; i++) 
            {
				pWriter.println(javacode[i]);
	            pWriter.flush(); 
			}
        }catch(IOException ioe){ 
            ioe.printStackTrace(); 
        } 
	}
}
