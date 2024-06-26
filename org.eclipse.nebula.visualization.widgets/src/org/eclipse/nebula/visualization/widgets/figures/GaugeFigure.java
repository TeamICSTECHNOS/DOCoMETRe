/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.nebula.visualization.widgets.figures;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.visualization.widgets.figureparts.RoundScale;
import org.eclipse.nebula.visualization.widgets.figureparts.RoundScaleTickMarks;
import org.eclipse.nebula.visualization.widgets.figureparts.RoundScaledRamp;
import org.eclipse.nebula.visualization.widgets.util.GraphicsUtil;
import org.eclipse.nebula.visualization.widgets.util.PointsUtil;
import org.eclipse.nebula.visualization.xygraph.linearscale.AbstractScale.LabelSide;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;


/**
 * The figure of gauge
 * @author Xihui Chen
 *
 */
public class GaugeFigure extends AbstractRoundRampedFigure {

	private final Color WHITE_COLOR = XYGraphMediaFactory.getInstance().getColor(
			XYGraphMediaFactory.COLOR_WHITE); 
	private final Color BORDER_COLOR = XYGraphMediaFactory.getInstance().getColor(
			new RGB(100, 100, 100)); 
	private final Color GRAY_COLOR = XYGraphMediaFactory.getInstance().getColor(
			XYGraphMediaFactory.COLOR_GRAY); 
	private final Color DEFAULT_NEEDLE_COLOR = XYGraphMediaFactory.getInstance().getColor(
			XYGraphMediaFactory.COLOR_RED);
	private final Font DEFAULT_LABEL_FONT = XYGraphMediaFactory.getInstance().getFont(
			new FontData("Arial", 12, SWT.BOLD));
	private final static int BORDER_WIDTH = 2;
	
	private boolean effect3D = true;
	
	private NeedleCenter needleCenter;
	
	private Needle needle;
	
	private Label valueLabel;
	private Boolean support3D;
	
	private Label unitLabel;
	private String unit;
	
	private Label titleLabel;
	private String title;
	
	public GaugeFigure() {
		super();
		transparent = true;
		scale.setScaleLineVisible(false);
		scale.setTickLabelSide(LabelSide.Secondary);
		ramp.setRampWidth(10);
		
		valueLabel = new Label();	
		valueLabel.setFont(DEFAULT_LABEL_FONT);
		
		unitLabel = new Label();
		unitLabel.setFont(DEFAULT_LABEL_FONT);
		
		titleLabel = new Label();
		titleLabel.setFont(DEFAULT_LABEL_FONT);
		
		needle = new Needle();
		needle.setFill(true);
		needle.setOutline(false);
		
		needleCenter = new NeedleCenter();
		needleCenter.setOutline(false);
		
		setLayoutManager(new GaugeLayout());
		add(ramp, GaugeLayout.RAMP);
		add(scale, GaugeLayout.SCALE);		
		add(valueLabel, GaugeLayout.VALUE_LABEL);
		add(unitLabel, GaugeLayout.UNIT_LABEL);
		add(titleLabel, GaugeLayout.TITLE_LABEL);
		add(needle, GaugeLayout.NEEDLE);
		add(needleCenter, GaugeLayout.NEEDLE_CENTER);		
		addFigureListener(new FigureListener() {			
			public void figureMoved(IFigure source) {
				ramp.setDirty(true);
				revalidate();	
			}
		});	
		
	}
	@Override
	public void setBounds(Rectangle rect) {
		
		super.setBounds(rect);
	}
	
	@Override
	public void setValue(double value) {
		super.setValue(value);
		valueLabel.setText(getValueText());			
	}
	
