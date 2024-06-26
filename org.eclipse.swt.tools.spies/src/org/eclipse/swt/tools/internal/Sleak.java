/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tools.internal;

import java.io.*;
import java.util.Arrays;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Instructions on how to use the Sleak tool with a standlaone SWT example:
 * 
 * Modify the main method below to launch your application.
 * Run Sleak.
 * 
 */
public class Sleak {
	ListViewer listViewer;
	Canvas canvas;
	Button start, stop, check;
	Text text;
	Label label;
	
	Object [] oldObjects = new Object [0];
	Error [] oldErrors = new Error [0];
	Object [] objects = new Object [0];
	Error [] errors = new Error [0];

public static void main (String [] args) {
	DeviceData data = new DeviceData();
	data.tracking = true;
	Display display = new Display (data);
	Sleak sleak = new Sleak ();
	Shell shell = new Shell(display);
	shell.setText ("S-Leak");
	Point size = shell.getSize ();
	shell.setSize (size.x / 2, size.y / 2);
	sleak.create (shell);
	shell.open();
	
	// Launch your application here
	// e.g.		
//	Shell shell = new Shell(display);
//	Button button1 = new Button(shell, SWT.PUSH);
//	button1.setBounds(10, 10, 100, 50);
//	button1.setText("Hello World");
//	Image image = new Image(display, 20, 20);
//	Button button2 = new Button(shell, SWT.PUSH);
//	button2.setBounds(10, 70, 100, 50);
//	button2.setImage(image);
//	shell.open();
	
	while (!shell.isDisposed ()) {
		if (!display.readAndDispatch ()) display.sleep ();
	}
	display.dispose ();
}

public void create (Composite parent) {
	SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

	Composite leftContainer = new Composite(sashForm, SWT.BORDER);
	leftContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
	leftContainer.setLayout(new GridLayout());
	
	start = new Button (leftContainer, SWT.PUSH);
	start.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	start.setText ("Snap");
	start.addListener (SWT.Selection, event -> refreshAll ());
	
	stop = new Button (leftContainer, SWT.PUSH);
	stop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	stop.setText ("Diff");
	stop.addListener (SWT.Selection, event -> refreshDifference ());
	
	check = new Button (leftContainer, SWT.CHECK);
	check.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	check.setText ("Stack");
	check.addListener (SWT.Selection, e -> toggleStackTrace ());
	check.setSelection (false);
	
	listViewer = new ListViewer(leftContainer, SWT.BORDER | SWT.V_SCROLL);
	listViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	listViewer.getList().addListener (SWT.Selection, event -> refreshObject ());
	listViewer.setContentProvider(new ArrayContentProvider());
	listViewer.setLabelProvider(new LabelProvider());
	listViewer.setComparator(new ViewerComparator());
	
	label = new Label (leftContainer, SWT.BORDER);
	label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	label.setText ("0 object");
	
	canvas = new Canvas (sashForm, SWT.BORDER);
	canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	canvas.addListener (SWT.Paint, event -> paintCanvas (event));
	
	text = new Text(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
	text.setVisible (false);

    sashForm.setWeights(new int[] { 50, 25, 25});
    sashForm.setSashWidth(5);
}

void refreshLabel () {
	int colors = 0, cursors = 0, fonts = 0, gcs = 0, images = 0;
	int paths = 0, patterns = 0, regions = 0, textLayouts = 0, transforms= 0;
	for (int i=0; i<objects.length; i++) {
		Object object = objects [i];
		if (object instanceof Color) colors++;
		if (object instanceof Cursor) cursors++;
		if (object instanceof Font) fonts++;
		if (object instanceof GC) gcs++;
		if (object instanceof Image) images++;
		if (object instanceof Path) paths++;
		if (object instanceof Pattern) patterns++;
		if (object instanceof Region) regions++;
		if (object instanceof TextLayout) textLayouts++;
		if (object instanceof Transform) transforms++;
	}
	String string = "";
	if (colors != 0) string += colors + " Color(s)\n";
	if (cursors != 0) string += cursors + " Cursor(s)\n";
	if (fonts != 0) string += fonts + " Font(s)\n";
	if (gcs != 0) string += gcs + " GC(s)\n";
	if (images != 0) string += images + " Image(s)\n";
	if (paths != 0) string += paths + " Paths(s)\n";
	if (patterns != 0) string += patterns + " Pattern(s)\n";
	if (regions != 0) string += regions + " Region(s)\n";
	if (textLayouts != 0) string += textLayouts + " TextLayout(s)\n";
	if (transforms != 0) string += transforms + " Transform(s)\n";
	string += objects.length + " total object(s) (handle(s))";
	label.setText (string);
	layout();
}

void refreshDifference () {
	Display display = canvas.getDisplay();
	DeviceData info = display.getDeviceData ();
	if (!info.tracking) {
		Shell shell = canvas.getShell();
		MessageBox dialog = new MessageBox (shell, SWT.ICON_WARNING | SWT.OK);
		dialog.setText (shell.getText ());
		dialog.setMessage ("Warning: Device is not tracking resource allocation");
		dialog.open ();
	}
	Object [] newObjects = info.objects;
	Error [] newErrors = info.errors;
	Object [] diffObjects = new Object [newObjects.length];
	Error [] diffErrors = new Error [newErrors.length];
	int count = 0;
	for (int i=0; i<newObjects.length; i++) {
		int index = 0;
		while (index < oldObjects.length) {
			if (newObjects [i] == oldObjects [index]) break;
			index++;
		}
		if (index == oldObjects.length) {
			diffObjects [count] = newObjects [i];
			diffErrors [count] = newErrors [i];
			count++;
		}
	}
	objects = new Object [count];
	errors = new Error [count];
	System.arraycopy (diffObjects, 0, objects, 0, count);
	System.arraycopy (diffErrors, 0, errors, 0, count);
	listViewer.setInput(null);
	text.setText ("");
	canvas.redraw ();
	
	listViewer.setInput(objects);
	refreshLabel ();
	layout ();
}

void toggleStackTrace () {
	refreshObject ();
	layout ();
}

void paintCanvas (Event event) {
	canvas.setCursor (null);
	Object object = listViewer.getStructuredSelection().getFirstElement();
	if (object == null) return;
	GC gc = event.gc;
	if (object instanceof Color) {
		if (((Color)object).isDisposed ()) return;
		gc.setBackground ((Color) object);
		gc.fillRectangle (canvas.getClientArea());
		return;
	}
	if (object instanceof Cursor) {
		if (((Cursor)object).isDisposed ()) return;
		canvas.setCursor ((Cursor) object);
		return;
	}
	if (object instanceof Font) {
		if (((Font)object).isDisposed ()) return;
		gc.setFont ((Font) object);
		FontData [] array = gc.getFont ().getFontData ();
		String string = "";
		String lf = text.getLineDelimiter ();
		for (int i=0; i<array.length; i++) {
			FontData data = array [i];
			String style = "NORMAL";
			int bits = data.getStyle ();
			if (bits != 0) {
				if ((bits & SWT.BOLD) != 0) style = "BOLD ";
				if ((bits & SWT.ITALIC) != 0) style += "ITALIC";
			}
			string += data.getName () + " " + data.getHeight () + " " + style + lf;
		}
		gc.drawString (string, 0, 0);
		return;
	}
	//NOTHING TO DRAW FOR GC
//	if (object instanceof GC) {
//		return;
//	}
	if (object instanceof Image) {
		if (((Image)object).isDisposed ()) return;
		gc.drawImage ((Image) object, 0, 0);
		return;
	}
	if (object instanceof Path) {
		if (((Path)object).isDisposed ()) return;
		gc.drawPath ((Path) object);
		return;
	}
	if (object instanceof Pattern) {
		if (((Pattern)object).isDisposed ()) return;
		gc.setBackgroundPattern ((Pattern)object);
		gc.fillRectangle (canvas.getClientArea ());
		gc.setBackgroundPattern (null);
		return;
	}
	if (object instanceof Region) {
		if (((Region)object).isDisposed ()) return;
		String string = ((Region)object).getBounds().toString();
		gc.drawString (string, 0, 0);
		return;
	}
	if (object instanceof TextLayout) {
		if (((TextLayout)object).isDisposed ()) return;
		((TextLayout)object).draw (gc, 0, 0);
		return;
	}
	if (object instanceof Transform) {
		if (((Transform)object).isDisposed ()) return;
		String string = ((Transform)object).toString();
		gc.drawString (string, 0, 0);
		return;
	}
}

void refreshObject () {
	Object object = listViewer.getStructuredSelection().getFirstElement();
	if (object == null) return;
	if (check.getSelection ()) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream ();
		PrintStream s = new PrintStream (stream);
		int index = Arrays.binarySearch(objects, object);
		errors [index].printStackTrace (s);
		text.setText (stream.toString ());
		text.setVisible (true);
		canvas.setVisible (false);
	} else {
		canvas.setVisible (true);
		text.setVisible (false);
		canvas.redraw ();
	}
	layout();
}

void refreshAll () {
	oldObjects = new Object [0];
	oldErrors = new Error [0];
	refreshDifference ();
	oldObjects = objects;
	oldErrors = errors;
}

void layout () {
	listViewer.getList().getParent().layout();
	canvas.layout();
	text.getParent().layout();
//	Composite parent = canvas.getParent();
//	Rectangle rect = parent.getClientArea ();
//	int width = 0;
//	String [] items = list.getItems ();
//	GC gc = new GC (list);
//	for (int i=0; i<objects.length; i++) {
//		width = Math.max (width, gc.stringExtent (items [i]).x);
//	}
//	gc.dispose ();
//	Point size1 = start.computeSize (SWT.DEFAULT, SWT.DEFAULT);
//	Point size2 = stop.computeSize (SWT.DEFAULT, SWT.DEFAULT);
//	Point size3 = check.computeSize (SWT.DEFAULT, SWT.DEFAULT);
//	Point size4 = label.computeSize (SWT.DEFAULT, SWT.DEFAULT);
//	width = Math.max (size1.x, Math.max (size2.x, Math.max (size3.x, width)));
//	width = Math.max (64, Math.max (size4.x, list.computeSize (width, SWT.DEFAULT).x));
//	start.setBounds (0, 0, width, size1.y);
//	stop.setBounds (0, size1.y, width, size2.y);
//	check.setBounds (0, size1.y + size2.y, width, size3.y);
//	label.setBounds (0, rect.height - size4.y, width, size4.y);
//	int height = size1.y + size2.y + size3.y;
//	list.setBounds (0, height, width, rect.height - height - size4.y);
//	text.setBounds (width, 0, rect.width - width, rect.height);
//	canvas.setBounds (width, 0, rect.width - width, rect.height);
}
		
}
