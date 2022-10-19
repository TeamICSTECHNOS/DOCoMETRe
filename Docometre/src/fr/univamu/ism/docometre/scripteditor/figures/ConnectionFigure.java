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
package fr.univamu.ism.docometre.scripteditor.figures;

import java.util.Arrays;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.DelegatingLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;

import fr.univamu.ism.docometre.Activator;
import fr.univamu.ism.docometre.IImageKeys;
import fr.univamu.ism.docometre.scripteditor.connections.model.BlocksConnection;
import fr.univamu.ism.process.Block;
import fr.univamu.ism.process.DoBlock;
import fr.univamu.ism.process.IfBlock;

public class ConnectionFigure extends PolylineConnection {

	private static final Image trueImage = Activator.getImage(IImageKeys.YES_ICON);
	private static final Image falseImage = Activator.getImage(IImageKeys.NO_ICON);
	private static final Image doLoopImage = Activator.getImage(IImageKeys.DO_LOOP_ICON);
	
	private Label trueLabel = new Label("True", trueImage);
	private Label falseLabel = new Label("False", falseImage);
	private Label doLoopLabel = new Label(doLoopImage);
	private BlocksConnection blocksConnection;
	private IFigure blocksLayer;
	
	public ConnectionFigure(IFigure blocksLayer, BlocksConnection blocksConnection) {
		super();
		this.blocksConnection = blocksConnection;
		this.blocksLayer = blocksLayer;
		setLayoutManager(new DelegatingLayout());
		PolygonDecoration decoration = new PolygonDecoration();
		decoration.setTemplate(PolygonDecoration.TRIANGLE_TIP);
		decoration.setBackgroundColor(ColorConstants.darkGray);
		setTargetDecoration(decoration);
	}
	
	
	@Override
	protected void paintChildren(Graphics graphics) {
		super.paintChildren(graphics);
		updateConnection();
		repaint();
	}
	
	private void updateConnection() {
		if(getChildren().indexOf(trueLabel) != -1) remove(trueLabel);
		if(getChildren().indexOf(falseLabel) != -1) remove(falseLabel);
		if(getChildren().indexOf(doLoopLabel) != -1) remove(doLoopLabel);
		if(blocksConnection.sourceBlock instanceof IfBlock) {
			setIfConnection();
		} else if(blocksConnection.targetBlock instanceof DoBlock) {
			DoBlock doBlock = (DoBlock) blocksConnection.targetBlock;
			Block block = blocksConnection.sourceBlock;
			if(block.isLastBlockOf(doBlock)) {
				setDoConnection();
			} else setSimpleConnection();
		} else setSimpleConnection();
	}

	private void setSimpleConnection() {
		ShortestPathConnectionRouter connectionRouter = new ShortestPathConnectionRouter(blocksLayer);
		setConnectionRouter(connectionRouter);
	}
	
	private void setIfConnection() {
		setSimpleConnection();
		IfBlock sourceIfBlock = (IfBlock)blocksConnection.sourceBlock;
		if(sourceIfBlock.getNextTrueBranchBlock() == blocksConnection.targetBlock) {
			// Decorate with true icon
			add(trueLabel, new ConnectionLocator(this, ConnectionLocator.MIDDLE));
		} else if(sourceIfBlock.getNextFalseBranchBlock() == blocksConnection.targetBlock) {
			// Decorate with false icon
			add(falseLabel, new ConnectionLocator(this, ConnectionLocator.MIDDLE));
		}
	}
	
	private void setDoConnection() {
		int deltaX = 40*(int)Math.signum(blocksConnection.targetBlock.getX() - blocksConnection.sourceBlock.getX());
		BendpointConnectionRouter bendpointConnectionRouter = new BendpointConnectionRouter();
		int x0 = blocksConnection.sourceBlock.getX() + blocksConnection.sourceBlock.getWidth()/2;
		int y0 = blocksConnection.sourceBlock.getY() + blocksConnection.sourceBlock.getHeight()/2;
		int x1 = blocksConnection.targetBlock.getX() + blocksConnection.targetBlock.getWidth()/2;
		int y1 = blocksConnection.targetBlock.getY() + blocksConnection.targetBlock.getHeight()/2;
		Point middlePoint = new Point((x0+x1)/2 + deltaX, (y0+y1)/2);
		AbsoluteBendpoint absoluteBendpoint = new AbsoluteBendpoint(middlePoint);
		bendpointConnectionRouter.setConstraint(this, Arrays.asList(new Bendpoint[] {absoluteBendpoint}));
		setConnectionRouter(bendpointConnectionRouter);
		// Decorate with loop icon
		add(doLoopLabel, new ConnectionLocator(this, ConnectionLocator.MIDDLE));
	}

}