	@Override
	protected void paintClientArea(Graphics graphics) {
		graphics.setAntialias(SWT.ON);
		Rectangle area = getClientArea();
		
		Point center = area.getCenter();		
		
		area.width = Math.min(area.width, area.height) - 10;
		area.height = area.width;
//		Pattern pattern = null;
		graphics.pushState();
		graphics.setBackgroundColor(GRAY_COLOR);
		
//		if(support3D == null)
//			support3D = GraphicsUtil.testPatternSupported(graphics);
//		if(effect3D && support3D) {		
//			//add this to eliminate the repaint bug on Mac
//			//Who added this? this will cause problem in zoom.
//			graphics.fillOval(new Rectangle());
//			pattern = GraphicsUtil.createScaledPattern(graphics, Display.getCurrent(), area.x, area.y, 
//				area.x+area.width, area.y + area.height, BORDER_COLOR, WHITE_COLOR);
//			graphics.setBackgroundPattern(pattern);				
//		}	
		
		Rectangle ovalBounds = area.getCopy();
		ovalBounds.x = center.x - ovalBounds.width/2;
		ovalBounds.y = center.y - ovalBounds.height/2;
		graphics.fillOval(ovalBounds);		
//		if(effect3D && support3D){	
//			pattern.dispose();
//			area.shrink(BORDER_WIDTH, BORDER_WIDTH);
//		}else
			area.shrink(1, 1);
		graphics.popState();
//		
		graphics.fillOval(ovalBounds);
//		
		super.paintClientArea(graphics);
		
			//glossy effect
//		if(effect3D && support3D) {
//			graphics.pushState();
//			graphics.setAntialias(SWT.ON);
//			final double R = area.width/2.0d;
//			final double UD_FILL_PART = 9.5d/10d;
//			final double UP_DOWN_RATIO = 1d/2d;
//			final double LR_FILL_PART = 8.5d/10d;
//			final double UP_ANGLE = 0d * Math.PI/180d;
//			final double DOWN_ANGLE = 35d * Math.PI/180d;
////
//			Pattern glossyPattern = GraphicsUtil.createScaledPattern(graphics, Display.getCurrent(), 
//					area.x + center.x - area.width/2, (float)(area.y + center.y - area.height/2 - R * UD_FILL_PART),
//					area.x + center.x - area.width/2, (float) (area.y + center.y - area.height/2 + R * UP_DOWN_RATIO),
//					WHITE_COLOR, 90, WHITE_COLOR, 0);
//			graphics.setBackgroundPattern(glossyPattern);
//			Rectangle rectangle = new Rectangle(
//					(int)(area.x + center.x  - area.width/2  - R * LR_FILL_PART *Math.cos(UP_ANGLE)),
//					(int)(area.y + center.y - area.height/2  - R * UD_FILL_PART),
//					(int)(2*R* LR_FILL_PART*Math.cos(UP_ANGLE)), (int)(R*UD_FILL_PART + R*UP_DOWN_RATIO));			
//			graphics.fillOval(rectangle);
//			glossyPattern.dispose();
//			
//			glossyPattern = GraphicsUtil.createScaledPattern(graphics, Display.getCurrent(), 
//					area.x + area.width/2, (float)(area.y + area.height/2 + R * UP_DOWN_RATIO -1),
//					area.x + area.width/2, (float) (area.y + area.height/2 + R * UD_FILL_PART + 1),
//					WHITE_COLOR, 0, WHITE_COLOR, 40);
//			graphics.setBackgroundPattern(glossyPattern);
//			rectangle = new Rectangle(
//					(int)(area.x + area.width/2 + center.x - R*LR_FILL_PART*Math.sin(DOWN_ANGLE)),
//					(int)Math.ceil(area.y + area.height/2 + center.y + R * UP_DOWN_RATIO),
//					(int)(2*R*LR_FILL_PART*Math.sin(DOWN_ANGLE)), 
//					(int)Math.ceil(R*UD_FILL_PART - R*UP_DOWN_RATIO));	
//			graphics.fillOval(rectangle);
//			glossyPattern.dispose();
//			graphics.popState();			
//		}
		
		
	}
	
	/**
	 * @param needleColor the needleColor to set
	 */
	public void setNeedleColor(Color needleColor) {
		titleLabel.setForegroundColor(needleColor);
		unitLabel.setForegroundColor(needleColor);
		valueLabel.setForegroundColor(needleColor);
		needle.setBackgroundColor(needleColor);
	}
	
