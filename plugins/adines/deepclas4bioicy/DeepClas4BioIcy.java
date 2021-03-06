package plugins.adines.deepclas4bioicy;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import icy.gui.dialog.ActionDialog;
import icy.gui.dialog.MessageDialog;
import icy.image.IcyBufferedImage;
import icy.plugin.abstract_.PluginActionable;

public class DeepClas4BioIcy extends PluginActionable {


	private JComboBox<String> frameworkChoices;
	private JComboBox<String> modelChoices;
	private ActionDialog gd;

	@Override
	public void run() {

		IcyBufferedImage imp = getActiveImage();

		if (imp == null) {
			MessageDialog.showDialog("This plugin needs an opened image.");
			return;
		}

		String image = getActiveSequence().getFilename();

		try {

			String comando ="deepclas4bio-listFrameworks";
			Process p = Runtime.getRuntime().exec(comando);
			p.waitFor();
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("data.json"));
			JSONArray frameworks = (JSONArray) jsonObject.get("frameworks");

			int i = 0;
			String opcionesFramework[] = new String[frameworks.size()];
			for (Object o : frameworks) {
				opcionesFramework[i] = (String) o;
				i++;
			}

			frameworkChoices = new JComboBox<>(opcionesFramework);
			modelChoices = new JComboBox<>();

			JLabel label1 = new JLabel("Select the framework and the model");
			JLabel lFrmaework = new JLabel("Framework: ");
			JLabel lModel = new JLabel("Model: ");

			GridLayout gl = new GridLayout(3, 2);
			gl.setHgap(10);
			gl.setVgap(10);

			comando ="deepclas4bio-listModels Keras";
			p = Runtime.getRuntime().exec(comando);
			p.waitFor();
			JSONParser parser2 = new JSONParser();
			JSONObject jsonObject2 = (JSONObject) parser2.parse(new FileReader("data.json"));
			JSONArray models = (JSONArray) jsonObject2.get("models");
			modelChoices.removeAllItems();
			for (Object o : models) {
				modelChoices.addItem((String) o);
			}

			JPanel gd1 = new JPanel(gl);

			gd1.add(label1);
			gd1.add(new Label());
			gd1.add(lFrmaework);
			gd1.add(frameworkChoices);
			gd1.add(lModel);

			gd1.add(modelChoices);
			gd1.repaint();

			gd = new ActionDialog("Select Input", gd1);
			gd.pack();

			frameworkChoices.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					try {
						String frameworkSelected = (String) frameworkChoices.getSelectedItem();
						String comando ="deepclas4bio-listModels " + frameworkSelected;
						Process p = Runtime.getRuntime().exec(comando);
						p.waitFor();
						JSONParser parser = new JSONParser();
						JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("data.json"));
						JSONArray frameworks = (JSONArray) jsonObject.get("models");
						modelChoices.removeAllItems();
						for (Object o : frameworks) {
							modelChoices.addItem((String) o);
						}
						modelChoices.doLayout();

					} catch (InterruptedException ex) {
						Logger.getLogger(DeepClas4BioIcy.class.getName()).log(Level.SEVERE, null, ex);
					} catch (IOException ex) {
						Logger.getLogger(DeepClas4BioIcy.class.getName()).log(Level.SEVERE, null, ex);
					} catch (ParseException ex) {
						Logger.getLogger(DeepClas4BioIcy.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			});

			gd.setVisible(true);
			if (gd.isCanceled()) {
				return;
			}

			String framework = (String) frameworkChoices.getSelectedItem();
			String model = (String) modelChoices.getSelectedItem();

			comando ="deepclas4bio-predict " + image + " " + framework + " " + model;
			System.out.println(comando);
			p = Runtime.getRuntime().exec(comando);
			p.waitFor();

			JSONParser parser3 = new JSONParser();
			JSONObject jsonObject3 = (JSONObject) parser3.parse(new FileReader("data.json"));
			String classPredict = (String) jsonObject3.get("class");

			MessageDialog.showDialog("Prediction", "The class which the image belongs is " + classPredict);

		} catch (FileNotFoundException ex) {
			Logger.getLogger(DeepClas4BioIcy.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(DeepClas4BioIcy.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InterruptedException ex) {
			Logger.getLogger(DeepClas4BioIcy.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ParseException ex) {
			Logger.getLogger(DeepClas4BioIcy.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

}
