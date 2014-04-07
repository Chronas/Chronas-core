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
		int limitation = 0;
		
		//Forschleife, die alle Zeilen des chr-Dokuments durchgeht und übersetzt
		for (int i = 0; i < dokument.getLength(); i++)
		{
			//Zeile in einzelne Befehle aufteilen und Leerzeichen am Anfang und am Ende entfernen
			String[] words = dokument.getText(i).trim().split("\\s+");
			
			//Den Text des Javacodes von "null" auf "" setzen
			javacode[i] = "";
						
			//Leerzeichen am Anfang der Zeilen um den generierten Code übersichtlicher zu gestalten
			for (int j = 0; j < limitation; j++) 
			{
				javacode[i] += "   ";
			}
			
			
			
			
			//Befehle prüfen und übersetzen
			switch(words[0].toLowerCase())
			{
				//Methoden und Klassen
				case "class" : javacode[i] += "class " + words[1] + "{"; limitation++;  break;
				case "run"   : runMethode(i); break;		//Methode ausführen
				case "method": methode(i); limitation++; break;
			
				
				/*
				 * Kontrollstrukturen
				 */
				case "for": forSchleife(words, i); limitation++; break;
				case "}":   javacode[i] += "}";     limitation--; break;
				
				
				/*
				 * Variablentypen
				 */
				//Zahlen
				case "int":    javacode[i] += "int " + words[1] + ";";  break;
				case "long":   javacode[i] += "long " + words[1] + ";"; break;
				//Gleitkommazahlen
				case "float":    javacode[i] += "float " + words[1] + ";";  break;
				case "double":   javacode[i] += "double " + words[1] + ";"; break;
				
				//Zeichen und Zeichenketten
				case "string": javacode[i] += "String " + words[1] + ";";  break;
				case "char":   javacode[i] += "char " + words[1] + ";";  break;
				
				//Wahrheitswert
				case "bool":   javacode[i] += "boolean " + words[1] + ";";  break;
				
					
				
				/*
				 * Javacode & Kommentar
				 */
				case "java": for (int j = 1; j < words.length; j++)   //Javacode
								javacode[i] += words[j] + " ";
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
	public void forSchleife(String[] words, int i) //i=Zeile
	{		//Nur wenn die Sprachgrammatik stimmt, wird der Code ausgeführt
		if(words[2].equals("to"))
		{
		
			String[] var1 = words[1].split("=");					   //Aufteilung der Variablendeklaration in 2 Wörter
			javacode[i] += "for(int " + words[1] + "; " + var1[0];    //Variable deklarieren
		
			//Schrittzahl ist standartmäßig 1, kann allerdings geändert werden
			int step = 1;
			if(words.length >= 6 && words[5].equals("step"))
				step = Integer.parseInt(words[5]);
			
			if(Integer.parseInt(var1[1]) > Integer.parseInt(words[3]))//größer oder kleiner Zeichen
			{
				javacode[i] += ">" + words[3] + "; " + var1[0] + "-=" + step + "){";
			}
			else
			{
				javacode[i] += "<" + words[3] + "; " + var1[0] + "+=" + step + "){";
			}
		}
	}
	
	
	
	
	/*
	 * Mehtode erstellen:
	 * method rückgabetyp name <modifizierer>: parameter
	 */
	public void methode(int i) //i=Zeile
	{		
		//Modifizierer wie public, private, final, static
		String[] words = dokument.getText(i).trim().split("<");
		         words = words[1].trim().split(">");
		String   modifizierer = words[0].trim().replace(",", " ");
		javacode[i] += modifizierer + " ";
		
		//Rückgabetyp
		words = dokument.getText(i).trim().split("\\s+");
		javacode[i] += words[1] + " ";
		
		//Name 
		words = words[2].trim().split("<");
		javacode[i] += words[0] + "(";
        
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
		//String in einzelne Bestandteile aufteilen und den Doppelpunkt entfernen
		String[] words = dokument.getText(i).replace("run", "").trim().replace(':', ' ').replace(',', ' ').split("\\s+");
		
		/*
		 * Den Code übersetzen
		 */
		javacode[i] += words[0] + "(";
		//Die Variable wörter neu bestimmen
		words = dokument.getText(i).trim().split(":");
		
		//Argumente dem Javacode hinzufügen
		javacode[i] += words[1].trim() + ");";
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
        }
	    catch(IOException ioe)
	    { 
            ioe.printStackTrace(); 
        } 
	}
}
