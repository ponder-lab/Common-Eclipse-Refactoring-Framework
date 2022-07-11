package edu.cuny.citytech.refactoring.common.ui;

import java.util.function.Consumer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * @author <a href="mailto:raffi.khatchadourian@hunter.cuny.edu">Raffi
 *         Khatchadourian</a>
 */
public abstract class InputPage extends UserInputWizardPage {

	private RefactoringProcessor processor;

	private IDialogSettings settings;

	public InputPage(String name) {
		super(name);
	}

	protected void addBooleanButton(String text, final String key, final Consumer<Boolean> valueConsumer,
			Composite result) {
		Button button = new Button(result, SWT.CHECK);
		button.setText(text);
		boolean value = this.settings.getBoolean(key);
		valueConsumer.accept(value);
		button.setSelection(value);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selection = ((Button) e.widget).getSelection();
				InputPage.this.settings.put(key, selection);
				valueConsumer.accept(selection);
			}
		});
	}

	protected void addIntegerButton(String text, String key, Consumer<Integer> valueConsumer, Composite result) {
		Label label = new Label(result, SWT.HORIZONTAL);
		label.setText(text);

		Text textBox = new Text(result, SWT.SINGLE);
		int value = this.settings.getInt(key);
		valueConsumer.accept(value);
		textBox.setText(String.valueOf(value));
		textBox.addModifyListener(event -> {
			int selection;
			try {
				selection = Integer.parseInt(((Text) event.widget).getText());
			} catch (NumberFormatException e) {
				return;
			}
			InputPage.this.settings.put(key, selection);
			valueConsumer.accept(selection);
		});
	}

	@Override
	public void createControl(Composite parent) {
		ProcessorBasedRefactoring processorBasedRefactoring = (ProcessorBasedRefactoring) this.getRefactoring();
		RefactoringProcessor refactoringProcessor = processorBasedRefactoring.getProcessor();
		this.setProcessor(refactoringProcessor);
		this.loadSettings();

		Composite result = new Composite(parent, SWT.NONE);
		this.setControl(result);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		result.setLayout(layout);

		Label doit = new Label(result, SWT.WRAP);
		doit.setText(this.getDoItLabelTitle());
		doit.setLayoutData(new GridData());

		Label separator = new Label(result, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		Composite compositeForIntegerButton = new Composite(result, SWT.NONE);
		GridLayout layoutForIntegerButton = new GridLayout(2, false);

		compositeForIntegerButton.setLayout(layoutForIntegerButton);

		this.updateStatus();
		Dialog.applyDialogFont(result);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), this.getHelpContextID());
	}

	protected abstract String getDialoGSettingSectionTitle();

	protected abstract String getDoItLabelTitle();

	protected abstract String getHelpContextID();

	private void loadSettings() {
		this.settings = this.getDialogSettings().getSection(this.getDialoGSettingSectionTitle());
		if (this.settings == null)
			this.settings = this.getDialogSettings().addNewSection(this.getDialoGSettingSectionTitle());
	}

	protected abstract void setProcessor(RefactoringProcessor processor);

	private void updateStatus() {
		this.setPageComplete(true);
	}
}
