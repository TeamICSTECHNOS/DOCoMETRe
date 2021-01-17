package fr.univamu.ism.docometre.analyse.handlers;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import fr.univamu.ism.docometre.Activator;
import fr.univamu.ism.docometre.ObjectsController;
import fr.univamu.ism.docometre.ResourceProperties;
import fr.univamu.ism.docometre.analyse.MathEngineFactory;
import fr.univamu.ism.docometre.analyse.datamodel.BatchDataProcessing;
import fr.univamu.ism.docometre.analyse.datamodel.BatchDataProcessingItem;
import fr.univamu.ism.docometre.preferences.MathEnginePreferencesConstants;
import fr.univamu.ism.process.Script;
import fr.univamu.ism.process.ScriptSegmentType;

public final class RunBatchDataProcessingDelegate {
	
	public static void run(BatchDataProcessing batchDataProcessing) {
		// Get all data processing
		BatchDataProcessingItem[] processes = batchDataProcessing.getProcesses();
		ArrayList<IResource> processesResource = new ArrayList<>();
		for (BatchDataProcessingItem batchDataProcessingItem : processes) {
			if(batchDataProcessingItem.isActivated()) {
				IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(batchDataProcessingItem.getPath());
				if(resource != null) processesResource.add(resource);
			}
		}
		// Generate global script
		String code = "";
		for (IResource resource : processesResource) {
			boolean removeHandle = false;
			Object object = ResourceProperties.getObjectSessionProperty(resource);
			if(object == null) {
				object = ObjectsController.deserialize((IFile)resource);
				ResourceProperties.setObjectSessionProperty(resource, object);
				ObjectsController.addHandle(object);
				removeHandle = true;
			}
			if(object instanceof Script) {
				try {
					Script script = (Script)object;
					code = code + script.getLoopCode(object, ScriptSegmentType.LOOP) + "\n";
				} catch (Exception e) {
					Activator.logErrorMessageWithCause(e);
					e.printStackTrace();
				}
			}
			if(removeHandle) ObjectsController.removeHandle(object);
		}
		System.out.println(code);
		// Get all subjects
		BatchDataProcessingItem[] subjects = batchDataProcessing.getSubjects();
		ArrayList<IResource> subjectsResource = new ArrayList<>();
		for (BatchDataProcessingItem batchDataProcessingItem : subjects) {
			if(batchDataProcessingItem.isActivated()) {
				IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(batchDataProcessingItem.getPath());
				if(resource != null) subjectsResource.add(resource);
			}
		}
		// For each subject
		for (IResource subjectResource : subjectsResource) {
			// Load subject if necessary
			if(batchDataProcessing.loadSubject() && !MathEngineFactory.getMathEngine().isSubjectLoaded(subjectResource)) {
				boolean loadFromSavedFile = Activator.getDefault().getPreferenceStore().getBoolean(MathEnginePreferencesConstants.ALWAYS_LOAD_FROM_SAVED_DATA);
				MathEngineFactory.getMathEngine().load(subjectResource, loadFromSavedFile);
			}
			// Run global script on current subject
			// TODO
			code = code.replaceAll("['ReachabilityCoriolis\\.\\w+\\.", "");// Only for Python...
			MathEngineFactory.getMathEngine().runScript(code);
		}
		
	}

}
