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
	
	//�bersetzter Code
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
	 * �bersetzen des Codes in Javacode
	 */
	private void compilieren()
	{
		for (int i = 0; i < dokument.getLength(); i++) 
		{
			//Zeile in einzelne Befehle aufteilen
			String[] w�rter = dokument.getText(i).split("\\s+" );
			
			//Den Text der entsprechenden zeile von "null" auf "" setzen
			javacode[i] = "";
						
			//Befehle pr�fen
			switch(w�rter[0])
			{
				//Methoden
				case "method": javacode[i] += "void " + w�rter[1] + "(){";
							   break;
			
				/*
				 * Kontrollstrukturen
				 */
				case "for": String[] var1 = w�rter[1].split("=");					   //Erste Variable
							javacode[i] += "for(int " + w�rter[1] + "; " + var1[0];//Variable deklarieren
							
							if(Integer.parseInt(var1[1]) > Integer.parseInt(w�rter[2]))//gr��er oder kleiner Zeichen
							{
								javacode[i] += ">" + w�rter[2] + "; " + var1[0] + "--" + "){";
							}
							else
							{
								javacode[i] += "<" + w�rter[2] + "; " + var1[0] + "++" + "){";
							}
							break;
				case "}":   javacode[i] += "}";
							break;
							
				/*
				 * Javacode & Kommentar
				 */
				case "java": for (int j = 1; j < w�rter.length; j++)   //Javacode
								javacode[i] += w�rter[j] + " ";
						     break;
				default:    javacode[i] += "//" + dokument.getText(i); //Kommentar	
			}
		}
	}
	
	/*
	 * Abspeichern des Javacodes als .java Datei
	 */
	public void speichern()
	{
		//Neuen Pfad erstellen
		String neuPfad = pfad.getAbsolutePath();
		String epfad = neuPfad.replace("chr", "class");
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
