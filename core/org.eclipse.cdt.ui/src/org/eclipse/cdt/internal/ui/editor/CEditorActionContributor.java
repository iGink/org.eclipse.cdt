package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
  
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.IStatusField;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextOperationAction;

import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;


public class CEditorActionContributor extends BasicTextEditorActionContributor {

	protected static class SelectionAction extends TextEditorAction implements ISelectionChangedListener {
		
		protected int fOperationCode;
		protected ITextOperationTarget fOperationTarget= null;
		
		
		public SelectionAction(String prefix, int operation) {
			super(CPlugin.getDefault().getResourceBundle(), prefix, null);
			fOperationCode= operation;
			setEnabled(false);
		}
		
		/**
		 * @see TextEditorAction#setEditor(ITextEditor)
		 */
		public void setEditor(ITextEditor editor) {
			if (getTextEditor() != null) {
				ISelectionProvider p= getTextEditor().getSelectionProvider();
				if (p != null) p.removeSelectionChangedListener(this);
			}
				
			super.setEditor(editor);
			
			if (editor != null) {
				ISelectionProvider p= editor.getSelectionProvider();
				if (p != null) p.addSelectionChangedListener(this);
				fOperationTarget= (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
			} else
				fOperationTarget= null;
				
			selectionChanged(null);
		}
		
		/**
		 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			boolean isEnabled= (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
			setEnabled(isEnabled);
		}
		
		/**
		 * @see Action#run()
		 */
		public void run() {
			if (fOperationCode != -1 && fOperationTarget != null)
				fOperationTarget.doOperation(fOperationCode);
		}
	};
	
		
	protected CEditor fCEditor;
	protected RetargetTextEditorAction fContentAssist;
	protected RetargetTextEditorAction fAddInclude;
	protected RetargetTextEditorAction fOpenOnSelection;
	protected SelectionAction fShiftLeft;
	protected SelectionAction fShiftRight;
	private TextOperationAction caAction;
	//private ToggleTextHoverAction fToggleTextHover;
	private GotoErrorAction fPreviousError;
	private GotoErrorAction fNextError;
	private Map fStatusFields;
	
	private final static String[] STATUSFIELDS= {
		CTextEditorActionConstants.STATUS_INPUT_MODE,
		CTextEditorActionConstants.STATUS_CURSOR_POS,
	};
	
	
	public CEditorActionContributor() {
		super();
		
		ResourceBundle bundle = CEditorMessages.getResourceBundle();

			
		fShiftRight= new SelectionAction("Editor.ShiftRight.", ITextOperationTarget.SHIFT_RIGHT);		
		fShiftLeft= new SelectionAction("Editor.ShiftLeft.", ITextOperationTarget.SHIFT_LEFT);
		
		CPluginImages.setImageDescriptors(fShiftRight, CPluginImages.T_LCL, CPluginImages.IMG_MENU_SHIFT_RIGHT);
		CPluginImages.setImageDescriptors(fShiftLeft, CPluginImages.T_LCL, CPluginImages.IMG_MENU_SHIFT_LEFT);
		
		fContentAssist = new RetargetTextEditorAction(bundle, "ContentAssistProposal.");
		fAddInclude = new RetargetTextEditorAction(bundle, "AddIncludeOnSelection");
		fOpenOnSelection = new RetargetTextEditorAction(bundle, "OpenOnSelection");

		//fToggleTextHover= new ToggleTextHoverAction();
		fPreviousError= new GotoErrorAction("Editor.PreviousError.", false); //$NON-NLS-1$
		CPluginImages.setImageDescriptors(fPreviousError, CPluginImages.T_TOOL, CPluginImages.IMG_TOOL_GOTO_PREV_ERROR);
		fNextError= new GotoErrorAction("Editor.NextError.", true); //$NON-NLS-1$
		CPluginImages.setImageDescriptors(fNextError, CPluginImages.T_TOOL, CPluginImages.IMG_TOOL_GOTO_NEXT_ERROR);
		
		fStatusFields= new HashMap(3);
		for (int i= 0; i < STATUSFIELDS.length; i++)
			fStatusFields.put(STATUSFIELDS[i], new StatusLineContributionItem(STATUSFIELDS[i]));
	}	


	
	/**
	 * @see IActionBarContributor#contributeToMenu(MenuManager)
	 */
	public void contributeToMenu(IMenuManager menu) {
		
		super.contributeToMenu(menu);
		
		/*
		 * Hook in the code assist
		 */
		
		IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {	
			editMenu.add(fShiftRight);
			editMenu.add(fShiftLeft);
					 		
			editMenu.add(new Separator(IContextMenuConstants.GROUP_OPEN));
			editMenu.add(fNextError);
			editMenu.add(fPreviousError);
			
			editMenu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
			editMenu.appendToGroup(IContextMenuConstants.GROUP_GENERATE, fContentAssist);
			editMenu.appendToGroup(IContextMenuConstants.GROUP_GENERATE, fAddInclude);
			editMenu.appendToGroup(IContextMenuConstants.GROUP_GENERATE, fOpenOnSelection);
		}
	}
	
	/**
	 * @see EditorActionBarContributor#contributeToToolBar(IToolBarManager)
	 */
	public void contributeToToolBar(IToolBarManager tbm) {
		super.contributeToToolBar(tbm);
		tbm.add(new Separator());
		//tbm.add(fTogglePresentation);
		//tbm.add(fToggleTextHover);
		tbm.add(fNextError);
		tbm.add(fPreviousError);
	}
	
	/**
	 * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) {
		
		super.setActiveEditor(part);
		
		ITextEditor textEditor= null;
		if (part instanceof ITextEditor)
			textEditor= (ITextEditor) part;
		
		if (part instanceof CEditor) {
			for (int i= 0; i < STATUSFIELDS.length; i++)
				((CEditor)part).setStatusField(null, STATUSFIELDS[i]);
		}	
			
		fShiftRight.setEditor(textEditor);
		fShiftLeft.setEditor(textEditor);
		fNextError.setEditor(textEditor);
		fPreviousError.setEditor(textEditor);
		//caAction.setEditor(textEditor);
		//caAction.update();
		fContentAssist.setAction(getAction(textEditor, "ContentAssistProposal")); //$NON-NLS-1$
		fAddInclude.setAction(getAction(textEditor, "AddIncludeOnSelection")); //$NON-NLS-1$
		fOpenOnSelection.setAction(getAction(textEditor, "OpenOnSelection")); //$NON-NLS-1$

		
		if (part instanceof CEditor) {
			CEditor ed = (CEditor) part;
			for (int i= 0; i < STATUSFIELDS.length; i++)
				ed.setStatusField((IStatusField) fStatusFields.get(STATUSFIELDS[i]), STATUSFIELDS[i]);
		}
	}
	
	/*
	 * @see EditorActionBarContributor#contributeToStatusLine(IStatusLineManager)
	 *
	 * More code here only until we move to 2.0...
	 */
	public void contributeToStatusLine(IStatusLineManager statusLineManager) {
		super.contributeToStatusLine(statusLineManager);
		for (int i= 0; i < STATUSFIELDS.length; i++)
			statusLineManager.add((IContributionItem) fStatusFields.get(STATUSFIELDS[i]));
	}	
}