/*******************************************************************************
 * Copyright or © or Copr. Institut des Sciences du Mouvement 
 * (CNRS & Aix Marseille Université)
 * 
 * The DOCoMETER Software must be used with a real time data acquisition 
 * system marketed by ADwin (ADwin Pro and Gold, I and II) or an Arduino 
 * Uno. This software, created within the Institute of Movement Sciences, 
 * has been developed to facilitate their use by a "neophyte" public in the 
 * fields of industrial computing and electronics.  Students, researchers or 
 * engineers can configure this acquisition system in the best possible 
 * conditions so that it best meets their experimental needs. 
 * 
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 * 
 * Contributors:
 *  - Frank Buloup - frank.buloup@univ-amu.fr - initial API and implementation [25/03/2020]
 ******************************************************************************/
package fr.univamu.ism.docometre.dacqsystems.adwin;

import fr.univamu.ism.docometre.dacqsystems.Channel;
import fr.univamu.ism.docometre.dacqsystems.ChannelProperties;
import fr.univamu.ism.docometre.dacqsystems.Property;

public final class ADWinDigInOutChannelProperties extends Property {

	public static final ADWinDigInOutChannelProperties IN_OUT = new ADWinDigInOutChannelProperties("ADWinDigInOutChannelProperties.IN_OUT", ADWinMessages.InOut_Label, ADWinMessages.InOut_Tooltip, "^(input|output)$", "input:output");  
	public static final ADWinDigInOutChannelProperties STIMULUS = new ADWinDigInOutChannelProperties("ADWinDigInOutChannelProperties.STIMULUS", ADWinMessages.DIO_Stimulus_Label, ADWinMessages.DIO_Stimulus_Tooltip, "^(true|false)$", "true:false");
	
	public static String INPUT = "input";
	public static String OUTPUT = "output";
	public static String[] INPUT_OUTPUT = new String[]{INPUT, OUTPUT};
	
	public static void populateProperties(Channel channel) {
		ChannelProperties.populateProperties(channel);
		channel.setProperty(STIMULUS, "false");
		channel.setProperty(IN_OUT, "input");
	}

	public static Channel cloneChannel(ADWinChannel channel) {
		ADWinChannel newChannel = new ADWinChannel(null);
		ChannelProperties.cloneChannel(channel, newChannel);
		newChannel.setProperty(IN_OUT, new String(channel.getProperty(IN_OUT)));
		newChannel.setProperty(STIMULUS, new String(channel.getProperty(STIMULUS)));
		return channel;
	}

	private ADWinDigInOutChannelProperties(String key, String label, String tooltip, String regExp) {
		super(key, label, tooltip, regExp);
	}
	
	private ADWinDigInOutChannelProperties(String key, String label, String tooltip, String regExp, String availableValues) {
		super(key, label, tooltip, regExp, availableValues);
	}
}