	public Color getNeedleColor(){
		return needle.getBackgroundColor();
	}

	/**
	 * @param effect3D the effect3D to set
	 */
	public void setEffect3D(boolean effect3D) {
		if(this.effect3D == effect3D)
			return;
		this.effect3D = effect3D;
		repaint();
	}

	/**
	 * @return the effect3D
	 */
	public boolean isEffect3D() {
		return effect3D;
	}
	
	/**
	 * @return the displayed unit
	 */
	public String getUnit() {
		return unit;
	}
	
	/**
	 * @param unit unit that will be displayed above the value label
	 */
	public void setUnit(String unit) {
		this.unit = unit;
		unitLabel.setText(unit);		
	}
	
	/**
	 * @return the title associated to this gauge
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @param title title that will be displayed under the value label
	 */
	public void setTitle(String title) {
		this.title = title;
		titleLabel.setText(title);
	}



	class Needle extends Polygon {
		public Needle() {
			setBackgroundColor(DEFAULT_NEEDLE_COLOR);
		}
		@Override
		protected void fillShape(Graphics g) {
			g.setAntialias(SWT.ON);
			super.fillShape(g);
		}
	}

	class NeedleCenter extends Ellipse {
		
		public static final int DIAMETER = 16;
	
		@Override
		protected void fillShape(Graphics graphics) {
			graphics.setAntialias(SWT.ON);
			Pattern pattern = null;
			graphics.setBackgroundColor(GRAY_COLOR);
			if(support3D == null)
				support3D = GraphicsUtil.testPatternSupported(graphics);
			if(effect3D && support3D){		
					pattern = GraphicsUtil.createScaledPattern(graphics, Display.getCurrent(), bounds.x, bounds.y,
							bounds.x + bounds.width, bounds.y + bounds.height, WHITE_COLOR, BORDER_COLOR);
					graphics.setBackgroundPattern(pattern);							
			}			
			super.fillShape(graphics);
			if(effect3D && support3D)
				pattern.dispose();			
		}		
	}
	

	class GaugeLayout extends AbstractLayout {
		
		private static final int GAP_BTW_NEEDLE_SCALE = -1;
		
		/** Used as a constraint for the scale. */
		public static final String SCALE = "scale";   //$NON-NLS-1$
		/** Used as a constraint for the Needle. */
		public static final String NEEDLE = "needle"; //$NON-NLS-1$
		/** Used as a constraint for the Ramp */
		public static final String RAMP = "ramp";      //$NON-NLS-1$
		/** Used as a constraint for the needleCenter */
		public static final String NEEDLE_CENTER = "needleCenter";      //$NON-NLS-1$
		/** Used as a constraint for the value label*/
		public static final String VALUE_LABEL = "valueLabel";      //$NON-NLS-1$
		/** Used as a constraint for the unit label*/
		public static final String UNIT_LABEL = "unitLabel";      //$NON-NLS-1$
		/** Used as a constraint for the title label*/
		public static final String TITLE_LABEL = "titleLabel";      //$NON-NLS-1$
		
		private RoundScale scale;
		private RoundScaledRamp ramp;
		private Polygon needle;
		private NeedleCenter needleCenter;
		private Label valueLabel;
		private Label titleLabel;
		private Label unitLabel;
		private PointList needlePoints = new PointList(new int[] {0,0,0,0,0,0});
		
		
		@Override
		public void setConstraint(IFigure child, Object constraint) {
			if(constraint.equals(SCALE))
				scale = (RoundScale)child;
			else if (constraint.equals(RAMP))
				ramp = (RoundScaledRamp) child;
			else if (constraint.equals(NEEDLE))
				needle = (Polygon) child;
			else if (constraint.equals(NEEDLE_CENTER))
				needleCenter = (NeedleCenter) child;
			else if (constraint.equals(VALUE_LABEL))
				valueLabel = (Label)child;
			else if (constraint.equals(TITLE_LABEL))
				titleLabel = (Label)child;
			else if (constraint.equals(UNIT_LABEL))
				unitLabel = (Label)child;
			
		}


