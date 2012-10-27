package ccw.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.preferences.PreferenceConstants;


public final class ClojurePackageElementChangeListener implements
		IElementChangedListener {

	private static final String CLOJURE_PACKAGE = "clojure.lang";
	static final IPath CLOJURE_PACKAGE_PATH = new Path("clojure/lang");

	public void elementChanged(ElementChangedEvent javaModelEvent) {
		
		if (!CCWPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CCW_GENERAL_AUTOMATIC_NATURE_ADDITION))
			return;
		
		IJavaElementDelta delta = javaModelEvent.getDelta();
		IJavaElement element = delta.getElement();
		
		if (element instanceof IJavaModel) {
			
			visitJavaModelDelta(delta);
			
		} else if (element instanceof IJavaProject) {
			
			visitJavaProjectDelta(delta);
			
		} else if (element instanceof IPackageFragmentRoot) {
			
			IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) element;
			if (!ClojureNaturePropertyTest.hasClojureNature(packageFragmentRoot.getJavaProject().getProject())) {
				visitPackageFragmentRootDelta(delta);
			}
			
		}
	}
	
	private void visitJavaModelDelta(IJavaElementDelta javaModelDelta) {
		for (IJavaElementDelta javaProjectDelta: javaModelDelta.getAffectedChildren()) {
			visitJavaProjectDelta(javaProjectDelta);
		}
	}
	
	private void visitJavaProjectDelta(IJavaElementDelta javaProjectDelta) {
		IJavaProject javaProject = (IJavaProject) javaProjectDelta.getElement();
		IProject project = javaProject.getProject();

		if (!project.exists() || !project.isOpen())
			return;
		
		if (ClojureNaturePropertyTest.hasClojureNature(project))
			return;
		
		for (IJavaElementDelta fragmentRootDelta: javaProjectDelta.getAffectedChildren()) {
			if (visitPackageFragmentRootDelta(fragmentRootDelta)) {
				// Clojure fragment found, stop 
				break;
			}
		}
	}

	private boolean visitPackageFragmentRootDelta(IJavaElementDelta packageFragmentRootDelta) {
		IPackageFragmentRoot packageElement = (IPackageFragmentRoot) packageFragmentRootDelta.getElement();
		if (isClojureElement(packageElement)) {
			addClojureNature(packageElement.getJavaProject().getProject());
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isClojureElement(IPackageFragmentRoot packageFragmentRoot) {
		return packageFragmentRoot.getPackageFragment(CLOJURE_PACKAGE). exists();
	}

	private void addClojureNature(final IProject project) {
		WorkspaceJob job = new ClojureNatureAdderWorkspaceJob(project);
		job.schedule(100);
	}

	public void performFullScan() {
		if (!CCWPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CCW_GENERAL_AUTOMATIC_NATURE_ADDITION))
			return;
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project: workspaceRoot.getProjects()) {
			if (isCandidateClojureProject(project)) {
				addClojureNature(project);
			}
		}
	}
	
	private boolean isCandidateClojureProject(IProject project) {
		try {
			return (project.exists()
					&& 
					project.isOpen()
					&&
					!project.hasNature(ClojureCore.NATURE_ID)
					&&
					project.hasNature(JavaCore.NATURE_ID)
					&&
					(JavaCore.create(project).findElement(CLOJURE_PACKAGE_PATH)) != null);
		} catch (JavaModelException e) {
			CCWPlugin.logError("Error while  trying to determine if project " + project.getName() + " is a candidate to be converted to a clojure project", e);
			return false;
		} catch (CoreException e) {
			CCWPlugin.logError("Error while  trying to determine if project " + project.getName() + " is a candidate to be converted to a clojure project", e);
			return false;
		}
	}
	
}