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
 *  - Frank Buloup - frank.buloup@univ-amu.fr - initial API and implementation [01/06/2024]
 ******************************************************************************/
package fr.univamu.ism.nrtswtchart;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class RTSWTXYSerie {
	
	private double[] xValues;
	private double[] yValues;
	private int currentIndex;
	
	private int nbPoints = 10000;

	private double lastPointX;
	private double lastPointY;

	private RTSWTXYChart rtswtChart;
	private boolean modified;
	private String title;
	private Color color;

	protected RTSWTXYSerie(RTSWTXYChart rtswtChart, String title, Color color) {
		if(rtswtChart == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if(title == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if(title.equals("")) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		if(color == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if(color.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.rtswtChart = rtswtChart;
		this.title = title;
		this.color = color;
	}

	protected Color getColor() {
		return color;
	}

	protected String getTitle() {
		return title;
	}

	public void addPoints(final Double[] x, final Double[] y) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if(xValues == null || yValues == null) 
					reset();
				double dx = rtswtChart.getDx();
				double dy = rtswtChart.getDy();
				for (int i = 0; i < x.length; i++) {
					int nbPixelsX = Math.abs((int)((x[i] - lastPointX) / dx));
					int nbPixelsY = Math.abs((int)((y[i] - lastPointY) / dy));
					if(nbPixelsX > 0 || nbPixelsY > 0 || xValues[0] == Double.NaN) {
						addValue(x[i], y[i]);
						setModified(true);
					}
				}
				rtswtChart.checkUpdate();
			}
		});
	}

	protected void reset() {
		xValues = new double[nbPoints];
		yValues = new double[nbPoints];
		for(int i = 0; i < nbPoints; i++) {
			xValues[i] = Double.NaN;
			yValues[i] = Double.NaN;
		}
		lastPointX = - rtswtChart.getDx();
		lastPointY = - rtswtChart.getDy();
		currentIndex = 0;
		setModified(false);
	}

	private void addValue(double x, double y) {
		if(currentIndex >= nbPoints) {
			System.arraycopy(xValues, 1, xValues, 0, xValues.length-1);
			System.arraycopy(yValues, 1, yValues, 0, yValues.length-1);
			currentIndex = nbPoints - 1;
		}
		xValues[currentIndex] = x;
		yValues[currentIndex] = y;
		currentIndex ++;
		lastPointX = x;
		lastPointY = y;
	}

	protected boolean getModified() {
		return this.modified;
	}

	protected void setModified(boolean modified) {
		this.modified = modified;
	}

	protected double getyMin() {
		return RTSWTChartUtils.getMin(yValues);
	}

	protected double getyMax() {
		return RTSWTChartUtils.getMax(yValues);
	}

	protected double getxMin() {
		return RTSWTChartUtils.getMin(xValues);
	}

	protected double getxMax() {
		return RTSWTChartUtils.getMax(xValues);
	}

	protected int[] getPointsArrayToDraw() {
		double xMin = rtswtChart.getxMin();
		double yMin = rtswtChart.getyMin();
		double dx = rtswtChart.getDx();
		double dy = rtswtChart.getDy();
		ArrayList<Integer> pointsArray = new ArrayList<Integer>(0);
		int height = rtswtChart.getHeight() - rtswtChart.getBottomAxisHeight() - rtswtChart.getLegendHeight() - 1;
		for (int i = 0; i < xValues.length; i++) {
			if(!Double.isNaN(xValues[i])) {
				int vx = (int) Math.round((xValues[i] - xMin)/dx);
				int vy = height - (int) Math.round((yValues[i] - yMin)/dy);
				pointsArray.add(vx);
				pointsArray.add(vy);
			}
		}
		int[] pointsInt = new int[pointsArray.size()];
		for (int i = 0; i < pointsInt.length; i++) pointsInt[i] = pointsArray.get(i);
		return pointsInt;
	}
}