package start;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;


public class Anzeige 
{
	JFrame frame = new JFrame("Chronas");
	File f;//Pfad zur Datei
	
	public Anzeige()
	{
		//Schriftart
		Font font = new Font(Font.SERIF, Font.PLAIN, 25);

		//JFrame initialisieren
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-550)/2, (Toolkit.getDefaultToolkit() .getScreenSize().height-550)/2);
		frame.setSize(550, 550);
		
		//Pfad zur Datei
		JLabel label = new JLabel("Pfad: ");
		label.setBounds(25, 100, 75, 25);
		label.setFont(font);
		frame.add(label);
		
		//Textfeld
 		final JTextField pfad = new JTextField();		
 		pfad.setBounds(100, 100, 350, 25);
 		pfad.setFont(font);
 		frame.add(pfad);
 		
 		//Pfadauswahl mit JButton
 		JButton pfadwahl = new JButton("...");			
 		pfadwahl.setBounds(450, 100, 35, 25);
 		pfadwahl.setFont(font);
 		frame.add(pfadwahl);
 		pfadwahl.addActionListener(new ActionListener()
 		{
 			@Override
			public void actionPerformed(ActionEvent arg0) 
 			{
				JFileChooser chooser = new JFileChooser("c:/");
				FileFilter filter = new FileNameExtensionFilter("Chronasdatei", "chr"); //Filter für Dateiendungen
		        chooser.setFileFilter(filter);
		        chooser.setAcceptAllFileFilterUsed(false);
		        chooser.showOpenDialog(null);		    							  //Pfad auswählen mit JFileChooser
				f = chooser.getSelectedFile();									  //Pfad im Textfeld anzeigen
				pfad.setText(f.getAbsolutePath());									  
			}
 		});
 		
 		//Button zum compilieren
 		JButton compilieren = new JButton("Start");			
 		compilieren.setBounds(175, 300, 200, 100);
 		compilieren.setFont(font);
 		frame.add(compilieren);
 		compilieren.addActionListener(new ActionListener()
 		{
 			@Override
			public void actionPerformed(ActionEvent arg0) 
 			{
				if(!pfad.getText().equals(""))
				{
					//Compiler erstellen und ihm das ChrDokument übergeben
					ChrDokument dokument = new ChrDokument(f);
					Compiler compiler = new Compiler(dokument, f);
				}
			}
 		});
 		
 		
 		/*
 		 * Dateien per Drag & Drop einfügen
 		 */
		//Neues DropTarget erstellen
		DropTarget target = new DropTarget(frame.getContentPane(), new DropTargetAdapter() {
			
			//DropEvent Abfangen
			@SuppressWarnings("unchecked")
			public void drop(DropTargetDropEvent dtde) {
				if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					dtde.acceptDrop(dtde.getDropAction());
					try {
						//gedroppte Elemente werden in List geschrieben
						List<File> list = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
						//CanonicalPath von erstem Element wird in Textfeld geschrieben.
						pfad.setText(list.get(0).getCanonicalPath());
					} catch (UnsupportedFlavorException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
 		
 		frame.setVisible(true);
	}
}
