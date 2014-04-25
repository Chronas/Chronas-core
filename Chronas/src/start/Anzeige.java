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
	static JTextArea konsole = new JTextArea("Wählen Sie bitte einen Pfad aus!");
	
	public Anzeige()
	{
		//Schriftart
		Font font = new Font(Font.SERIF, Font.PLAIN, 25);

		
		//JFrame initialisieren
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-550)/2, (Toolkit.getDefaultToolkit() .getScreenSize().height-550)/2);
		frame.setSize(800, 550);
		
		
		//Textfeld, wo der Pfad angezeigt wird
 		final JTextField pfad = new JTextField();		
 		pfad.setBounds(0, 0, frame.getWidth()-75, 40);
 		pfad.setFont(font);
 		pfad.setDisabledTextColor(Color.darkGray);
 		pfad.setText("Pfad");
 		pfad.setEnabled(false);
 		frame.add(pfad);
 		
 		
 		//Pfadauswahl mit JButton
 		JButton pfadwahl = new JButton("...");			
 		pfadwahl.setBounds(pfad.getWidth(), 0, 75, 40);
 		pfadwahl.setFont(font);
 		pfadwahl.setForeground(Color.white);
 		pfadwahl.setBackground(Color.gray);
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
				
				if(!chooser.getSelectedFile().equals(""))
				{
					konsole.setText("Klicken Sie bitte auf Start!");
					f = chooser.getSelectedFile();									  //Pfad im Textfeld anzeigen
					pfad.setText(f.getAbsolutePath());	
				}
			}
 		});
 		
 		
 		//Button zum compilieren
 		JButton compilieren = new JButton("Start");			
 		compilieren.setBounds(0, 40, frame.getWidth()/2, 50);
 		compilieren.setFont(font);
 		compilieren.setForeground(Color.white);
 		compilieren.setBackground(Color.gray);
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
 		
 		
 		//Hilfefunktion
 		JButton hilfe = new JButton("Hilfe");			
 		hilfe.setBounds(frame.getWidth()/2, 40, frame.getWidth()/2, 50);
 		hilfe.setFont(font);
 		hilfe.setForeground(Color.white);
 		hilfe.setBackground(Color.gray);
 		frame.add(hilfe);
 		hilfe.addActionListener(new ActionListener()
 		{
 			@Override
			public void actionPerformed(ActionEvent arg0) 
 			{
				try
				{
					File file = new File("bin/Editor.jar");
					Runtime.getRuntime().exec("java -jar \"bin/Editor.jar\"");				} 
				catch (IOException e) 
				{
					System.out.println("Die Hilfe konnte nicht gestartet werden");
				}
			}
 		});
 		
 		
 		/*
 		 * Konsole
 		 */
 		//JPanel
 		JPanel panel = new JPanel();
 		panel.setLayout(null);
		panel.setBounds(0, 90, frame.getWidth(), frame.getHeight()-90); 
 		frame.add(panel);
 		//Konsole
		konsole.setBounds(0, 0, panel.getWidth(), panel.getHeight()); 	
		konsole.setFont(font);
 		konsole.setDisabledTextColor(Color.white);
 		konsole.setBackground(Color.black);
 		konsole.setEnabled(false);
 		//JScrollPane für die Konsole
 		JScrollPane scroll = new JScrollPane(konsole);
 		scroll.setBounds(0, 0, panel.getWidth(), panel.getHeight());
		panel.add(scroll);

 		
		
		
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
						//CanonicalPath von erstem Element wird in Textfeld geschrieben(Nur .chr Dateien)
						if(list.get(0).getCanonicalPath().contains(".chr"))
						{
							pfad.setText(list.get(0).getCanonicalPath());
							f = new File(pfad.getText());
						}
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
