package fr.univamu.ism.docometre.analyse.handlers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import fr.univamu.ism.docometre.ResourceType;
import fr.univamu.ism.docometre.analyse.MathEngineFactory;

public class LoadUnloadSubjectsHandler extends AbstractHandler implements ISelectionListener {
	
	private Set<IResource> selectedSubjects = new HashSet<IResource>(0);
	
	public LoadUnloadSubjectsHandler() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(this);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		for (IResource subject : selectedSubjects) {
			MathEngineFactory.getMathEngine().isSubjectLoaded(subject);
		}
		return null;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		selectedSubjects.clear();
		if(!(selection instanceof StructuredSelection)) return;
		StructuredSelection structuredSelection = (StructuredSelection)selection;
		for (Object element : structuredSelection) {
			if(element instanceof IResource) {
				IResource resource = (IResource)element;
				if(ResourceType.isSubject(resource)) selectedSubjects.add(resource);
			}
		}
	}

}