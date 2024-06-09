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
package fr.univamu.ism.docometre.scripteditor.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import fr.univamu.ism.docometre.Activator;
import fr.univamu.ism.process.Block;
import fr.univamu.ism.process.ScriptSegment;
import fr.univamu.ism.process.ScriptSegmentType;

public class CreateBlockCommand extends Command {
	
	private Block block;
	private ScriptSegment scriptSegment;
	
	public CreateBlockCommand(ScriptSegment scriptSegment, Block block, Rectangle rectangle) {
		this.scriptSegment = scriptSegment;
		this.block = block;
		block.setX(rectangle.x);
		block.setY(rectangle.y);
	}

	@Override
	public void execute() {
		activateSegmentProcessEditor();
		if(scriptSegment.getScriptSegmentType().equals(ScriptSegmentType.INITIALIZE)) scriptSegment.getScript().addInitializeBlock(block);
		if(scriptSegment.getScriptSegmentType().equals(ScriptSegmentType.LOOP)) scriptSegment.getScript().addLoopBlock(block);
		if(scriptSegment.getScriptSegmentType().equals(ScriptSegmentType.FINALIZE)) scriptSegment.getScript().addFinalizeBlock(block);
//		if(scriptSegment.getScriptSegmentType().equals(ScriptSegmentType.DATA_PROCESSING)) scriptSegment.getScript().addLoopBlock(block);
	}
	
	@Override
	public void undo() {
		activateSegmentProcessEditor();
		if(scriptSegment.getScriptSegmentType().equals(ScriptSegmentType.INITIALIZE)) scriptSegment.getScript().removeInitializeBlock(block);
		if(scriptSegment.getScriptSegmentType().equals(ScriptSegmentType.LOOP)) scriptSegment.getScript().removeLoopBlock(block);
		if(scriptSegment.getScriptSegmentType().equals(ScriptSegmentType.FINALIZE)) scriptSegment.getScript().removeFinalizeBlock(block);
//		if(scriptSegment.getScriptSegmentType().equals(ScriptSegmentType.DATA_PROCESSING)) scriptSegment.getScript().removeLoopBlock(block);
	}
	
	private void activateSegmentProcessEditor() {
		Activator.activateScriptSegmentEditor(scriptSegment);
	}

}
