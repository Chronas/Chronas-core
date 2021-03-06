package start;

import java.io.*;

public class ChrDokument 
{
	//In diesem Array wird das gesamte Dokument in einzelne Wörter gesplitet gespeichert
	String[] dokument = null;
	
	public ChrDokument(File pfad)
	{
		try 
		{
			/*
			 * Zeilen lesen und abspeichern
			 */
			BufferedReader in = new BufferedReader(new FileReader(pfad));
			int length = 0;
			//Länge des Dokuments herausfinden
			for(int i=0; in.readLine() != null; length++){}
			
			//Dokument erstellen
			dokument = new String[length];
			
			System.out.println("Zeilen: " + length);
			
			//Dokument abspeichern
			in = new BufferedReader(new FileReader(pfad));
			for(int i=0; i<dokument.length ;i++)
			{
				dokument[i] = in.readLine(); 
			}
			
			//Schließen des Datenstroms
			in.close();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}	
	}
	
	
	/*
	 * Anzahl der Zeilen ausgeben
	 */
	public int getLength()
	{
		return dokument.length;
	}
	
	/*
	 * Text in Zeile i ausgeben
	 */
	public String getText(int i)
	{
		return dokument[i];
	}
}