		@Override
		protected Dimension calculatePreferredSize(IFigure container, int w,
				int h) {
			Insets insets = container.getInsets();
			Dimension d = new Dimension(256, 256);
			d.expand(insets.getWidth(), insets.getHeight());
			return d;
		}


		public void layout(IFigure container) {
			Rectangle area = container.getClientArea();
			
			Point center = area.getCenter();		
			
			area.width = Math.min(area.width, area.height);
			area.height = area.width;
			area.shrink(BORDER_WIDTH, BORDER_WIDTH);
			
			if(scale != null) {
				Rectangle scaleBounds = area.getCopy();
				scaleBounds.x = center.x - scaleBounds.width/2;
				scaleBounds.y = center.y - scaleBounds.height/2;
				scale.setBounds(scaleBounds);
			}
			
			if(ramp != null && ramp.isVisible()) {
				Rectangle rampBounds = area.getCopy();
				rampBounds.x = center.x - rampBounds.width/2;
				rampBounds.y = center.y - rampBounds.height/2;
				ramp.setBounds(rampBounds.shrink(area.width/4, area.height/4));
			}
			
			if(valueLabel != null) {
				Dimension labelSize = valueLabel.getPreferredSize();
				valueLabel.setBounds(new Rectangle(area.x + center.x - labelSize.width/2,
						(int)(area.y + 2 * center.y * 6.3f/8 - labelSize.height/2),
						labelSize.width, labelSize.height));
			}
			
			if(title != null) {
				Dimension titleSize = titleLabel.getPreferredSize();
				titleLabel.setBounds(new Rectangle(area.x + center.x - titleSize.width/2,
						(int)(area.y + 2 * center.y * 7.1f/8 - titleSize.height/2),
						titleSize.width, titleSize.height));
			}
			
			if(unit != null) {
				Dimension unitSize = unitLabel.getPreferredSize();
				unitLabel.setBounds(new Rectangle(area.x + center.x - unitSize.width/2,
						(int)(area.y +  2 * center.y * 5.5f/8 - unitSize.height/2),
						unitSize.width, unitSize.height));
			}
			
			if(needle != null && scale != null) {
				needlePoints.setPoint (
						new Point(center.x, center.y - NeedleCenter.DIAMETER/2 + 3), 0);
				scale.getScaleTickMarks();
				needlePoints.setPoint(
						new Point(center.x + area.width/2 - RoundScaleTickMarks.MAJOR_TICK_LENGTH
						- GAP_BTW_NEEDLE_SCALE, center.y), 1);
				needlePoints.setPoint(
						new Point(center.x, center.y + NeedleCenter.DIAMETER/2 - 3), 2);
	
				double valuePosition = 360 - scale.getValuePosition(getCoercedValue(), false);
				if(maximum > minimum){
					if(value > maximum)
						valuePosition += 10;
					else if(value < minimum)
						valuePosition -=10;
				}else{
					if(value > minimum)
						valuePosition -= 10;
					else if(value < maximum)
						valuePosition +=10;
				}
				needlePoints.setPoint(
						PointsUtil.rotate(needlePoints.getPoint(0),	valuePosition, center), 0);
				needlePoints.setPoint(
						PointsUtil.rotate(needlePoints.getPoint(1), valuePosition, center), 1);
				needlePoints.setPoint(
						PointsUtil.rotate(needlePoints.getPoint(2), valuePosition, center),2);				
				needle.setPoints(needlePoints);			
				
			}
			
			if(needleCenter != null){
				needleCenter.setBounds(new Rectangle(center.x - NeedleCenter.DIAMETER/2,
						center.y - NeedleCenter.DIAMETER/2,
						NeedleCenter.DIAMETER, NeedleCenter.DIAMETER));
			}		
						
		}		
	}
	
}
